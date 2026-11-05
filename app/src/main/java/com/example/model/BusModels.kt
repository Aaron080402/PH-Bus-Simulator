package com.example.model

enum class StartingTerminal(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val exitHighway: String,
    val defaultRoute: RouteZone,
    val destinationCity: String,
    val baseFare: Int
) {
    CUBAO(
        id = "cubao",
        displayName = "Cubao Terminal",
        subtitle = "Northbound routes via EDSA & NLEX",
        exitHighway = "EDSA North ➔ NLEX Balintawak",
        defaultRoute = RouteZone.CUBAO_TERMINAL_START,
        destinationCity = "Baguio City",
        baseFare = 480
    ),
    PASAY(
        id = "pasay",
        displayName = "Pasay Terminal",
        subtitle = "Southbound routes via Magallanes & SLEX",
        exitHighway = "Taft Ave ➔ SLEX Alabang / Calamba",
        defaultRoute = RouteZone.SLEX_ALABANG,
        destinationCity = "Batangas City / Calamba",
        baseFare = 320
    ),
    PITX(
        id = "pitx",
        displayName = "PITX Exchange",
        subtitle = "Parañaque Integrated Terminal Exchange (Cavite / Coastal)",
        exitHighway = "Macapagal Blvd ➔ CAVITEX / SLEX",
        defaultRoute = RouteZone.EDSA_CENTRAL,
        destinationCity = "Tagaytay / Nasugbu",
        baseFare = 290
    )
}

enum class SteeringControlMode(val label: String, val description: String) {
    STEERING_WHEEL("Virtual Steering Wheel", "Analog touch drag wheel with force feedback"),
    BUTTONS("On-Screen Buttons", "Responsive Left (<) and Right (>) tap buttons"),
    GYROSCOPE_TILT("Gyroscope / Tilt Steering", "Tilt device phone left/right (-30° to +30°)")
}

data class BusJobContract(
    val id: String,
    val company: String,
    val originTerminal: String,
    val destination: String,
    val rewardPesos: Int,
    val distanceKm: Float,
    val targetTimeMinutes: Int,
    val cargoDescription: String
)

data class AnimatedPedestrian(
    val id: String,
    val name: String,
    var xPos: Float, // 0..1 normalized across platform
    var yPos: Float,
    var targetX: Float,
    var speed: Float,
    var walkCyclePhase: Float = 0f,
    var hasBoarded: Boolean = false,
    var colorHex: Long = 0xFF38BDF8
)

enum class WeatherType(val displayName: String, val frictionMultiplier: Float, val visibilityFactor: Float) {
    CLEAR("Sunny Day (God Rays)", 1.0f, 1.0f),
    RAIN("Monsoon Wet Asphalt", 0.68f, 0.75f),
    HEAVY_STORM("Typhoon Signal 2 Puddles", 0.52f, 0.45f),
    FOG("Tagaytay Ridge Haze", 0.85f, 0.35f),
    NIGHT_CLEAR("Starry Night Overpass", 0.95f, 0.80f)
}

enum class ViewMode(val label: String) {
    COCKPIT("ETS2 Cabin Cockpit"),
    CHASE_CAM("Exterior 3rd Person"),
    CCTV_PASSENGER("CCTV 1: Passenger Cabin"),
    CCTV_DOOR("CCTV 2: Pneumatic Door"),
    CCTV_REAR("CCTV 3: Rear Highway Traffic")
}

enum class RouteStage(val title: String, val description: String, val speedLimitKph: Int) {
    TERMINAL_BAY("Terminal Boarding Bay", "Parked in terminal bay. Start engine and board passengers.", 15),
    CITY_STREETS("City Streets & Avenues", "Navigate traffic lights, jeepneys, and urban traffic toward expressway.", 50),
    NLEX_TOLL_ENTRY("Expressway Toll Plaza", "Queue in RFID lane, stop for barrier clearance, then enter expressway.", 80),
    EXPRESSWAY_CRUISE("Expressway Cruise", "High-speed cruise across provincial highway.", 100)
}

enum class RouteZone(val title: String, val speedLimitKph: Int, val highwayName: String, val tollFee: Int) {
    CUBAO_TERMINAL_START("Cubao Terminal to NLEX via EDSA", 50, "Cubao & EDSA City Streets", 128),
    EDSA_CENTRAL("EDSA Cubao - Guadalupe Busway", 60, "City Transit", 0),
    NLEX_BALINTAWAK("NLEX Balintawak to Bocaue Tollway", 100, "NLEX Expressway", 128),
    SLEX_ALABANG("Pasay / SLEX Alabang to Calamba", 100, "SLEX Expressway", 164),
    TAGAYTAY_RIDGE("PITX to Tagaytay Scenic Ridge", 50, "Rural Mountain Highway", 0)
}

enum class TrafficLightState {
    GREEN, YELLOW, RED
}

enum class WiperSpeed(val label: String, val intervalMs: Long) {
    OFF("OFF", 0L),
    INTERMITTENT("INT", 2200L),
    NORMAL("LO", 1200L),
    FAST("HI", 600L)
}

enum class TurnSignalState {
    OFF, LEFT, RIGHT, HAZARD
}

data class TrafficVehicle(
    val id: String,
    val type: String,
    var lane: Int,
    var distanceMeters: Float,
    var speedKph: Float,
    val colorHex: Long,
    val isEmergency: Boolean = false,
    var isJeepneyStopped: Boolean = false,
    var signalingLeft: Boolean = false,
    var signalingRight: Boolean = false,
    var hazardLights: Boolean = false,
    var reactionCooldownSec: Int = 0
)

data class RoadHazard(
    val title: String,
    val distanceMeters: Float,
    val type: String,
    val warningText: String
)

data class BusLivery(
    val id: String,
    val name: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val companyName: String,
    val description: String,
    val unlocked: Boolean = true
)

data class Passenger(
    val id: String,
    val name: String,
    val origin: String,
    val destination: String,
    val farePaid: Int,
    val satisfaction: Float,
    val complaint: String? = null
)

data class RadioStation(
    val name: String,
    val frequency: String,
    val currentTrack: String,
    val genre: String
)

data class MultiplayerDriver(
    val id: String,
    val callsign: String,
    val busModel: String,
    val route: String,
    val speedKph: Int,
    val distanceKm: Float,
    val safetyScore: Int,
    val isCoOpPartner: Boolean = false
)

data class ChatMessage(
    val id: String,
    val senderCallsign: String,
    val channel: String,
    val text: String,
    val timestamp: String,
    val isSystem: Boolean = false
)
