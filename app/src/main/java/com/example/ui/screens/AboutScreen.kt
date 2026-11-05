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
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CockpitCardBg
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.DeepCockpitBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SlateDim
import com.example.ui.theme.SlateLight

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepCockpitBg)
            .testTag("about_screen")
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
                        .testTag("about_back_button")
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
                        text = "ABOUT THE SIMULATOR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Philippine 3D Real Life Highway Bus Simulation",
                        fontSize = 10.sp,
                        color = SlateDim
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CockpitCardBorder))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = "Bus", tint = NeonAmber, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("3D REAL-LIFE TRANSIT ENGINE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PH Bus Simulator is engineered to deliver a hyper-realistic, authentic experience of operating provincial and metropolitan transit coaches across major Philippine expressways and national arteries.",
                        fontSize = 11.sp,
                        color = SlateDim,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulation Features Grid
            AboutFeatureCard(
                title = "NLEX & SLEX EXPRESSWAYS",
                description = "Drive through Balintawak, Bocaue, Calamba viaducts, and EDSA Busway with fully automated electronic RFID toll collection and speed enforcement.",
                icon = Icons.Default.Timeline,
                accentColor = NeonCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            AboutFeatureCard(
                title = "3-STATE STOP LIGHTS & DYNAMIC TRAFFIC",
                description = "Realistic city traffic light cycles (Red, Yellow, Green) with proximity sensing and headway monitoring.",
                icon = Icons.Default.Speed,
                accentColor = EmeraldGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            AboutFeatureCard(
                title = "COCKPIT CCTV SAFETY & MULTI-VIEW",
                description = "Switch seamlessly between 3D photorealistic First-Person Cockpit, Exterior 3rd-Person Chase Cam, and internal CCTV surveillance cameras.",
                icon = Icons.Default.Security,
                accentColor = NeonAmber
            )

            Spacer(modifier = Modifier.height(8.dp))

            AboutFeatureCard(
                title = "MONSOON WEATHER & DAY/NIGHT CYCLE",
                description = "Dynamic wet road friction changes during rains, typhoons, and Tagaytay ridge fog synchronized with real device time.",
                icon = Icons.Default.SportsMotorsports,
                accentColor = Color(0xFF64B5F6)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Return to Home button
            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("BACK TO HOME SCREEN", fontWeight = FontWeight.Black, fontSize = 13.sp, color = DeepCockpitBg)
            }
        }
    }
}

@Composable
fun AboutFeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CockpitCardBg),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CockpitCardBorder))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateLight)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, fontSize = 10.sp, color = SlateDim, lineHeight = 14.sp)
            }
        }
    }
}
