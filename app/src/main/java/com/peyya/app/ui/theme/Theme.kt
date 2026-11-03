package com.peyya.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PhoneColorScheme = lightColorScheme(
    primary = PeyyaBrand,
    onPrimary = PeyyaCanvas,
    background = PeyyaCanvas,
    onBackground = PeyyaInk,
    surface = PeyyaCanvas,
    onSurface = PeyyaInk,
    surfaceVariant = PeyyaSoft,
    onSurfaceVariant = PeyyaInkDim,
    outline = PeyyaLine,
)

private val CarColorScheme = darkColorScheme(
    primary = PeyyaBrand,
    onPrimary = PeyyaCanvas,
    background = CarBg,
    onBackground = CarText,
    surface = CarPanel,
    onSurface = CarText,
    surfaceVariant = CarCard,
    onSurfaceVariant = CarDim,
    outline = CarLine,
)

@Composable
fun PeyyaTheme(
    carMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (carMode) CarColorScheme else PhoneColorScheme,
        typography = if (carMode) CarTypography else PeyyaTypography,
        content = content,
    )
}
