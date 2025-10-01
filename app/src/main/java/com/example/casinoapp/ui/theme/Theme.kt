package com.example.casinoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = CasinoGreen,
    onPrimary = Color.White,
    secondary = CasinoBlue,
    onSecondary = Color.White,
    tertiary = CasinoGold,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onSurface = Color(0xFF1F1F1F)
)

private val DarkColors = darkColorScheme(
    primary = CasinoGreen,
    onPrimary = Color.White,
    secondary = CasinoBlue,
    onSecondary = Color.White,
    tertiary = CasinoGold,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFEAEAEA)
)

@Composable
fun CasinoAppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
