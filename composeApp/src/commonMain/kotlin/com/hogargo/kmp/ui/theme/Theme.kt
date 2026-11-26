package com.hogargo.kmp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hogargo.kmp.household.ui.HogarGoColors

private val LightScheme = lightColorScheme(
    primary = Color(0xFF8C4F28),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBC9),
    onPrimaryContainer = Color(0xFF6F3812),
    secondary = Color(0xFF765847),
    secondaryContainer = Color(0xFFFFDBC9),
    onSecondaryContainer = Color(0xFF5C4131),
    tertiary = Color(0xFF626032),
    tertiaryContainer = Color(0xFFE9E5AB),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFFFB68C),
    onPrimary = Color(0xFF532200),
    primaryContainer = Color(0xFF5A3319),
    onPrimaryContainer = Color(0xFFFFDBC9),
    secondary = Color(0xFFE6BEAB),
    onSecondary = Color(0xFF432B1C),
    secondaryContainer = Color(0xFF5A3319),
    onSecondaryContainer = Color(0xFFFFDBC9),
    tertiary = Color(0xFFCCC991),
    tertiaryContainer = Color(0xFF4A481D),
    background = Color(0xFF19120E),
    onBackground = Color(0xFFF1DFD6),
    surface = Color(0xFF19120E),
    onSurface = Color(0xFFF1DFD6),
    surfaceVariant = Color(0xFF52443C),
    onSurfaceVariant = Color(0xFFD7C2B8),
    outline = Color(0xFFA08D84),
    outlineVariant = Color(0xFF52443C),
    surfaceContainerLowest = Color(0xFF140C09),
    surfaceContainerLow = Color(0xFF221A15),
    surfaceContainer = Color(0xFF271E19),
    surfaceContainerHigh = Color(0xFF322823),
    surfaceContainerHighest = Color(0xFF3D322D),
)

/** Tema de la app: sigue el modo claro u oscuro del sistema en Android e iOS. */
@Composable
fun HogarGoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    HogarGoColors.isDark = darkTheme
    MaterialTheme(colorScheme = if (darkTheme) DarkScheme else LightScheme) {
        // Surface fija el color de texto por defecto segun el tema.
        Surface(
            color = HogarGoColors.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            content = content,
        )
    }
}
