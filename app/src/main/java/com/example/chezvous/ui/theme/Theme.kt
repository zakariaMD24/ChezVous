package com.example.chezvous.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ChezVousOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDED2),
    onPrimaryContainer = Color(0xFF3B0B00),
    secondary = ChezVousGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F3E1),
    onSecondaryContainer = Color(0xFF062415),
    tertiary = ChezVousGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFECB0),
    onTertiaryContainer = Color(0xFF2D2100),
    background = ChezVousBackground,
    onBackground = ChezVousText,
    surface = ChezVousSurface,
    onSurface = ChezVousText,
    surfaceVariant = ChezVousSurfaceVariant,
    onSurfaceVariant = ChezVousTextMuted,
    outline = ChezVousOutline,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = ChezVousOrangeDark,
    onPrimary = Color(0xFF5F1600),
    primaryContainer = Color(0xFF852200),
    onPrimaryContainer = Color(0xFFFFDED2),
    secondary = ChezVousGreenDark,
    onSecondary = Color(0xFF00391E),
    secondaryContainer = Color(0xFF00522D),
    onSecondaryContainer = Color(0xFFD7F3E1),
    tertiary = ChezVousGoldDark,
    onTertiary = Color(0xFF473600),
    tertiaryContainer = Color(0xFF685000),
    onTertiaryContainer = Color(0xFFFFECB0),
    background = ChezVousDarkBackground,
    onBackground = ChezVousDarkText,
    surface = ChezVousDarkSurface,
    onSurface = ChezVousDarkText,
    surfaceVariant = ChezVousDarkSurfaceVariant,
    onSurfaceVariant = ChezVousDarkTextMuted,
    outline = ChezVousDarkOutline,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val ChezVousShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(ChezVousRadius.sm),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(ChezVousRadius.md),
    large = androidx.compose.foundation.shape.RoundedCornerShape(ChezVousRadius.lg),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(ChezVousRadius.xl)
)

@Composable
fun ChezVousTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ChezVousShapes,
        content = content
    )
}
