package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BusSimulationState
import com.example.model.TrafficLightState
import com.example.model.ViewMode
import com.example.model.WeatherType
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepCockpitBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateDim
import com.example.ui.theme.SlateLight
import com.example.ui.theme.WarningYellow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SimulatorWorldCanvas(
    state: BusSimulationState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "road_motion")
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state.speedKph > 0.5f) {
                    (900f / (state.speedKph / 25f).coerceAtLeast(0.5f)).toInt().coerceIn(50, 1600)
                } else 100000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "dash_phase"
    )

    val isNight = state.weather == WeatherType.NIGHT_CLEAR || (state.timeOfDayMinutes > 1100 || state.timeOfDayMinutes < 360)

    // ETS2 Dynamic Cabin Suspension Tilt & Bounce offsets
    val cabinOffsetY = (state.cabinPitch * 3f + state.cabinBounce).dp
    val cabinOffsetX = (state.cabinRoll * 1.5f).dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, CockpitCardBorder, RoundedCornerShape(12.dp))
            .testTag("simulator_viewport")
    ) {
        // 1. Base 3D Viewport with Cabin Suspension Inset
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = cabinOffsetX, y = cabinOffsetY)
        ) {
            when (state.activeViewMode) {
                ViewMode.COCKPIT -> {
                    val cockpitDrawable = when (state.currentStage) {
                        com.example.model.RouteStage.TERMINAL_BAY -> {
                            when (state.selectedTerminal) {
                                com.example.model.StartingTerminal.CUBAO -> R.drawable.ph_bus_terminal_cubao_1793881691606
                                com.example.model.StartingTerminal.PASAY -> R.drawable.ph_bus_pasay_terminal_1793882460825
                                com.example.model.StartingTerminal.PITX -> R.drawable.ph_bus_pitx_terminal_1793882442681
                            }
                        }
                        com.example.model.RouteStage.CITY_STREETS -> R.drawable.ph_bus_city_edsa_1793881706425
                        com.example.model.RouteStage.NLEX_TOLL_ENTRY,
                        com.example.model.RouteStage.EXPRESSWAY_CRUISE -> R.drawable.ph_bus_cockpit_3d_1793880354125
                    }
                    Image(
                        painter = painterResource(id = cockpitDrawable),
                        contentDescription = "ETS2 Style Cockpit",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                ViewMode.CHASE_CAM -> {
                    Image(
                        painter = painterResource(id = R.drawable.ph_bus_chase_3d_1793880368271),
                        contentDescription = "3D Chase Cam",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                ViewMode.CCTV_PASSENGER, ViewMode.CCTV_DOOR, ViewMode.CCTV_REAR -> {
                    Image(
                        painter = painterResource(id = R.drawable.ph_bus_cctv_3d_1793880381541),
                        contentDescription = "3D CCTV Surveillance",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // 2. Atmospheric Effects: Volumetric Sunbeams (God Rays), Haze, Wet Asphalt Reflections, Lens Flare
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Volumetric Warm God Rays (ETS2 Golden Sunset Horizon)
            if (state.weather == WeatherType.CLEAR && !isNight) {
                val sunX = width * 0.72f
                val sunY = height * 0.28f
                // Radiating sunbeams
                for (angleDeg in listOf(15f, 35f, 55f, 75f, 105f, 135f)) {
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val endX = sunX + width * cos(rad).toFloat()
                    val endY = sunY + height * sin(rad).toFloat()

                    val beamPath = Path().apply {
                        moveTo(sunX, sunY)
                        lineTo(endX - 35.dp.toPx(), endY)
                        lineTo(endX + 35.dp.toPx(), endY)
                        close()
                    }
                    drawPath(
                        beamPath,
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x35FFD54F), Color(0x00FFB300)),
                            center = Offset(sunX, sunY),
                            radius = width * 0.8f
                        )
                    )
                }

                // Lens Flare Glint
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x88FFF9C4), Color(0x00FFF59D)),
                        center = Offset(sunX, sunY),
                        radius = 28.dp.toPx()
                    ),
                    radius = 28.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
            }

            // Wet Asphalt Mirror Reflections during Rain
            if (state.weather == WeatherType.RAIN || state.weather == WeatherType.HEAVY_STORM) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x000F172A), Color(0x4438BDF8), Color(0x3300E5FF)),
                        startY = height * 0.5f,
                        endY = height
                    ),
                    topLeft = Offset(0f, height * 0.5f),
                    size = Size(width, height * 0.5f)
                )
            }

            // Dynamic Headlight Cones on Road
            if (state.headlightsOn || isNight) {
                val headlightBeamPath = Path().apply {
                    moveTo(width * 0.22f, height)
                    lineTo(width * 0.78f, height)
                    lineTo(width * 0.62f, height * 0.42f)
                    lineTo(width * 0.38f, height * 0.42f)
                    close()
                }
                drawPath(
                    headlightBeamPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x00FFFBEB), Color(0x55FEF08A)),
                        startY = height * 0.42f,
                        endY = height
                    )
                )
            }

            // AI Traffic Vehicles rendered ahead on highway
            state.aiVehicles.forEach { vehicle ->
                if (vehicle.distanceMeters in 5f..240f) {
                    val t = (1f - (vehicle.distanceMeters / 240f)).coerceIn(0.05f, 1f)
                    val vehicleY = height * (0.44f + t * 0.48f)
                    val laneCenterX = when (vehicle.lane) {
                        0 -> width * (0.32f - t * 0.12f) // Left overtaking lane
                        1 -> width * 0.50f              // Middle lane
                        else -> width * (0.68f + t * 0.12f) // Right slow lane
                    }
                    val vWidth = 24.dp.toPx() * (0.3f + t * 0.9f)
                    val vHeight = 16.dp.toPx() * (0.3f + t * 0.9f)

                    // Vehicle Body Box
                    drawRect(
                        color = Color(vehicle.colorHex),
                        topLeft = Offset(laneCenterX - vWidth / 2, vehicleY - vHeight),
                        size = Size(vWidth, vHeight)
                    )
                    // Tail lights
                    val isBlinkOn = (dashPhase.toInt() % 10 < 5)
                    val tailColor = if (vehicle.isEmergency && isBlinkOn) Color.Red else Color(0xFFFF2222)
                    drawCircle(color = tailColor, radius = 2.5.dp.toPx() * t, center = Offset(laneCenterX - vWidth * 0.35f, vehicleY - 3.dp.toPx()))
                    drawCircle(color = tailColor, radius = 2.5.dp.toPx() * t, center = Offset(laneCenterX + vWidth * 0.35f, vehicleY - 3.dp.toPx()))

                    // Turn signal blinking
                    if (vehicle.signalingLeft && isBlinkOn) {
                        drawCircle(color = WarningYellow, radius = 3.dp.toPx() * t, center = Offset(laneCenterX - vWidth * 0.45f, vehicleY - 3.dp.toPx()))
                    }
                }
            }

            // Animated Walking Pedestrians at Terminal Platform
            if (state.currentStage == com.example.model.RouteStage.TERMINAL_BAY) {
                state.pedestrians.filter { !it.hasBoarded }.forEach { ped ->
                    val pedX = width * ped.xPos
                    val pedY = height * ped.yPos
                    val headR = 5.dp.toPx()
                    val legSwing = sin(ped.walkCyclePhase) * 6.dp.toPx()

                    // Head
                    drawCircle(color = Color(0xFFFDBA74), radius = headR, center = Offset(pedX, pedY - 20.dp.toPx()))
                    // Torso
                    drawLine(color = Color(ped.colorHex), start = Offset(pedX, pedY - 15.dp.toPx()), end = Offset(pedX, pedY - 4.dp.toPx()), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                    // Walking Legs (Leg 1 & Leg 2 in opposite phases)
                    drawLine(color = Color(0xFF1E293B), start = Offset(pedX, pedY - 4.dp.toPx()), end = Offset(pedX + legSwing, pedY + 10.dp.toPx()), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(color = Color(0xFF1E293B), start = Offset(pedX, pedY - 4.dp.toPx()), end = Offset(pedX - legSwing, pedY + 10.dp.toPx()), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
                }
            }

            // Dynamic Rain drops on windshield
            if (state.weather == WeatherType.RAIN || state.weather == WeatherType.HEAVY_STORM) {
                val dropCount = if (state.weather == WeatherType.HEAVY_STORM) 45 else 25
                for (r in 0 until dropCount) {
                    val rx = (width * ((r * 37) % 100) / 100f)
                    val ry = (height * ((r * 53 + (dashPhase * 4).toInt()) % 100) / 100f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.65f),
                        start = Offset(rx, ry),
                        end = Offset(rx - 7.dp.toPx(), ry + 22.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Functional Animated Windshield Wipers
            if (state.wipersOn && state.activeViewMode == ViewMode.COCKPIT) {
                val pivotLeft = Offset(width * 0.38f, height * 0.88f)
                val pivotRight = Offset(width * 0.68f, height * 0.88f)
                val bladeLen = height * 0.36f
                val angleRad = Math.toRadians(-state.wiperBladeAngle.toDouble() - 20.0)

                val bladeLeftEnd = Offset(
                    pivotLeft.x + (bladeLen * cos(angleRad)).toFloat(),
                    pivotLeft.y + (bladeLen * sin(angleRad)).toFloat()
                )
                val bladeRightEnd = Offset(
                    pivotRight.x + (bladeLen * cos(angleRad)).toFloat(),
                    pivotRight.y + (bladeLen * sin(angleRad)).toFloat()
                )

                drawLine(color = Color(0xFF1E293B), start = pivotLeft, end = bladeLeftEnd, strokeWidth = 4.dp.toPx())
                drawLine(color = Color(0xFF1E293B), start = pivotRight, end = bladeRightEnd, strokeWidth = 4.dp.toPx())
            }

            // Dynamic Traffic Light
            val lightX = width * 0.88f
            val lightY = 36.dp.toPx()
            drawRect(color = Color(0xDD0F172A), topLeft = Offset(lightX - 11.dp.toPx(), lightY - 24.dp.toPx()), size = Size(22.dp.toPx(), 62.dp.toPx()))
            drawCircle(color = if (state.currentTrafficLight == TrafficLightState.RED) DangerRed else Color(0x333B0B0B), radius = 6.dp.toPx(), center = Offset(lightX, lightY - 14.dp.toPx()))
            drawCircle(color = if (state.currentTrafficLight == TrafficLightState.YELLOW) WarningYellow else Color(0x33382B05), radius = 6.dp.toPx(), center = Offset(lightX, lightY + 4.dp.toPx()))
            drawCircle(color = if (state.currentTrafficLight == TrafficLightState.GREEN) EmeraldGreen else Color(0x33063B18), radius = 6.dp.toPx(), center = Offset(lightX, lightY + 22.dp.toPx()))
        }

        // 3. Hanging Air Freshener in Cockpit (Little Tree Swaying with Physics)
        if (state.activeViewMode == ViewMode.COCKPIT && state.hasAirFreshener) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = (-30).dp, y = 14.dp)
                    .rotate(state.hangingAirFreshenerSway)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // String
                    Box(modifier = Modifier.size(width = 1.dp, height = 22.dp).background(Color(0xFFE2E8F0)))
                    // Pine Tree Scent Tag
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(EmeraldGreen)
                            .border(1.dp, Color.White, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PINE", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = DeepCockpitBg)
                    }
                }
            }
        }

        // 4. Working Live Rear-View Mirror Overlay in Top-Left (ETS2 Mirror Simulation)
        if (state.activeViewMode == ViewMode.COCKPIT && state.showSideMirrors) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(width = 110.dp, height = 48.dp),
                colors = CardDefaults.cardColors(containerColor = DeepCockpitBg.copy(alpha = 0.90f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CockpitCardBorder)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Mirror glass tint
                        drawRect(color = Color(0xFF1E293B))
                        // Horizon in mirror
                        drawLine(color = Color(0xFF475569), start = Offset(0f, size.height * 0.5f), end = Offset(size.width, size.height * 0.5f), strokeWidth = 1.dp.toPx())
                        // Approaching rear vehicles in mirror
                        state.aiVehicles.filter { it.distanceMeters < 0f }.forEach { rv ->
                            val mirrorT = (-rv.distanceMeters / 80f).coerceIn(0.1f, 1f)
                            val carW = 14.dp.toPx() * (1f - mirrorT * 0.5f)
                            drawRect(
                                color = Color(rv.colorHex),
                                topLeft = Offset(size.width * 0.5f - carW / 2, size.height * 0.55f),
                                size = Size(carW, 8.dp.toPx())
                            )
                        }
                    }
                    Text(
                        text = "REAR MIRROR",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(2.dp)
                    )
                }
            }
        }

        // 5. Active Road Hazard / DPWH Alert Toast
        if (state.activeHazards.isNotEmpty()) {
            val hazard = state.activeHazards.first()
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DeepCockpitBg.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WarningYellow))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Hazard", tint = WarningYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${hazard.title.uppercase()} • ${hazard.warningText}",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
