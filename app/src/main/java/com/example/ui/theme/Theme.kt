package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BusSimDarkColorScheme = darkColorScheme(
    primary = NeonAmber,
    onPrimary = DeepCockpitBg,
    primaryContainer = CockpitCardBorder,
    onPrimaryContainer = NeonAmber,
    secondary = NeonCyan,
    onSecondary = DeepCockpitBg,
    secondaryContainer = CockpitCardBg,
    onSecondaryContainer = NeonCyan,
    tertiary = EmeraldGreen,
    onTertiary = DeepCockpitBg,
    background = DeepCockpitBg,
    onBackground = SlateLight,
    surface = CockpitCardBg,
    onSurface = SlateLight,
    surfaceVariant = DashboardSurface,
    onSurfaceVariant = SlateDim,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Keep consistent immersive dark simulator aesthetic
    MaterialTheme(
        colorScheme = BusSimDarkColorScheme,
        typography = Typography,
        content = content
    )
}
