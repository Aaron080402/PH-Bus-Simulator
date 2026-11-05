package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusSimulationState
import com.example.model.SteeringControlMode
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.DeepCockpitBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateDim
import com.example.ui.theme.SlateLight

@Composable
fun SettingsScreen(
    state: BusSimulationState,
    onSetControlMode: (SteeringControlMode) -> Unit,
    onSetForceFeedback: (Float) -> Unit,
    onSetWheelSensitivity: (Float) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }
    val scrollState = rememberScrollState()

    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var soundEffectsVolume by remember { mutableFloatStateOf(85f) }
    var radioVolume by remember { mutableFloatStateOf(60f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpitBg)
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header with Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CockpitCardBg)
                        .border(1.dp, CockpitCardBorder, RoundedCornerShape(8.dp))
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SIMULATION SETTINGS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Steering Scheme, Gyroscope Tilt, and ETS2 Physics",
                        fontSize = 10.sp,
                        color = SlateDim
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. STEERING CONTROL SCHEME SELECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(NeonAmber))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gamepad, contentDescription = "Controls", tint = NeonAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("STEERING CONTROL MODE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SteeringControlMode.values().forEach { mode ->
                            val isSelected = state.controlMode == mode
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) NeonAmber else CockpitCardBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSetControlMode(mode) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) NeonAmber.copy(alpha = 0.22f) else DeepCockpitBg
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            SteeringControlMode.STEERING_WHEEL -> Icons.Default.Gamepad
                                            SteeringControlMode.BUTTONS -> Icons.Default.DisplaySettings
                                            SteeringControlMode.GYROSCOPE_TILT -> Icons.Default.ScreenRotation
                                        },
                                        contentDescription = mode.label,
                                        tint = if (isSelected) NeonAmber else SlateDim,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = mode.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonAmber else Color.White,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mode.description,
                                        fontSize = 8.sp,
                                        color = SlateDim,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Wheel Force Feedback & Tilt Sensitivity
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CockpitCardBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "FORCE FEEDBACK & STEERING SENSITIVITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "RESISTANCE STRENGTH: ${state.forceFeedbackStrength.toInt()}%",
                        fontSize = 10.sp,
                        color = SlateDim
                    )
                    Slider(
                        value = state.forceFeedbackStrength,
                        onValueChange = onSetForceFeedback,
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "STEERING ANGLE SENSITIVITY: ${state.wheelSensitivity.toInt()}%",
                        fontSize = 10.sp,
                        color = SlateDim
                    )
                    Slider(
                        value = state.wheelSensitivity,
                        onValueChange = onSetWheelSensitivity,
                        valueRange = 20f..100f,
                        colors = SliderDefaults.colors(thumbColor = NeonAmber, activeTrackColor = NeonAmber)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. VR Haptics & ETS2 Cabin Suspension
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CockpitCardBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vibration & Seatbelt Pneumatic Tension", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SlateLight)
                            Text("Haptic rumble on potholes, rumble strips, and air brake release", fontSize = 9.sp, color = SlateDim)
                        }
                        Switch(
                            checked = hapticFeedbackEnabled,
                            onCheckedChange = { hapticFeedbackEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldGreen, checkedTrackColor = EmeraldGreen.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("SAVE & RETURN TO HOME", fontWeight = FontWeight.Black, fontSize = 13.sp, color = DeepCockpitBg)
            }
        }
    }
}
