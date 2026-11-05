package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusLivery
import com.example.model.BusSimulationState
import com.example.model.ChatMessage
import com.example.model.MultiplayerDriver
import com.example.model.Passenger
import com.example.model.RouteZone
import com.example.model.ViewMode
import com.example.model.WeatherType
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepCockpitBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ManilaGold
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateDim
import com.example.ui.theme.SlateLight
import com.example.ui.theme.WarningYellow

@Composable
fun SimulatorSystemsDrawer(
    state: BusSimulationState,
    liveries: List<BusLivery>,
    multiplayerDrivers: List<MultiplayerDriver>,
    chatMessages: List<ChatMessage>,
    onSelectView: (ViewMode) -> Unit,
    onSelectWeather: (WeatherType) -> Unit,
    onSelectRoute: (RouteZone) -> Unit,
    onSelectLivery: (String) -> Unit,
    onAdjustAc: (Int) -> Unit,
    onToggleRadio: () -> Unit,
    onNextRadio: () -> Unit,
    onToggleTv: () -> Unit,
    onRefuel: () -> Unit,
    onRepair: () -> Unit,
    onBoardPassengers: () -> Unit,
    onSendChat: (String) -> Unit,
    onSetForceFeedback: (Float) -> Unit,
    onSetWheelSensitivity: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("CABIN & CCTV", "PASSENGERS", "GPS ROUTES", "MULTIPLAYER", "WORKSHOP")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            // Tab Header Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeepCockpitBg,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonAmber
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) NeonAmber else SlateDim
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            when (selectedTab) {
                0 -> CabinAndCctvTab(
                    state = state,
                    onSelectView = onSelectView,
                    onAdjustAc = onAdjustAc,
                    onToggleRadio = onToggleRadio,
                    onNextRadio = onNextRadio,
                    onToggleTv = onToggleTv
                )
                1 -> PassengerManagementTab(
                    state = state,
                    onBoardPassengers = onBoardPassengers
                )
                2 -> GpsRoutesAndWeatherTab(
                    state = state,
                    onSelectRoute = onSelectRoute,
                    onSelectWeather = onSelectWeather
                )
                3 -> MultiplayerAndHamChatTab(
                    drivers = multiplayerDrivers,
                    messages = chatMessages,
                    onSendMessage = onSendChat
                )
                4 -> WorkshopAndTuningTab(
                    state = state,
                    liveries = liveries,
                    onSelectLivery = onSelectLivery,
                    onRefuel = onRefuel,
                    onRepair = onRepair,
                    onSetForceFeedback = onSetForceFeedback,
                    onSetWheelSensitivity = onSetWheelSensitivity
                )
            }
        }
    }
}

@Composable
fun CabinAndCctvTab(
    state: BusSimulationState,
    onSelectView: (ViewMode) -> Unit,
    onAdjustAc: (Int) -> Unit,
    onToggleRadio: () -> Unit,
    onNextRadio: () -> Unit,
    onToggleTv: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Multi-Camera & CCTV Selector Row
        Text(
            text = "CAMERA FEEDS & ON-BOARD CCTV SAFETY MONITORS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SlateDim
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ViewMode.values().forEach { mode ->
                val isSelected = state.activeViewMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DeepCockpitBg)
                        .border(1.dp, if (isSelected) NeonCyan else CockpitCardBorder, RoundedCornerShape(6.dp))
                        .clickable { onSelectView(mode) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (mode) {
                            ViewMode.COCKPIT -> "COCKPIT"
                            ViewMode.CHASE_CAM -> "CHASE"
                            ViewMode.CCTV_PASSENGER -> "CCTV 1"
                            ViewMode.CCTV_DOOR -> "CCTV 2"
                            ViewMode.CCTV_REAR -> "CCTV 3"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) NeonCyan else SlateLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AC & Radio/TV Onboard Media System
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Air Conditioner Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DeepCockpitBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AcUnit, contentDescription = "AC", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DENSO DUAL AC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                        }
                        Text("${state.acTempCelsius}°C", fontSize = 12.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onAdjustAc(-1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Cooler", tint = NeonCyan)
                        }
                        Text(
                            text = if (state.acTempCelsius <= 20) "FREEZING DELIGHT" else "COMFORTABLE",
                            fontSize = 9.sp,
                            color = SlateDim
                        )
                        IconButton(onClick = { onAdjustAc(1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Warmer", tint = NeonAmber)
                        }
                    }
                }
            }

            // Radio & Passenger TV Entertainment
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DeepCockpitBg),
                shape = RoundedCornerShape(8.dp)
            ) {
                val currentStation = state.stations[state.activeRadioStationIndex]
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Radio, contentDescription = "Radio", tint = NeonAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentStation.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
                        }
                        Text(currentStation.frequency, fontSize = 11.sp, color = SlateLight, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = currentStation.currentTrack,
                        fontSize = 9.sp,
                        color = SlateDim,
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onNextRadio,
                            colors = ButtonDefaults.buttonColors(containerColor = CockpitCardBorder),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text("TUNE CH", fontSize = 9.sp, color = SlateLight)
                        }
                        Button(
                            onClick = onToggleTv,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.tvPowerOn) EmeraldGreen.copy(alpha = 0.3f) else DeepCockpitBg
                            ),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(Icons.Default.Tv, contentDescription = "TV", modifier = Modifier.size(12.dp), tint = if (state.tvPowerOn) EmeraldGreen else SlateDim)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (state.tvPowerOn) "TV ON" else "TV OFF", fontSize = 9.sp, color = SlateLight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassengerManagementTab(
    state: BusSimulationState,
    onBoardPassengers: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PASSENGERS: ${state.passengers.size} / ${state.passengerCapacity}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Text(
                    text = "Comfort Score: ${state.passengerComfortScore.toInt()}% | Cash Collected: ₱${state.cashEarnedPesos}",
                    fontSize = 10.sp,
                    color = SlateDim
                )
            }
            Button(
                onClick = onBoardPassengers,
                enabled = state.speedKph < 3f && state.passengers.size < state.passengerCapacity,
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Board", tint = DeepCockpitBg, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("BOARD PASSENGERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DeepCockpitBg)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Passenger List preview
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            items(state.passengers) { passenger ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(DeepCockpitBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(passenger.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                        Text("${passenger.origin} ➔ ${passenger.destination}", fontSize = 9.sp, color = SlateDim)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₱${passenger.farePaid}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = EmeraldGreen, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = WarningYellow, modifier = Modifier.size(12.dp))
                        Text("${passenger.satisfaction.toInt()}%", fontSize = 10.sp, color = SlateLight)
                    }
                }
            }
        }
    }
}

@Composable
fun GpsRoutesAndWeatherTab(
    state: BusSimulationState,
    onSelectRoute: (RouteZone) -> Unit,
    onSelectWeather: (WeatherType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("SELECT EXPRESSWAY & HIGHWAY ROUTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateDim)
        Spacer(modifier = Modifier.height(4.dp))

        RouteZone.values().forEach { route ->
            val isCurrent = state.currentRoute == route
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isCurrent) NeonAmber.copy(alpha = 0.2f) else DeepCockpitBg)
                    .border(1.dp, if (isCurrent) NeonAmber else CockpitCardBorder, RoundedCornerShape(6.dp))
                    .clickable { onSelectRoute(route) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(route.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) NeonAmber else SlateLight)
                    Text("Toll: ₱${route.tollFee} | Speed Limit: ${route.speedLimitKph} km/h", fontSize = 9.sp, color = SlateDim)
                }
                if (isCurrent) {
                    Icon(Icons.Default.Check, contentDescription = "Active", tint = NeonAmber, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic Weather Selection
        Text("DYNAMIC WEATHER & TYPHOON ENGINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateDim)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            WeatherType.values().forEach { weather ->
                val isSelected = state.weather == weather
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) WarningYellow.copy(alpha = 0.25f) else DeepCockpitBg)
                        .border(1.dp, if (isSelected) WarningYellow else CockpitCardBorder, RoundedCornerShape(6.dp))
                        .clickable { onSelectWeather(weather) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = weather.displayName.split(" ").first(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) WarningYellow else SlateLight
                    )
                }
            }
        }
    }
}

@Composable
fun MultiplayerAndHamChatTab(
    drivers: List<MultiplayerDriver>,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit
) {
    var chatInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Co-Op Drivers Leaderboard
        Text("ONLINE HIGHWAY LOBBY & CO-OP DRIVERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateDim)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            drivers.take(3).forEach { driver ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DeepCockpitBg),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(EmeraldGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(driver.callsign, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonCyan, maxLines = 1)
                        }
                        Text("${driver.speedKph} km/h • ${driver.route.split(" ").first()}", fontSize = 8.sp, color = SlateDim)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // HAM Radio Dispatch Messages
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("[${msg.channel}] ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonAmber, fontFamily = FontFamily.Monospace)
                    Text("${msg.senderCallsign}: ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                    Text(msg.text, fontSize = 9.sp, color = SlateDim, maxLines = 1)
                }
            }
        }

        // Chat Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Radio Dispatch / HAM Chat...", fontSize = 10.sp, color = SlateDim) },
                modifier = Modifier.weight(1f).height(46.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CockpitCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        onSendMessage(chatInput)
                        chatInput = ""
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = NeonCyan)
            }
        }
    }
}

@Composable
fun WorkshopAndTuningTab(
    state: BusSimulationState,
    liveries: List<BusLivery>,
    onSelectLivery: (String) -> Unit,
    onRefuel: () -> Unit,
    onRepair: () -> Unit,
    onSetForceFeedback: (Float) -> Unit,
    onSetWheelSensitivity: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Vehicle Livery Customizer
        Text("CUSTOM BUS LIVERIES (PHILIPPINE FLEET)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SlateDim)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            liveries.forEach { livery ->
                val isSelected = state.activeLiveryId == livery.id
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectLivery(livery.id) },
                    colors = CardDefaults.cardColors(containerColor = DeepCockpitBg),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) NeonAmber else CockpitCardBorder)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color(livery.primaryColorHex), CircleShape)
                                .border(2.dp, Color(livery.secondaryColorHex), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(livery.companyName.split(" ").first(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SlateLight, maxLines = 1)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Force Feedback & Wheel Tuning
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("FORCE FEEDBACK STRENGTH: ${state.forceFeedbackStrength.toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                Slider(
                    value = state.forceFeedbackStrength,
                    onValueChange = onSetForceFeedback,
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("STEERING SENSITIVITY: ${state.wheelSensitivity.toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonAmber)
                Slider(
                    value = state.wheelSensitivity,
                    onValueChange = onSetWheelSensitivity,
                    valueRange = 20f..100f,
                    colors = SliderDefaults.colors(thumbColor = NeonAmber, activeTrackColor = NeonAmber)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Gas Station & Repair Shop Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRefuel,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(Icons.Default.LocalGasStation, contentDescription = "Gas", tint = DeepCockpitBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PETRON GAS REFUEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepCockpitBg)
            }
            Button(
                onClick = onRepair,
                colors = ButtonDefaults.buttonColors(containerColor = WarningYellow),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = "Repair", tint = DeepCockpitBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PNEUMATIC REPAIR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepCockpitBg)
            }
        }
    }
}
