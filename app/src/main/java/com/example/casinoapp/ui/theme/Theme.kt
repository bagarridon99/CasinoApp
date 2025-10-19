package com.example.casinoapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

/* ===== Brand palette (morado–dorado con acentos azules) ===== */
private val Purple          = Color(0xFF6953F1)
private val PurpleDark      = Color(0xFF4F3CCB)
private val PurpleContainer = Color(0xFFE6DEFF)
private val OnPurpleContainer = Color(0xFF21174B)

private val BlueAccent          = Color(0xFF5AB8FF)
private val BlueAccentContainer = Color(0xFFD7ECFF)
private val OnBlueContainer     = Color(0xFF00344F)

private val Gold          = Color(0xFFFFC107)
private val GoldContainer = Color(0xFFFFE08A)
private val OnGoldContainer = Color(0xFF3D2F00)

/* ===== LIGHT ===== */
private val LightColors = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnPurpleContainer,

    secondary = BlueAccent,
    onSecondary = Color.Black,
    secondaryContainer = BlueAccentContainer,
    onSecondaryContainer = OnBlueContainer,

    tertiary = Gold,
    onTertiary = Color.Black,
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = OnGoldContainer,

    background = Color(0xFFF7F5FB),
    onBackground = Color(0xFF1C1B20),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1F24),
    surfaceVariant = Color(0xFFEAE6F9),
    onSurfaceVariant = Color(0xFF5B566B),

    outline = Color(0xFF8D86A5),
    outlineVariant = Color(0xFFD9D4EC),

    inverseSurface = Color(0xFF2E2B36),
    inverseOnSurface = Color(0xFFF0ECF9),
    surfaceTint = Purple,

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD4),
    onErrorContainer = Color(0xFF410002)
)

/* ===== DARK ===== */
private val DarkColors = darkColorScheme(
    primary = Color(0xFFBEB4FF),
    onPrimary = Color(0xFF281D70),
    primaryContainer = PurpleDark,
    onPrimaryContainer = Color(0xFFE6DEFF),

    secondary = Color(0xFF9FD4FF),
    onSecondary = Color(0xFF002537),
    secondaryContainer = Color(0xFF1B4B68),
    onSecondaryContainer = Color(0xFFD7ECFF),

    tertiary = Color(0xFFFFD36B),
    onTertiary = Color(0xFF3C2F00),
    tertiaryContainer = Color(0xFF5A4700),
    onTertiaryContainer = Color(0xFFFFE08A),

    background = Color(0xFF121116),
    onBackground = Color(0xFFE6E1EF),

    surface = Color(0xFF1A1820),
    onSurface = Color(0xFFE6E1EF),
    surfaceVariant = Color(0xFF2A2638),
    onSurfaceVariant = Color(0xFFC7C2D6),

    outline = Color(0xFF958FB0),
    outlineVariant = Color(0xFF3E3951),

    inverseSurface = Color(0xFFEAE6F9),
    inverseOnSurface = Color(0xFF282532),
    surfaceTint = Color(0xFFBEB4FF),

    error = Color(0xFFFFB4A9),
    onError = Color(0xFF680003),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD4)
)

/* ===== Shapes (coincide con GlassCard y chips redonditos) ===== */
private val CasinoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/* ===== Theme ===== */
@Composable
fun CasinoAppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (useDarkTheme) DarkColors else LightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CasinoShapes,
        content = content
    )
}
