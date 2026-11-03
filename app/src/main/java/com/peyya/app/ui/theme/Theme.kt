package com.peyya.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PeyyaColorScheme = lightColorScheme(
    primary = PeyyaBlue,
    onPrimary = PeyyaSand,
    secondary = PeyyaAccent,
    onSecondary = PeyyaInk,
    background = PeyyaSand,
    onBackground = PeyyaInk,
    surface = PeyyaSand,
    onSurface = PeyyaInk,
    primaryContainer = PeyyaBlueDark,
    onPrimaryContainer = PeyyaSand,
)

@Composable
fun PeyyaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PeyyaColorScheme,
        typography = PeyyaTypography,
        content = content,
    )
}
