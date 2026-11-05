package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusSimulationState
import com.example.ui.components.CockpitTelemetryPanel
import com.example.ui.components.SimulatorControlPanel
import com.example.ui.components.SimulatorSystemsDrawer
import com.example.ui.components.SimulatorWorldCanvas
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.DeepCockpitBg
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateDim
import com.example.viewmodel.BusSimulatorViewModel

@Composable
fun SimulatorDriveScreen(
    viewModel: BusSimulatorViewModel,
    state: BusSimulationState,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackToHome() }
    val scrollState = rememberScrollState()

    val multiplayerDrivers = viewModel.multiplayerDrivers.value
    val chatMessages = viewModel.chatMessages.value

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpitBg)
            .testTag("simulator_drive_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Top Navigation Bar to go back to Home
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CockpitCardBg)
                        .border(1.dp, CockpitCardBorder, RoundedCornerShape(8.dp))
                        .testTag("back_to_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = NeonAmber
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.currentStage.title.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${state.currentStage.description} • LIMIT ${state.currentStage.speedLimitKph} KM/H",
                        fontSize = 9.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                    shape = RoundedCornerShape(6.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CockpitCardBorder))
                ) {
                    Text(
                        text = "₱${state.cashEarnedPesos}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            // 1. 3D Real Life Simulator World Viewport
            SimulatorWorldCanvas(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .testTag("simulator_viewport")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Cockpit Telemetry Dashboard
            CockpitTelemetryPanel(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cockpit_telemetry")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Driver Controls (Steering wheel, Buttons, Tilt, Throttle/Brake, Lights, Horn, Doors, Wipers, Blinkers)
            SimulatorControlPanel(
                state = state,
                onAccelPressed = { viewModel.setAccelerator(it) },
                onBrakePressed = { viewModel.setBrake(it) },
                onSteeringChanged = { viewModel.setSteeringAngle(it) },
                onSteerLeftTap = { viewModel.steerLeftTap() },
                onSteerRightTap = { viewModel.steerRightTap() },
                onToggleEngine = { viewModel.toggleEngine() },
                onToggleParkingBrake = { viewModel.toggleParkingBrake() },
                onToggleHeadlights = { viewModel.toggleHeadlights() },
                onToggleHighBeams = { viewModel.toggleHighBeams() },
                onToggleHazard = { viewModel.toggleHazardLights() },
                onCycleWipers = { viewModel.cycleWipers() },
                onToggleDoors = { viewModel.togglePassengerDoors() },
                onHorn = { viewModel.triggerHorn() },
                onCycleBacklight = { viewModel.cycleDashboardBacklight() },
                onTurnSignal = { viewModel.toggleTurnSignal(it) },
                onCycleControlMode = {
                    val nextMode = when (state.controlMode) {
                        com.example.model.SteeringControlMode.STEERING_WHEEL -> com.example.model.SteeringControlMode.BUTTONS
                        com.example.model.SteeringControlMode.BUTTONS -> com.example.model.SteeringControlMode.GYROSCOPE_TILT
                        com.example.model.SteeringControlMode.GYROSCOPE_TILT -> com.example.model.SteeringControlMode.STEERING_WHEEL
                    }
                    viewModel.setControlMode(nextMode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_controls")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Cabin Systems Drawer (CCTV, Passenger management, Routes, HAM Radio chat, Workshop)
            SimulatorSystemsDrawer(
                state = state,
                liveries = viewModel.availableLiveries,
                multiplayerDrivers = multiplayerDrivers,
                chatMessages = chatMessages,
                onSelectView = { viewModel.setViewMode(it) },
                onSelectWeather = { viewModel.setWeather(it) },
                onSelectRoute = { viewModel.setRoute(it) },
                onSelectLivery = { viewModel.applyLivery(it) },
                onAdjustAc = { viewModel.adjustAcTemperature(it) },
                onToggleRadio = { viewModel.toggleRadio() },
                onNextRadio = { viewModel.nextRadioStation() },
                onToggleTv = { viewModel.toggleTv() },
                onRefuel = { viewModel.refuelBus() },
                onRepair = { viewModel.repairVehicle() },
                onBoardPassengers = { viewModel.boardNewPassengers() },
                onSendChat = { viewModel.sendChatMessage(it) },
                onSetForceFeedback = { viewModel.setForceFeedback(it) },
                onSetWheelSensitivity = { viewModel.setWheelSensitivity(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("systems_drawer")
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
