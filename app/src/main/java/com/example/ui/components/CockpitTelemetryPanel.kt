package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusSimulationState
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
fun CockpitTelemetryPanel(
    state: BusSimulationState,
    modifier: Modifier = Modifier
) {
    val backlightColor = Color(state.dashboardBacklightColor)

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
                .padding(10.dp)
        ) {
            // Top Row: ETS2 Instrument Cluster with Realistic Needles & Sleek Center GPS Screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Realistic Speedometer with Physical Needle
                Ets2NeedleGauge(
                    currentVal = state.speedKph,
                    maxVal = 140f,
                    unit = "KM/H",
                    label = "SPEED",
                    needleColor = DangerRed,
                    backlightColor = backlightColor,
                    modifier = Modifier.size(95.dp)
                )

                // 2. Central ETS2 Minimalist GPS Navigation Screen
                Ets2GpsDisplay(
                    state = state,
                    modifier = Modifier
                        .weight(1f)
                        .height(98.dp)
                        .padding(horizontal = 6.dp)
                )

                // 3. Realistic Tachometer RPM with Physical Needle
                Ets2NeedleGauge(
                    currentVal = state.rpm.toFloat(),
                    maxVal = 2600f,
                    unit = "RPM",
                    label = "DIESEL",
                    needleColor = NeonAmber,
                    backlightColor = backlightColor,
                    modifier = Modifier.size(95.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vital Truck/Bus Metrics: Air Brake Pressure, Diesel Level, Tire Wear, Engine Health
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricProgressItem(
                    label = "AIR BRAKE",
                    value = "${state.airBrakePressurePsi.toInt()} PSI",
                    progress = (state.airBrakePressurePsi / 125f).coerceIn(0f, 1f),
                    accentColor = if (state.airBrakePressurePsi < 85f) DangerRed else NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                MetricProgressItem(
                    label = "DIESEL",
                    value = "${state.fuelLevelPercent.toInt()}%",
                    progress = state.fuelLevelPercent / 100f,
                    accentColor = if (state.fuelLevelPercent < 20f) DangerRed else EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                MetricProgressItem(
                    label = "TIRE GRIP",
                    value = "${(100f - state.tireWearPercent).toInt()}%",
                    progress = (100f - state.tireWearPercent) / 100f,
                    accentColor = if (state.tireWearPercent > 50f) WarningYellow else NeonAmber,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                MetricProgressItem(
                    label = "CABIN TENSION",
                    value = "${state.seatbeltTensionPercent.toInt()}%",
                    progress = state.seatbeltTensionPercent / 100f,
                    accentColor = backlightColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun Ets2GpsDisplay(
    state: BusSimulationState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Next Turn & Highway Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "GPS Nav",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${state.selectedTerminal.displayName.uppercase()} ➔ ${state.selectedTerminal.destinationCity.uppercase()}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }

                // Speed Limit Badge (Circular ETS2 style)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, DangerRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${state.currentStage.speedLimitKph}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            // Center: Visual mini GPS road trajectory
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
            ) {
                // Background highway path line
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(size.width * 0.1f, size.height * 0.8f),
                    end = Offset(size.width * 0.9f, size.height * 0.2f),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Active route progression line
                val progressX = size.width * (0.1f + 0.8f * (state.routeProgressPercent / 100f))
                val progressY = size.height * (0.8f - 0.6f * (state.routeProgressPercent / 100f))
                drawLine(
                    color = EmeraldGreen,
                    start = Offset(size.width * 0.1f, size.height * 0.8f),
                    end = Offset(progressX, progressY),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Bus Marker on Route
                drawCircle(color = NeonAmber, radius = 5.dp.toPx(), center = Offset(progressX, progressY))
            }

            // Footer: Gear, ETA & Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GEAR: ${state.gear}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonAmber,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "ETA: ${state.etaMinutes} MIN",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateLight
                )
                Text(
                    text = "${state.distanceTraveledKm.format(1)} KM",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun Ets2NeedleGauge(
    currentVal: Float,
    maxVal: Float,
    unit: String,
    label: String,
    needleColor: Color,
    backlightColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 5.dp.toPx()
            val diameter = size.minDimension - strokeWidth * 2
            val radius = diameter / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Outer gauge ring with ETS2 ambient backlight
            drawCircle(
                color = DeepCockpitBg,
                radius = radius,
                center = center
            )
            drawCircle(
                color = backlightColor.copy(alpha = 0.35f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Tick marks around dial (from 140 deg to 400 deg)
            val startAngle = 140f
            val sweepTotal = 260f
            val numTicks = 10
            for (i in 0..numTicks) {
                val tickAngle = Math.toRadians((startAngle + sweepTotal * (i.toFloat() / numTicks)).toDouble())
                val outerP = Offset(center.x + (radius - 2.dp.toPx()) * cos(tickAngle).toFloat(), center.y + (radius - 2.dp.toPx()) * sin(tickAngle).toFloat())
                val innerP = Offset(center.x + (radius - 8.dp.toPx()) * cos(tickAngle).toFloat(), center.y + (radius - 8.dp.toPx()) * sin(tickAngle).toFloat())
                drawLine(color = backlightColor, start = innerP, end = outerP, strokeWidth = 1.5.dp.toPx())
            }

            // Physical Rotating Gauge Needle
            val norm = (currentVal / maxVal).coerceIn(0f, 1f)
            val needleAngleRad = Math.toRadians((startAngle + sweepTotal * norm).toDouble())
            val needleTip = Offset(
                center.x + (radius - 10.dp.toPx()) * cos(needleAngleRad).toFloat(),
                center.y + (radius - 10.dp.toPx()) * sin(needleAngleRad).toFloat()
            )

            drawLine(
                color = needleColor,
                start = center,
                end = needleTip,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = Color(0xFF1E293B), radius = 6.dp.toPx(), center = center)
            drawCircle(color = needleColor, radius = 3.dp.toPx(), center = center)
        }

        // Numerical readout in dial center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "${currentVal.toInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = unit,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = SlateDim
            )
        }
    }
}

@Composable
fun MetricProgressItem(
    label: String,
    value: String,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DeepCockpitBg, RoundedCornerShape(6.dp))
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateDim
            )
            Text(
                text = value,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = accentColor,
            trackColor = CockpitCardBorder
        )
    }
}

fun Float.format(digits: Int): String = "%.${digits}f".format(this)
