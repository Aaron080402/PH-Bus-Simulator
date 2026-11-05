package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusSimulationState
import com.example.model.SteeringControlMode
import com.example.model.TurnSignalState
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
import kotlin.math.atan2

@Composable
fun SimulatorControlPanel(
    state: BusSimulationState,
    onAccelPressed: (Boolean) -> Unit,
    onBrakePressed: (Boolean) -> Unit,
    onSteeringChanged: (Float) -> Unit,
    onSteerLeftTap: () -> Unit,
    onSteerRightTap: () -> Unit,
    onToggleEngine: () -> Unit,
    onToggleParkingBrake: () -> Unit,
    onToggleHeadlights: () -> Unit,
    onToggleHighBeams: () -> Unit,
    onToggleHazard: () -> Unit,
    onCycleWipers: () -> Unit,
    onToggleDoors: () -> Unit,
    onHorn: () -> Unit,
    onCycleBacklight: () -> Unit,
    onTurnSignal: (TurnSignalState) -> Unit,
    onCycleControlMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Row 1: Primary Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CockpitToggleButton(
                    label = if (state.isEngineRunning) "IGNITION" else "START",
                    isActive = state.isEngineRunning,
                    activeColor = EmeraldGreen,
                    icon = Icons.Default.PowerSettingsNew,
                    onClick = onToggleEngine
                )

                CockpitToggleButton(
                    label = if (state.parkingBrakeOn) "AIR BRAKE" else "P-BRAKE OFF",
                    isActive = state.parkingBrakeOn,
                    activeColor = DangerRed,
                    icon = Icons.Default.Warning,
                    onClick = onToggleParkingBrake
                )

                CockpitToggleButton(
                    label = "HEADLIGHT",
                    isActive = state.headlightsOn,
                    activeColor = NeonAmber,
                    icon = Icons.Default.Highlight,
                    onClick = onToggleHeadlights
                )

                CockpitToggleButton(
                    label = "WIPER ${state.wiperSetting.label}",
                    isActive = state.wipersOn,
                    activeColor = NeonCyan,
                    icon = Icons.Default.WaterDrop,
                    onClick = onCycleWipers
                )

                CockpitToggleButton(
                    label = if (state.passengerDoorsOpen) "DOOR OPEN" else "DOOR CLS",
                    isActive = state.passengerDoorsOpen,
                    activeColor = WarningYellow,
                    icon = Icons.Default.DoorFront,
                    onClick = onToggleDoors
                )

                CockpitToggleButton(
                    label = "AIR HORN",
                    isActive = state.hornActive,
                    activeColor = DangerRed,
                    icon = Icons.Default.Campaign,
                    onClick = onHorn
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Secondary Indicators & Quick Control Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CockpitMiniButton(
                    label = "◄ SIGNAL L",
                    isActive = state.turnSignal == TurnSignalState.LEFT,
                    activeColor = EmeraldGreen,
                    onClick = { onTurnSignal(TurnSignalState.LEFT) }
                )

                CockpitMiniButton(
                    label = "▲ HAZARD ▲",
                    isActive = state.turnSignal == TurnSignalState.HAZARD,
                    activeColor = DangerRed,
                    onClick = onToggleHazard
                )

                CockpitMiniButton(
                    label = "SIGNAL R ►",
                    isActive = state.turnSignal == TurnSignalState.RIGHT,
                    activeColor = EmeraldGreen,
                    onClick = { onTurnSignal(TurnSignalState.RIGHT) }
                )

                // Quick Switch Steering Scheme Button
                CockpitMiniButton(
                    label = when (state.controlMode) {
                        SteeringControlMode.STEERING_WHEEL -> "MODE: WHEEL"
                        SteeringControlMode.BUTTONS -> "MODE: BUTTONS"
                        SteeringControlMode.GYROSCOPE_TILT -> "MODE: TILT"
                    },
                    isActive = true,
                    activeColor = NeonAmber,
                    onClick = onCycleControlMode
                )

                CockpitMiniButton(
                    label = "DIAL GLOW",
                    isActive = true,
                    activeColor = Color(state.dashboardBacklightColor),
                    onClick = onCycleBacklight
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Dynamic Steering Controls (Wheel OR Buttons OR Gyroscope Spirit Bar) & Pedals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. DYNAMIC STEERING INPUT BASED ON CONTROL MODE
                when (state.controlMode) {
                    SteeringControlMode.STEERING_WHEEL -> {
                        SteeringWheelControl(
                            currentAngle = state.steeringAngle,
                            wheelSensitivity = state.wheelSensitivity,
                            onAngleChanged = onSteeringChanged,
                            modifier = Modifier.size(130.dp)
                        )
                    }
                    SteeringControlMode.BUTTONS -> {
                        // On-Screen Left (<) and Right (>) Directional Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.size(width = 130.dp, height = 120.dp)
                        ) {
                            Button(
                                onClick = onSteerLeftTap,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(95.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepCockpitBg),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, NeonAmber)
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Turn Left", tint = NeonAmber, modifier = Modifier.size(32.dp))
                            }
                            Button(
                                onClick = onSteerRightTap,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(95.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepCockpitBg),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, NeonAmber)
                            ) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Turn Right", tint = NeonAmber, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    SteeringControlMode.GYROSCOPE_TILT -> {
                        // Gyroscope / Tilt Spirit Level Indicator
                        Column(
                            modifier = Modifier
                                .size(width = 130.dp, height = 120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DeepCockpitBg)
                                .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = "Tilt", tint = NeonCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "TILT STEERING",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Dynamic Spirit Level Bar
                            Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
                                val w = size.width
                                val h = size.height
                                drawRect(color = Color(0xFF1E293B), size = size)
                                drawLine(color = SlateDim, start = Offset(w * 0.5f, 0f), end = Offset(w * 0.5f, h), strokeWidth = 2.dp.toPx())
                                // Bubble position from -30 deg to +30 deg
                                val bubbleX = (w * 0.5f) + (state.deviceTiltAngleDeg / 30f) * (w * 0.42f)
                                drawCircle(color = EmeraldGreen, radius = 6.dp.toPx(), center = Offset(bubbleX, h * 0.5f))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.deviceTiltAngleDeg.toInt()}° TILT",
                                fontSize = 8.sp,
                                color = SlateLight,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Center Suspension & Recenter Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "CABIN SUSPENSION",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateDim
                    )
                    Text(
                        text = "DYNAMIC TILT ${(state.cabinPitch * 10).toInt()}°",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onSteeringChanged(0f) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepCockpitBg),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("RECENTER", fontSize = 9.sp, color = SlateLight)
                    }
                }

                // Air Brake & Throttle Pedals
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DrivingPedal(
                        label = "AIR BRAKE",
                        color = DangerRed,
                        height = 105,
                        width = 54,
                        onStateChange = onBrakePressed
                    )

                    DrivingPedal(
                        label = "DIESEL GAS",
                        color = EmeraldGreen,
                        height = 125,
                        width = 50,
                        onStateChange = onAccelPressed
                    )
                }
            }
        }
    }
}

@Composable
fun CockpitMiniButton(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else DeepCockpitBg)
            .border(1.dp, if (isActive) activeColor else CockpitCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onClick() },
                    onDrag = { change, _ -> change.consume() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) activeColor else SlateDim,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun CockpitToggleButton(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(if (isActive) activeColor.copy(alpha = 0.25f) else DeepCockpitBg, label = "btn_bg")
    val borderCol by animateColorAsState(if (isActive) activeColor else CockpitCardBorder, label = "btn_border")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else SlateDim,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) activeColor else SlateDim,
            maxLines = 1
        )
    }
}

@Composable
fun SteeringWheelControl(
    currentAngle: Float,
    wheelSensitivity: Float,
    onAngleChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var rawAngleDeg by remember { mutableFloatStateOf(currentAngle * 90f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = Math.toDegrees(atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())).toFloat()
                        rawAngleDeg = angle
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val touchAngle = Math.toDegrees(atan2((change.position.y - center.y).toDouble(), (change.position.x - center.x).toDouble())).toFloat()
                        val normalized = (touchAngle / 90f).coerceIn(-1f, 1f)
                        rawAngleDeg = touchAngle
                        onAngleChanged(normalized * (wheelSensitivity / 60f))
                    },
                    onDragEnd = {
                        rawAngleDeg = 0f
                        onAngleChanged(0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().rotate(currentAngle * 75f)) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = size.minDimension / 2 - 8.dp.toPx()
            val innerRadius = 20.dp.toPx()

            drawCircle(color = Color(0xFF1E293B), radius = outerRadius, center = center, style = Stroke(width = 16.dp.toPx()))
            drawCircle(color = NeonAmber, radius = outerRadius, center = center, style = Stroke(width = 3.dp.toPx()))

            drawLine(color = Color(0xFF475569), start = center, end = Offset(center.x - outerRadius + 8.dp.toPx(), center.y), strokeWidth = 9.dp.toPx())
            drawLine(color = Color(0xFF475569), start = center, end = Offset(center.x + outerRadius - 8.dp.toPx(), center.y), strokeWidth = 9.dp.toPx())
            drawLine(color = Color(0xFF475569), start = center, end = Offset(center.x, center.y + outerRadius - 8.dp.toPx()), strokeWidth = 9.dp.toPx())

            drawCircle(color = Color(0xFF0F172A), radius = innerRadius, center = center)
            drawCircle(color = NeonCyan, radius = innerRadius, center = center, style = Stroke(width = 2.dp.toPx()))
        }

        Text(
            text = "ETS2",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = NeonAmber,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun DrivingPedal(
    label: String,
    color: Color,
    height: Int,
    width: Int,
    onStateChange: (Boolean) -> Unit
) {
    var isPressed by remember { androidx.compose.runtime.mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(width = width.dp, height = height.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPressed) color else DeepCockpitBg)
            .border(2.dp, if (isPressed) Color.White else color, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isPressed = true
                        onStateChange(true)
                    },
                    onDragEnd = {
                        isPressed = false
                        onStateChange(false)
                    },
                    onDragCancel = {
                        isPressed = false
                        onStateChange(false)
                    },
                    onDrag = { change, _ -> change.consume() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .size(width = (width - 16).dp, height = 3.dp)
                        .background(if (isPressed) Color.White else Color(0xFF334155))
                )
            }
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (isPressed) DeepCockpitBg else color,
                fontFamily = FontFamily.Monospace,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
