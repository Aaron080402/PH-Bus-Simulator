package com.example.model

import com.example.model.AnimatedPedestrian
import com.example.model.BusJobContract
import com.example.model.ChatMessage
import com.example.model.MultiplayerDriver
import com.example.model.Passenger
import com.example.model.RadioStation
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

data class BusSimulationState(
    // Terminal & Job Selection
    val selectedTerminal: StartingTerminal = StartingTerminal.CUBAO,
    val activeJobContract: BusJobContract = BusJobContract(
        id = "job-01",
        company = "Victory Liner Express",
        originTerminal = "Cubao Terminal Bay 7",
        destination = "Baguio City Terminal",
        rewardPesos = 14500,
        distanceKm = 245f,
        targetTimeMinutes = 240,
        cargoDescription = "45 Provincial Passengers + Express Parcels"
    ),

    // Steering Control Scheme
    val controlMode: SteeringControlMode = SteeringControlMode.STEERING_WHEEL,
    val deviceTiltAngleDeg: Float = 0f, // From Gyroscope/Accelerometer sensor (-30 to +30 deg)

    // Dynamics & ETS2 Physics
    val speedKph: Float = 0f,
    val targetSpeedKph: Float = 0f,
    val steeringAngle: Float = 0f,
    val busLateralPosition: Float = 0.5f,
    val distanceTraveledKm: Float = 0.0f,
    val rpm: Int = 0,
    val gear: String = "N",
    val isEngineRunning: Boolean = false,
    val parkingBrakeOn: Boolean = true,
    val airBrakePressurePsi: Float = 118f,
    val engineBrakeActive: Boolean = false,

    // Starting Location & Stage Progression
    val currentStage: RouteStage = RouteStage.TERMINAL_BAY,
    val passengersBoardedAtTerminal: Boolean = false,
    val terminalBayCleared: Boolean = false,
    val cityStreetDistanceTraveledKm: Float = 0.0f,
    val distanceToNlexTollMeters: Float = 1200f,
    val collisionDetected: Boolean = false,
    val collisionMessage: String = "",

    // ETS2 Route Advisor Telemetry & Fatigue
    val fatigueLevelPercent: Float = 15f, // 0..100% (ETS2 Rest timer)
    val etaMinutes: Int = 45,
    val nextRestStopKm: Float = 65f,

    // ETS2 Dynamic Cabin Suspension Simulation
    val cabinPitch: Float = 0f,
    val cabinRoll: Float = 0f,
    val cabinBounce: Float = 0f,
    val suspensionPitch: Float = 0f,
    val suspensionRoll: Float = 0f,
    val roadBumpOffset: Float = 0f,
    val seatbeltTensionPercent: Float = 35f,
    val forceFeedbackStrength: Float = 75f,
    val wheelSensitivity: Float = 60f,

    // Vehicle Consumables & Wear
    val fuelLevelPercent: Float = 95f,
    val tireWearPercent: Float = 5f,
    val engineHealthPercent: Float = 100f,
    val vehicleDamagePercent: Float = 0f,
    val oilTempC: Int = 82,

    // ETS2 Cabin Interior & Details
    val dashboardBacklightColor: Long = 0xFFFFB300,
    val dashboardBacklightIntensity: Float = 0.85f,
    val hangingAirFreshenerSway: Float = 0f,
    val hasAirFreshener: Boolean = true,
    val wiperSetting: WiperSpeed = WiperSpeed.OFF,
    val wiperBladeAngle: Float = 0f,
    val windshieldWaterLevel: Float = 0f,
    val turnSignal: TurnSignalState = TurnSignalState.OFF,
    val turnSignalClicker: Boolean = false,

    // Cabin Amenities & Electricals
    val headlightsOn: Boolean = false,
    val highBeamsOn: Boolean = false,
    val hazardLightsOn: Boolean = false,
    val interiorLightsOn: Boolean = true,
    val wipersOn: Boolean = false,
    val acTempCelsius: Int = 21,
    val acFanSpeed: Int = 2,
    val passengerDoorsOpen: Boolean = true,
    val hornActive: Boolean = false,

    // Entertainment & HAM Radio
    val tvPowerOn: Boolean = true,
    val radioPowerOn: Boolean = true,
    val radioVolumePercent: Int = 55,
    val hamRadioChannel: String = "HAM 144.200 MHz (Luzon Busway)",
    val hamPttActive: Boolean = false,
    val activeRadioStationIndex: Int = 0,
    val stations: List<RadioStation> = listOf(
        RadioStation("Wish 107.5 FM", "107.5", "OPM Hits & Acoustic Live", "Pop / Acoustic"),
        RadioStation("Love Radio 90.7", "90.7", "Kailangan Pa Ba I-memorize Yan?", "OPM / Sentimental"),
        RadioStation("Monster RX 93.1", "93.1", "Top 40 International Hits", "Top 40"),
        RadioStation("Highway Patrol Net", "144.20", "Live NLEX/SLEX Traffic Advisories", "Dispatch")
    ),

    // Environment & Dynamic 24H Cycle
    val weather: WeatherType = WeatherType.CLEAR,
    val timeOfDayMinutes: Int = 1260, // 21:00
    val syncRealTime: Boolean = false,
    val ambientTempCelsius: Int = 27,
    val sunbeamIntensity: Float = 0.0f,
    val atmosphericHaze: Float = 0.20f,
    val asphaltWetness: Float = 0.0f,

    // Route, Traffic & Hazards
    val currentRoute: RouteZone = RouteZone.CUBAO_TERMINAL_START,
    val routeProgressPercent: Float = 0.0f,
    val nextStopDistanceMeters: Int = 200,
    val currentTrafficLight: TrafficLightState = TrafficLightState.RED,
    val trafficLightTimerSec: Int = 12,
    val distanceToTrafficLightMeters: Float = 45f,
    val tollGateApproaching: Boolean = false,
    val tollGateDistanceMeters: Float = 1200f,
    val tollGateOpen: Boolean = false,
    val tollBoothPhysicalBarrier: Boolean = true,
    val rfidBalancePesos: Int = 1500,
    val lastTollPaid: Int = 0,

    // AI Traffic Simulation
    val aiVehicles: List<TrafficVehicle> = emptyList(),
    val activeHazards: List<RoadHazard> = emptyList(),

    // Animated Walking Pedestrians at Terminal Platform
    val pedestrians: List<AnimatedPedestrian> = emptyList(),
    val crowdNoiseVolume: Float = 0.85f,

    // Gas Station & Refueling
    val gasStationNearby: Boolean = false,
    val isFueling: Boolean = false,

    // Camera & Mirrors
    val activeViewMode: ViewMode = ViewMode.COCKPIT,
    val showSideMirrors: Boolean = true,
    val cctvIncidentDetected: Boolean = false,

    // Passengers & Economy
    val passengers: List<Passenger> = emptyList(),
    val passengerCapacity: Int = 45,
    val cashEarnedPesos: Int = 0,
    val passengerComfortScore: Float = 98f,
    val activeSponsorship: String = "Victory Liner Cubao Express",
    val driverRating: Float = 4.90f,
    val driverRankTier: String = "Professional Bus Pilot",
    val safetyScore: Int = 1000,

    // Selected Livery
    val activeLiveryId: String = "victory_liner"
)
