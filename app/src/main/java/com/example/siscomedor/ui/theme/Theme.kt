package com.example.siscomedor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B5D4B), onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F2ED), onPrimaryContainer = Color(0xFF123D32),
    secondary = Color(0xFF725317), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFBD55), onSecondaryContainer = Color(0xFF173F35),
    tertiary = Color(0xFF7057A2), tertiaryContainer = Color(0xFFEEE9F8), onTertiaryContainer = Color(0xFF49316F),
    background = Color(0xFFF4F7F5), onBackground = Color(0xFF172420),
    surface = Color.White, onSurface = Color(0xFF172420),
    surfaceVariant = Color(0xFFF1F4F2), onSurfaceVariant = Color(0xFF52635B),
    outline = Color(0xFF73877E), outlineVariant = Color(0xFFDDE5E1),
    error = Color(0xFF9B352E), errorContainer = Color(0xFFFBE9E7), onErrorContainer = Color(0xFF73241F)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA3D8C4), onPrimary = Color(0xFF103B2C),
    primaryContainer = Color(0xFF1B5D4B), onPrimaryContainer = Color(0xFFD8F5E8),
    secondary = Color(0xFFEFBD55), onSecondary = Color(0xFF392900),
    secondaryContainer = Color(0xFF725317), onSecondaryContainer = Color(0xFFFFE1A1),
    tertiary = Color(0xFFD2BCF3), tertiaryContainer = Color(0xFF49316F), onTertiaryContainer = Color(0xFFF0E5FF),
    background = Color(0xFF101D18), onBackground = Color(0xFFE1EBE5),
    surface = Color(0xFF172820), onSurface = Color(0xFFE1EBE5),
    surfaceVariant = Color(0xFF233C30), onSurfaceVariant = Color(0xFFC0D0C6),
    outline = Color(0xFF9BAFA3), outlineVariant = Color(0xFF3C5448)
)

@Composable
fun SisComeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}
