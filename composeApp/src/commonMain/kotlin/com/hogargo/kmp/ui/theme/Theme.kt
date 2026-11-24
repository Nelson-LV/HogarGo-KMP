package com.hogargo.kmp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HogarGoColors = lightColorScheme(
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

@Composable
fun HogarGoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = HogarGoColors, content = content)
}
