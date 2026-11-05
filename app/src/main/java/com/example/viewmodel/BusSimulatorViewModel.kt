package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BusSoundEngine
import com.example.model.AnimatedPedestrian
import com.example.model.BusJobContract
import com.example.model.BusLivery
import com.example.model.BusSimulationState
import com.example.model.ChatMessage
import com.example.model.MultiplayerDriver
import com.example.model.Passenger
import com.example.model.RoadHazard
import com.example.model.RouteStage
import com.example.model.RouteZone
import com.example.model.StartingTerminal
import com.example.model.SteeringControlMode
import com.example.model.TrafficLightState
import com.example.model.TrafficVehicle
import com.example.model.TurnSignalState
import com.example.model.ViewMode
import com.example.model.WeatherType
import com.example.model.WiperSpeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

class BusSimulatorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val prefs: SharedPreferences = application.getSharedPreferences("ph_bus_sim_prefs", Context.MODE_PRIVATE)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val soundEngine = BusSoundEngine(application)

    // Available Starting Terminals
    val availableTerminals = listOf(
        StartingTerminal.CUBAO,
        StartingTerminal.PASAY,
        StartingTerminal.PITX
    )

    // Job Market Contracts (ETS2 Freight Market style)
    val availableContracts = listOf(
        BusJobContract("job-cubao-baguio", "Victory Liner Express", "Cubao Terminal Bay 7", "Baguio City Terminal", 16800, 245f, 240, "45 Passengers + Baguio Cold Express Cargo"),
        BusJobContract("job-pasay-calamba", "DLTB Co. Executive", "Pasay Terminal Platform 3", "Batangas Pier / Calamba", 12400, 110f, 130, "42 Commuters + Southern Freight"),
        BusJobContract("job-pitx-tagaytay", "JAC Liner Tagaytay Transit", "PITX Bay 4 CAVITEX", "Tagaytay Ridge Terminal", 9800, 68f, 90, "38 Weekend Tourists + Fresh Goods")
    )

    private val _uiState = MutableStateFlow(
        BusSimulationState(
            controlMode = loadSavedControlMode()
        )
    )
    val uiState: StateFlow<BusSimulationState> = _uiState.asStateFlow()

    val availableLiveries = listOf(
        BusLivery("victory_liner", "Victory Liner Golden Sun", 0xFFFFC107, 0xFFD32F2F, "Victory Express", "Iconic provincial carrier with gold-red speed stripes."),
        BusLivery("genesis_joy", "Genesis JoyBus Royal Blue", 0xFF1976D2, 0xFFFFEB3B, "Genesis Transport", "Executive express service along Northern Luzon expressway."),
        BusLivery("philtranco_classic", "Philtranco Centennial Fleet", 0xFF00796B, 0xFFCFD8DC, "Philtranco Service", "Pioneering Philippine Southern Luzon long-distance fleet."),
        BusLivery("dltb_scania", "DLTB Co. Red Dragon", 0xFFD50000, 0xFF212121, "DLTB Express", "Modern turbocharged rear-engine aerodynamic coach.")
    )

    private val _multiplayerDrivers = MutableStateFlow(
        listOf(
            MultiplayerDriver("p1", "CaptainPinoy_99", "Scania Marcopolo", "Cubao Bay 7", 0, 0.0f, 995, true),
            MultiplayerDriver("p2", "ManilaExpress_Kuya", "Hino Grand View", "EDSA Cubao", 38, 2.4f, 960, false),
            MultiplayerDriver("p3", "BatangasKing_Leo", "Volvo B11R", "SLEX Calamba", 85, 14.8f, 982, true),
            MultiplayerDriver("p4", "BaguioClimber_77", "Daewoo Cruising", "Bocaue Exit", 92, 28.3f, 940, false)
        )
    )
    val multiplayerDrivers: StateFlow<List<MultiplayerDriver>> = _multiplayerDrivers.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("1", "Terminal Dispatch", "HAM 144.200", "Clearance granted. Passengers are boarding on the platform. Conduct pre-trip check.", "21:00", isSystem = true),
            ChatMessage("2", "CaptainPinoy_99", "HAM 144.200", "Copy base! Engine warming up, GPS route advisor active.", "21:02"),
            ChatMessage("3", "ManilaExpress_Kuya", "Traffic Advisory", "Caution: City avenue traffic moving at 35 kph. Watch for jeepneys pulling over.", "21:05")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private var acceleratorPressed = false
    private var brakePressed = false
    private var simTickCounter = 0
    private var wiperPhase = 0f
    private var footstepTick = 0

    init {
        initializeSamplePassengers()
        initializeInitialTraffic()
        initializeAnimatedPedestrians()
        registerGyroscopeSensor()
        startSimulationLoop()
    }

    private fun loadSavedControlMode(): SteeringControlMode {
        val savedName = prefs.getString("control_mode", SteeringControlMode.STEERING_WHEEL.name)
        return try {
            SteeringControlMode.valueOf(savedName ?: SteeringControlMode.STEERING_WHEEL.name)
        } catch (_: Exception) {
            SteeringControlMode.STEERING_WHEEL
        }
    }

    fun selectStartingTerminal(terminal: StartingTerminal) {
        val job = when (terminal) {
            StartingTerminal.CUBAO -> availableContracts[0]
            StartingTerminal.PASAY -> availableContracts[1]
            StartingTerminal.PITX -> availableContracts[2]
        }
        _uiState.update {
            it.copy(
                selectedTerminal = terminal,
                activeJobContract = job,
                currentRoute = terminal.defaultRoute,
                currentStage = RouteStage.TERMINAL_BAY,
                distanceTraveledKm = 0f,
                cityStreetDistanceTraveledKm = 0f,
                routeProgressPercent = 0f,
                terminalBayCleared = false,
                passengersBoardedAtTerminal = false,
                speedKph = 0f,
                rpm = 0,
                isEngineRunning = false,
                passengerDoorsOpen = true,
                parkingBrakeOn = true,
                tollGateDistanceMeters = 1200f
            )
        }
        initializeSamplePassengers()
        initializeAnimatedPedestrians()
    }

    fun setControlMode(mode: SteeringControlMode) {
        prefs.edit().putString("control_mode", mode.name).apply()
        _uiState.update { it.copy(controlMode = mode) }
    }

    private fun registerGyroscopeSensor() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || _uiState.value.controlMode != SteeringControlMode.GYROSCOPE_TILT) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // In Landscape orientation:
            // Tilting phone left/right corresponds to event.values[1] (Y-axis tilt in landscape)
            val tiltY = event.values[1] // -9.8 to +9.8 m/s^2
            // Calculate tilt angle in degrees: approx -30° to +30°
            val angleDeg = (tiltY * 5.0f).coerceIn(-30f, 30f)

            // Deadzone of 2.5 degrees in the center to prevent jittering
            val normalizedSteer = if (abs(angleDeg) < 2.5f) 0f else {
                val sign = if (angleDeg > 0) 1f else -1f
                ((abs(angleDeg) - 2.5f) / 27.5f * sign).coerceIn(-1f, 1f)
            }

            _uiState.update {
                it.copy(
                    deviceTiltAngleDeg = angleDeg,
                    steeringAngle = normalizedSteer * (it.wheelSensitivity / 60f)
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun initializeSamplePassengers() {
        val term = _uiState.value.selectedTerminal
        val initialList = listOf(
            Passenger("P-01", "Aling Maria Santos", "${term.displayName} Bay", term.destinationCity, term.baseFare, 98f),
            Passenger("P-02", "Engr. Carlo Reyes", "${term.displayName} Bay", term.destinationCity, term.baseFare, 95f),
            Passenger("P-03", "Beatriz Aquino", "${term.displayName} Bay", term.destinationCity, term.baseFare, 96f),
            Passenger("P-04", "Kuya Danilo", "${term.displayName} Bay", term.destinationCity, term.baseFare, 92f),
            Passenger("P-05", "Nurse Joy Mendoza", "${term.displayName} Bay", term.destinationCity, term.baseFare, 99f)
        )
        _uiState.update { it.copy(passengers = initialList) }
    }

    private fun initializeInitialTraffic() {
        val initialTraffic = listOf(
            TrafficVehicle("tv1", "Jeepney (Sarao Classic)", 2, 35f, 0f, 0xFFFFD600, isJeepneyStopped = true, hazardLights = true),
            TrafficVehicle("tv2", "UV Express (Toyota HiAce)", 1, 65f, 42f, 0xFFFFFFFF, signalingLeft = true),
            TrafficVehicle("tv3", "Sedan (Toyota Vios Taxi)", 0, 110f, 48f, 0xFFE2E8F0),
            TrafficVehicle("tv4", "City Bus (Valenzuela Transport)", 1, 160f, 40f, 0xFF22C55E),
            TrafficVehicle("tv5", "Delivery Truck (Isuzu Elf)", 0, 210f, 45f, 0xFF3B82F6)
        )
        val initialHazards = listOf(
            RoadHazard("Jeepney Loading/Unloading Stop", 35f, "JeepneyStop", "Passengers crossing street ahead - Slow down and watch right curb"),
            RoadHazard("MMDA Traffic Light Intersection", 120f, "TrafficLight", "Follow traffic light cycle before merging toward Balintawak")
        )
        _uiState.update { it.copy(aiVehicles = initialTraffic, activeHazards = initialHazards) }
    }

    private fun initializeAnimatedPedestrians() {
        val peds = listOf(
            AnimatedPedestrian("ped1", "Maria", 0.15f, 0.72f, 0.48f, 0.0035f, 0f, false, 0xFF38BDF8),
            AnimatedPedestrian("ped2", "Carlo", 0.05f, 0.74f, 0.48f, 0.0030f, 0.5f, false, 0xFFF43F5E),
            AnimatedPedestrian("ped3", "Joy", 0.82f, 0.71f, 0.52f, -0.0032f, 0.2f, false, 0xFFEAB308),
            AnimatedPedestrian("ped4", "Danilo", 0.92f, 0.73f, 0.52f, -0.0028f, 0.8f, false, 0xFF10B981)
        )
        _uiState.update { it.copy(pedestrians = peds) }
    }

    private fun startSimulationLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(33) // ~30-60 FPS smooth motion loop
                updatePhysicsAndTelemetry()
            }
        }

        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                updateWorldAndTraffic()
            }
        }
    }

    private fun updatePhysicsAndTelemetry() {
        simTickCounter++
        footstepTick++

        _uiState.update { state ->
            // Pedestrian Walking Animation Update
            val updatedPedestrians = state.pedestrians.map { ped ->
                if (ped.hasBoarded) return@map ped
                var currentX = ped.xPos
                val phase = (ped.walkCyclePhase + 0.18f) % (Math.PI.toFloat() * 2f)

                // Move toward bus entrance door (approx x = 0.50f)
                if (abs(currentX - ped.targetX) > 0.01f) {
                    currentX += ped.speed
                } else if (state.passengerDoorsOpen && state.currentStage == RouteStage.TERMINAL_BAY) {
                    // Vanish into bus when boarding door reached
                    ped.hasBoarded = true
                }

                ped.copy(xPos = currentX, walkCyclePhase = phase)
            }

            // Footstep sound synchronized with walking cycle (only when in terminal)
            if (state.currentStage == RouteStage.TERMINAL_BAY && footstepTick % 12 == 0 && state.activeViewMode != ViewMode.COCKPIT) {
                soundEngine.playFootstep()
            }

            // Update terminal ambient crowd chatter
            val isInTerminal = state.currentStage == RouteStage.TERMINAL_BAY
            soundEngine.updateTerminalCrowdAudio(isInTerminal, if (state.activeViewMode == ViewMode.COCKPIT) 0.35f else 0.85f)

            if (!state.isEngineRunning) {
                soundEngine.updateEngineState(0, false, false)
                return@update state.copy(
                    speedKph = (state.speedKph - 1.2f).coerceAtLeast(0f),
                    rpm = (state.rpm - 25).coerceAtLeast(0),
                    cabinPitch = state.cabinPitch * 0.85f,
                    cabinRoll = state.cabinRoll * 0.85f,
                    cabinBounce = 0f,
                    roadBumpOffset = 0f,
                    pedestrians = updatedPedestrians
                )
            }

            val weatherFriction = state.weather.frictionMultiplier
            val speedLimit = state.currentStage.speedLimitKph.toFloat()
            val maxSpeed = speedLimit + 25f

            var currentSpeed = state.speedKph
            var gear = state.gear
            var rpm = state.rpm
            var cPitch = state.cabinPitch
            var cRoll = state.cabinRoll
            var fuel = state.fuelLevelPercent
            var tireWear = state.tireWearPercent
            var airPressure = state.airBrakePressurePsi
            var damage = state.vehicleDamagePercent
            var collisionWarning = state.collisionMessage
            var hasCollided = false

            if (state.parkingBrakeOn) {
                currentSpeed = (currentSpeed - 3.8f).coerceAtLeast(0f)
                rpm = 850
            } else {
                if (acceleratorPressed) {
                    val accelRate = (1.45f * weatherFriction) * (if (gear == "D1") 1.8f else 1.15f)
                    currentSpeed = (currentSpeed + accelRate).coerceAtMost(maxSpeed)
                    cPitch = (cPitch + 0.18f).coerceAtMost(2.8f)
                    fuel = (fuel - 0.003f).coerceAtLeast(0f)
                    tireWear = (tireWear + 0.001f).coerceAtMost(100f)
                    airPressure = (airPressure + 0.08f).coerceAtMost(125f)
                } else if (brakePressed) {
                    val brakeRate = 3.2f * weatherFriction
                    val prevSpeed = currentSpeed
                    currentSpeed = (currentSpeed - brakeRate).coerceAtLeast(0f)
                    airPressure = (airPressure - 0.12f).coerceAtLeast(65f)
                    cPitch = (cPitch - 0.38f).coerceAtLeast(-4.0f)
                    tireWear = (tireWear + 0.002f).coerceAtMost(100f)

                    if (prevSpeed > 2f && currentSpeed <= 0.1f) {
                        soundEngine.playAirBrakeRelease()
                    }
                } else {
                    currentSpeed = (currentSpeed - 0.28f).coerceAtLeast(0f)
                    cPitch *= 0.88f
                }

                // Automatic Gear & RPM calculation
                when {
                    currentSpeed < 1f -> {
                        gear = "D1"
                        rpm = 850 + (currentSpeed * 20).toInt()
                    }
                    currentSpeed < 20f -> {
                        gear = "D1"
                        rpm = 950 + ((currentSpeed / 20f) * 1100).toInt()
                    }
                    currentSpeed < 38f -> {
                        gear = "D2"
                        rpm = 1050 + (((currentSpeed - 20f) / 18f) * 1050).toInt()
                    }
                    currentSpeed < 55f -> {
                        gear = "D3"
                        rpm = 1150 + (((currentSpeed - 38f) / 17f) * 1000).toInt()
                    }
                    currentSpeed < 75f -> {
                        gear = "D4"
                        rpm = 1250 + (((currentSpeed - 55f) / 20f) * 950).toInt()
                    }
                    else -> {
                        gear = "D5"
                        rpm = 1350 + (((currentSpeed - 75f) / 35f) * 900).toInt()
                    }
                }
            }

            soundEngine.updateEngineState(rpm, state.isEngineRunning, acceleratorPressed)

            val bumpFrequency = if (currentSpeed > 5f) (currentSpeed / 35f) else 0f
            val roadBump = (sin(simTickCounter * 0.40f) * bumpFrequency * 1.8f)
            cRoll = (state.steeringAngle * -4.2f) * (currentSpeed / 50f)
            val sway = (cRoll * 2.2f + sin(simTickCounter * 0.15f) * (currentSpeed / 20f)).coerceIn(-35f, 35f)
            val cBounce = (sin(simTickCounter * 0.35f) * (currentSpeed / 45f) * 2f)
            val dynamicSeatbelt = (35f + abs(cPitch) * 14f + abs(cRoll) * 9f).coerceIn(20f, 95f)

            val distanceIncrement = (currentSpeed * (33f / 3600000f))
            val newDistance = state.distanceTraveledKm + distanceIncrement
            val newProgress = (state.routeProgressPercent + (distanceIncrement * 5f)).coerceAtMost(100f)
            val newLateral = (state.busLateralPosition + (state.steeringAngle * 0.012f * (currentSpeed / 40f))).coerceIn(0.1f, 0.9f)

            var currentStage = state.currentStage
            var clearedBay = state.terminalBayCleared
            val newCityDist = state.cityStreetDistanceTraveledKm + distanceIncrement

            if (currentStage == RouteStage.TERMINAL_BAY && newDistance > 0.15f) {
                currentStage = RouteStage.CITY_STREETS
                clearedBay = true
            } else if (currentStage == RouteStage.CITY_STREETS && newCityDist > 1.2f) {
                currentStage = RouteStage.NLEX_TOLL_ENTRY
            } else if (currentStage == RouteStage.NLEX_TOLL_ENTRY && state.tollGateOpen && state.tollGateDistanceMeters < 15f) {
                currentStage = RouteStage.EXPRESSWAY_CRUISE
            }

            // Wiper animation blade angle
            var bladeAngle = state.wiperBladeAngle
            var waterOnWindshield = state.windshieldWaterLevel
            if (state.weather == WeatherType.RAIN || state.weather == WeatherType.HEAVY_STORM) {
                waterOnWindshield = (waterOnWindshield + 0.15f).coerceAtMost(100f)
            }
            if (state.wiperSetting != WiperSpeed.OFF) {
                wiperPhase += when (state.wiperSetting) {
                    WiperSpeed.INTERMITTENT -> 0.08f
                    WiperSpeed.NORMAL -> 0.15f
                    WiperSpeed.FAST -> 0.28f
                    WiperSpeed.OFF -> 0f
                }
                bladeAngle = abs(sin(wiperPhase)) * 85f
                waterOnWindshield = (waterOnWindshield - 0.45f).coerceAtLeast(0f)
            }

            // Toll gate distance calculation
            val newTollDist = if (currentStage == RouteStage.NLEX_TOLL_ENTRY || currentStage == RouteStage.EXPRESSWAY_CRUISE) {
                if (state.tollGateDistanceMeters > 0) {
                    (state.tollGateDistanceMeters - (currentSpeed * (33f / 3600f))).coerceAtLeast(0f)
                } else 350f
            } else 1200f

            val playerLane = when {
                newLateral < 0.35f -> 0
                newLateral > 0.65f -> 2
                else -> 1
            }

            // Active Moving AI Traffic Loop (incrementing coordinates frame-by-frame)
            val updatedTraffic = state.aiVehicles.map { vehicle ->
                var vSpeed = vehicle.speedKph
                var vLane = vehicle.lane
                var vDist = vehicle.distanceMeters
                var isStopped = vehicle.isJeepneyStopped
                var sigLeft = vehicle.signalingLeft
                var sigRight = vehicle.signalingRight
                var hazard = vehicle.hazardLights

                if (state.currentTrafficLight == TrafficLightState.RED && vDist in 20f..80f && currentStage == RouteStage.CITY_STREETS) {
                    vSpeed = (vSpeed - 3.5f).coerceAtLeast(0f)
                } else if (!isStopped) {
                    val targetSpeed = when (vehicle.type) {
                        "Jeepney (Sarao Classic)" -> 38f
                        "UV Express (Toyota HiAce)" -> 46f
                        "City Bus (Valenzuela Transport)" -> 42f
                        else -> 52f
                    }
                    if (vSpeed < targetSpeed) vSpeed += 0.8f
                }

                val relSpeed = vSpeed - currentSpeed
                vDist += (relSpeed * (33f / 3600f))

                if (vehicle.type.contains("Jeepney") && (simTickCounter % 200 == 0)) {
                    isStopped = !isStopped
                    hazard = isStopped
                    vLane = 2
                    vSpeed = if (isStopped) 0f else 35f
                }

                if (!isStopped && (simTickCounter % 150 == 0) && (vehicle.id == "tv2" || vehicle.id == "tv3")) {
                    if (vLane == 1) {
                        vLane = 0
                        sigLeft = true
                        sigRight = false
                    } else {
                        vLane = 1
                        sigLeft = false
                        sigRight = true
                    }
                }

                if (vDist < -60f) {
                    vDist = 180f + (Math.random() * 80f).toFloat()
                    isStopped = false
                    hazard = false
                } else if (vDist > 280f) {
                    vDist = -40f - (Math.random() * 40f).toFloat()
                }

                if (vDist in -4f..7f && vLane == playerLane && currentSpeed > 5f) {
                    hasCollided = true
                    collisionWarning = "COLLISION ALERT: Minor scrape with ${vehicle.type}! Reduce speed & maintain safe gap."
                    currentSpeed = (currentSpeed - 6f).coerceAtLeast(0f)
                    damage = (damage + 2.5f).coerceAtMost(100f)
                    triggerHapticPulse(200)
                }

                vehicle.copy(
                    speedKph = vSpeed,
                    lane = vLane,
                    distanceMeters = vDist,
                    isJeepneyStopped = isStopped,
                    signalingLeft = sigLeft,
                    signalingRight = sigRight,
                    hazardLights = hazard
                )
            }

            state.copy(
                speedKph = currentSpeed,
                rpm = rpm,
                gear = gear,
                currentStage = currentStage,
                terminalBayCleared = clearedBay,
                cityStreetDistanceTraveledKm = newCityDist,
                cabinPitch = cPitch,
                cabinRoll = cRoll,
                cabinBounce = cBounce,
                suspensionPitch = cPitch,
                suspensionRoll = cRoll,
                roadBumpOffset = roadBump,
                airBrakePressurePsi = airPressure,
                hangingAirFreshenerSway = sway,
                wiperBladeAngle = bladeAngle,
                windshieldWaterLevel = waterOnWindshield,
                fuelLevelPercent = fuel,
                tireWearPercent = tireWear,
                vehicleDamagePercent = damage,
                collisionDetected = hasCollided,
                collisionMessage = if (hasCollided) collisionWarning else state.collisionMessage,
                seatbeltTensionPercent = dynamicSeatbelt,
                distanceTraveledKm = newDistance,
                routeProgressPercent = newProgress,
                busLateralPosition = newLateral,
                tollGateDistanceMeters = newTollDist,
                aiVehicles = updatedTraffic,
                pedestrians = updatedPedestrians
            )
        }
    }

    private fun updateWorldAndTraffic() {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        _uiState.update { state ->
            val nextLightTimer = state.trafficLightTimerSec - 1
            val (newLight, newTimer) = if (nextLightTimer <= 0) {
                when (state.currentTrafficLight) {
                    TrafficLightState.GREEN -> Pair(TrafficLightState.YELLOW, 4)
                    TrafficLightState.YELLOW -> Pair(TrafficLightState.RED, 12)
                    TrafficLightState.RED -> Pair(TrafficLightState.GREEN, 20)
                }
            } else {
                Pair(state.currentTrafficLight, nextLightTimer)
            }

            var rfidBal = state.rfidBalancePesos
            var tollPaid = state.lastTollPaid
            var isTollOpen = state.tollGateOpen

            if (state.tollGateDistanceMeters < 25f && !state.tollGateOpen && state.currentStage == RouteStage.NLEX_TOLL_ENTRY) {
                val fee = state.currentRoute.tollFee
                if (fee > 0 && rfidBal >= fee) {
                    rfidBal -= fee
                    tollPaid = fee
                    isTollOpen = true
                    triggerHapticPulse(150)
                    soundEngine.playAirBrakeRelease()
                } else if (fee == 0) {
                    isTollOpen = true
                }
            } else if (state.tollGateDistanceMeters > 150f && state.currentStage != RouteStage.EXPRESSWAY_CRUISE) {
                isTollOpen = false
            }

            val gasStationNear = (state.distanceTraveledKm.toInt() % 7 == 0 && state.distanceTraveledKm > 1f)

            if (state.turnSignal != TurnSignalState.OFF) {
                soundEngine.playTurnSignalClick()
            }

            // Fatigue gradual accumulation (ETS2 fatigue bar)
            val newFatigue = (state.fatigueLevelPercent + 0.1f).coerceAtMost(100f)

            state.copy(
                timeOfDayMinutes = if (state.syncRealTime) currentMinutes else (state.timeOfDayMinutes + 1) % 1440,
                currentTrafficLight = newLight,
                trafficLightTimerSec = newTimer,
                tollGateOpen = isTollOpen,
                rfidBalancePesos = rfidBal,
                lastTollPaid = tollPaid,
                gasStationNearby = gasStationNear,
                fatigueLevelPercent = newFatigue
            )
        }
    }

    // Controls
    fun setAccelerator(pressed: Boolean) {
        acceleratorPressed = pressed
        if (pressed) triggerHapticPulse(30)
    }

    fun setBrake(pressed: Boolean) {
        brakePressed = pressed
        if (pressed) triggerHapticPulse(45)
    }

    fun setSteeringAngle(angle: Float) {
        _uiState.update { it.copy(steeringAngle = angle.coerceIn(-1f, 1f)) }
    }

    fun steerLeftTap() {
        triggerHapticPulse(25)
        _uiState.update { it.copy(steeringAngle = (it.steeringAngle - 0.35f).coerceIn(-1f, 1f)) }
    }

    fun steerRightTap() {
        triggerHapticPulse(25)
        _uiState.update { it.copy(steeringAngle = (it.steeringAngle + 0.35f).coerceIn(-1f, 1f)) }
    }

    fun toggleEngine() {
        triggerHapticPulse(80)
        _uiState.update {
            val nextState = !it.isEngineRunning
            if (!nextState) {
                soundEngine.stopEngineAudio()
            }
            it.copy(
                isEngineRunning = nextState,
                rpm = if (nextState) 850 else 0,
                gear = if (nextState) "D1" else "N"
            )
        }
    }

    fun toggleParkingBrake() {
        triggerHapticPulse(70)
        soundEngine.playAirBrakeRelease()
        _uiState.update { it.copy(parkingBrakeOn = !it.parkingBrakeOn) }
    }

    fun toggleHeadlights() {
        _uiState.update { it.copy(headlightsOn = !it.headlightsOn) }
    }

    fun toggleHighBeams() {
        _uiState.update { it.copy(highBeamsOn = !it.highBeamsOn) }
    }

    fun toggleHazardLights() {
        _uiState.update {
            val next = if (it.turnSignal == TurnSignalState.HAZARD) TurnSignalState.OFF else TurnSignalState.HAZARD
            it.copy(hazardLightsOn = next == TurnSignalState.HAZARD, turnSignal = next)
        }
    }

    fun cycleWipers() {
        _uiState.update {
            val nextWiper = when (it.wiperSetting) {
                WiperSpeed.OFF -> WiperSpeed.INTERMITTENT
                WiperSpeed.INTERMITTENT -> WiperSpeed.NORMAL
                WiperSpeed.NORMAL -> WiperSpeed.FAST
                WiperSpeed.FAST -> WiperSpeed.OFF
            }
            it.copy(wiperSetting = nextWiper, wipersOn = nextWiper != WiperSpeed.OFF)
        }
    }

    fun toggleTurnSignal(side: TurnSignalState) {
        _uiState.update {
            val next = if (it.turnSignal == side) TurnSignalState.OFF else side
            val trafficReacted = it.aiVehicles.map { v ->
                if (v.distanceMeters in 8f..35f) v.copy(speedKph = (v.speedKph - 5f).coerceAtLeast(15f)) else v
            }
            it.copy(turnSignal = next, aiVehicles = trafficReacted)
        }
    }

    fun togglePassengerDoors() {
        triggerHapticPulse(40)
        soundEngine.playAirBrakeRelease()
        _uiState.update {
            if (it.speedKph < 5f) {
                it.copy(passengerDoorsOpen = !it.passengerDoorsOpen)
            } else it
        }
    }

    fun triggerHorn() {
        triggerHapticPulse(120)
        soundEngine.playAirHorn()
        _uiState.update {
            val reactedTraffic = it.aiVehicles.map { v ->
                if (v.distanceMeters in 5f..40f) {
                    val newSpeed = (v.speedKph + 8f).coerceAtMost(65f)
                    val newLane = if (v.lane == 1) 0 else v.lane
                    v.copy(speedKph = newSpeed, lane = newLane, signalingLeft = true)
                } else v
            }
            it.copy(hornActive = true, aiVehicles = reactedTraffic)
        }
        viewModelScope.launch {
            delay(450)
            _uiState.update { it.copy(hornActive = false) }
        }
    }

    fun cycleDashboardBacklight() {
        _uiState.update {
            val nextColor = when (it.dashboardBacklightColor) {
                0xFFFFB300 -> 0xFF00E5FF
                0xFF00E5FF -> 0xFFFFFFFF
                0xFFFFFFFF -> 0xFF00E676
                else -> 0xFFFFB300
            }
            it.copy(dashboardBacklightColor = nextColor)
        }
    }

    fun adjustAcTemperature(delta: Int) {
        _uiState.update {
            it.copy(acTempCelsius = (it.acTempCelsius + delta).coerceIn(16, 28))
        }
    }

    fun toggleTv() {
        _uiState.update { it.copy(tvPowerOn = !it.tvPowerOn) }
    }

    fun toggleRadio() {
        _uiState.update { it.copy(radioPowerOn = !it.radioPowerOn) }
    }

    fun nextRadioStation() {
        _uiState.update {
            val nextIdx = (it.activeRadioStationIndex + 1) % it.stations.size
            it.copy(activeRadioStationIndex = nextIdx)
        }
    }

    fun setWeather(weather: WeatherType) {
        _uiState.update { it.copy(weather = weather) }
        if (weather == WeatherType.HEAVY_STORM) {
            soundEngine.playThunderCrack()
        }
    }

    fun setRoute(route: RouteZone) {
        _uiState.update {
            it.copy(
                currentRoute = route,
                distanceTraveledKm = 0f,
                routeProgressPercent = 0f,
                tollGateDistanceMeters = 1200f,
                tollGateOpen = false,
                currentStage = RouteStage.TERMINAL_BAY
            )
        }
    }

    fun setViewMode(viewMode: ViewMode) {
        _uiState.update { it.copy(activeViewMode = viewMode) }
    }

    fun setForceFeedback(strength: Float) {
        _uiState.update { it.copy(forceFeedbackStrength = strength) }
    }

    fun setWheelSensitivity(sensitivity: Float) {
        _uiState.update { it.copy(wheelSensitivity = sensitivity) }
    }

    fun applyLivery(liveryId: String) {
        _uiState.update { it.copy(activeLiveryId = liveryId) }
    }

    fun refuelBus() {
        _uiState.update {
            val cost = ((100f - it.fuelLevelPercent) * 28).toInt()
            it.copy(
                fuelLevelPercent = 100f,
                cashEarnedPesos = (it.cashEarnedPesos - cost).coerceAtLeast(0),
                isFueling = false
            )
        }
        triggerHapticPulse(100)
    }

    fun repairVehicle() {
        _uiState.update {
            it.copy(
                tireWearPercent = 0f,
                engineHealthPercent = 100f,
                vehicleDamagePercent = 0f,
                airBrakePressurePsi = 125f,
                collisionDetected = false,
                collisionMessage = ""
            )
        }
        triggerHapticPulse(150)
    }

    fun restAtTerminal() {
        triggerHapticPulse(80)
        _uiState.update { it.copy(fatigueLevelPercent = 0f) }
    }

    fun boardNewPassengers() {
        if (_uiState.value.speedKph > 2f) return
        val term = _uiState.value.selectedTerminal
        val names = listOf("Juan Dela Cruz", "Maria Clara", "Jose Protacio", "Lea Salonga", "Manny Villar", "Aling Nena")

        val newPassenger = Passenger(
            id = "P-${System.currentTimeMillis() % 1000}",
            name = names.random(),
            origin = "${term.displayName} Bay",
            destination = term.destinationCity,
            farePaid = term.baseFare,
            satisfaction = 98f
        )

        _uiState.update {
            val list = it.passengers.toMutableList()
            if (list.size < it.passengerCapacity) {
                list.add(0, newPassenger)
            }
            it.copy(
                passengers = list,
                cashEarnedPesos = it.cashEarnedPesos + term.baseFare,
                passengersBoardedAtTerminal = true
            )
        }
        triggerHapticPulse(60)
    }

    fun sendChatMessage(text: String, channel: String = "HAM 144.200") {
        if (text.isBlank()) return
        soundEngine.playPttChirp()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val msg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            senderCallsign = "You (Bus #884)",
            channel = channel,
            text = text,
            timestamp = timeFormat.format(Date())
        )
        _chatMessages.update { listOf(msg) + it }
    }

    private fun triggerHapticPulse(milliseconds: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(milliseconds)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
        soundEngine.release()
    }
}
