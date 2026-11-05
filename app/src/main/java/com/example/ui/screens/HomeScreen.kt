package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BusSimulationState
import com.example.model.StartingTerminal
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.DeepCockpitBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateDim
import com.example.ui.theme.SlateLight

@Composable
fun HomeScreen(
    state: BusSimulationState,
    availableTerminals: List<StartingTerminal>,
    onSelectTerminal: (StartingTerminal) -> Unit,
    onStartClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // 3D Real Life Philippine Highway Bus Hero Background
        Image(
            painter = painterResource(id = R.drawable.ph_bus_hero_bg_1793880337629),
            contentDescription = "3D Real Life Philippine Bus Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark gradient overlay for contrast and sleek UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DeepCockpitBg.copy(alpha = 0.70f),
                            DeepCockpitBg.copy(alpha = 0.45f),
                            DeepCockpitBg.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top App Bar / Brand Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonAmber)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Logo",
                            tint = DeepCockpitBg,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PH BUS SIMULATOR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonAmber,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "EURO TRUCK SIMULATOR 2 (ETS2) EDITION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Driver Career Status Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepCockpitBg.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CockpitCardBorder))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(EmeraldGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RFID ₱${state.rfidBalancePesos} • ${state.driverRankTier.split(" ").first()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Middle Section: SELECT STARTING TERMINAL CARDS
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SELECT STARTING TERMINAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonAmber,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Terminal Cards Row (Cubao, Pasay, PITX)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    availableTerminals.forEach { terminal ->
                        val isSelected = state.selectedTerminal == terminal
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NeonAmber else CockpitCardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectTerminal(terminal) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) NeonAmber.copy(alpha = 0.20f) else DeepCockpitBg.copy(alpha = 0.85f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = terminal.displayName,
                                        tint = if (isSelected) NeonAmber else SlateDim,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = NeonAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = terminal.displayName.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) NeonAmber else Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = terminal.subtitle,
                                    fontSize = 8.sp,
                                    color = SlateDim,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 11.sp,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .background(DeepCockpitBg, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "FARE ₱${terminal.baseFare}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons: START, ABOUT, SETTINGS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // START BUTTON
                Button(
                    onClick = onStartClick,
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .testTag("start_game_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = DeepCockpitBg,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "START FROM ${state.selectedTerminal.displayName.uppercase()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepCockpitBg,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // ABOUT BUTTON
                OutlinedButton(
                    onClick = onAboutClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("about_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DeepCockpitBg.copy(alpha = 0.85f),
                        contentColor = NeonCyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "About", modifier = Modifier.size(16.dp), tint = NeonCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ABOUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                }

                // SETTINGS BUTTON
                OutlinedButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("settings_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DeepCockpitBg.copy(alpha = 0.85f),
                        contentColor = SlateLight
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CockpitCardBorder)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(16.dp), tint = SlateLight)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                }
            }
        }
    }
}
