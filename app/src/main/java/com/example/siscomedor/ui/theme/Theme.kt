package com.example.siscomedor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.siscomedor.R

val TicketGreen = Color(0xFF123D32)
val TicketSecondary = Color(0xFFC8DED6)
val BrandYellow = Color(0xFFEFBD55)

// El archivo verificado es variable: wght=100–900 (400 por defecto), opsz=14–32 (14 por defecto).
// Cada peso solicitado configura el eje real; no se etiqueta una instancia regular como si fuera negrita.
private val Inter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) FontFamily(
    *listOf(400, 500, 600, 700, 800).map { weight ->
        Font(R.font.inter, FontWeight(weight), variationSettings = FontVariation.Settings(
            FontVariation.weight(weight), FontVariation.Setting("opsz", 14f)))
    }.toTypedArray()
) else FontFamily(Font(R.font.inter)) // API 24–25 usa la instancia regular y la síntesis nativa de peso.

private fun type(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) =
    // sp respeta la escala de accesibilidad; el interletraje explícito evita heredar valores de otra familia.
    TextStyle(fontFamily = Inter, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight, letterSpacing = 0.sp)

// Roles de lectura: título de tarjeta 15sp, descripción 12sp y precio 14sp; no equivalen a píxeles web.
private val AppTypography = Typography(
    displayLarge = type(42, 48, FontWeight.Bold), displayMedium = type(36, 42, FontWeight.Bold), displaySmall = type(32, 38, FontWeight.Bold),
    headlineLarge = type(30, 36, FontWeight.Bold), headlineMedium = type(26, 32, FontWeight.Bold), headlineSmall = type(22, 28, FontWeight.Bold),
    titleLarge = type(20, 26, FontWeight.SemiBold), titleMedium = type(15, 21, FontWeight.SemiBold), titleSmall = type(14, 20, FontWeight.SemiBold),
    bodyLarge = type(16, 24), bodyMedium = type(14, 21), bodySmall = type(12, 18),
    labelLarge = type(14, 20, FontWeight.SemiBold), labelMedium = type(12, 16, FontWeight.SemiBold), labelSmall = type(11, 16, FontWeight.Medium)
)
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp), extraLarge = RoundedCornerShape(24.dp)
)

// Los colores se asignan por función Material; las superficies y sus textos tienen parejas claras/oscuras.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1B5D4B), onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F2ED), onPrimaryContainer = Color(0xFF123D32),
    secondary = Color(0xFF725317), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFBD55), onSecondaryContainer = Color(0xFF173F35),
    tertiary = Color(0xFF775719), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF4DB), onTertiaryContainer = Color(0xFF624813),
    background = Color(0xFFF4F7F5), onBackground = Color(0xFF172420),
    surface = Color.White, onSurface = Color(0xFF172420),
    surfaceTint = Color.Transparent,
    surfaceBright = Color.White, surfaceDim = Color(0xFFE5EBE7),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF8FAF9),
    surfaceContainer = Color.White, surfaceContainerHigh = Color(0xFFF1F4F2), surfaceContainerHighest = Color(0xFFE8EEEB),
    inverseSurface = Color(0xFF172820), inverseOnSurface = Color(0xFFE1EBE5), inversePrimary = Color(0xFFA3D8C4),
    surfaceVariant = Color(0xFFF1F4F2), onSurfaceVariant = Color(0xFF52635B),
    outline = Color(0xFF73877E), outlineVariant = Color(0xFFDDE5E1),
    error = Color(0xFF9B352E), errorContainer = Color(0xFFFBE9E7), onErrorContainer = Color(0xFF73241F)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA3D8C4), onPrimary = Color(0xFF103B2C),
    primaryContainer = Color(0xFF1B5D4B), onPrimaryContainer = Color(0xFFD8F5E8),
    secondary = Color(0xFFEFBD55), onSecondary = Color(0xFF392900),
    secondaryContainer = Color(0xFF725317), onSecondaryContainer = Color(0xFFFFE1A1),
    tertiary = Color(0xFFFFDA8E), onTertiary = Color(0xFF392900),
    tertiaryContainer = Color(0xFF483819), onTertiaryContainer = Color(0xFFFFE1A1),
    background = Color(0xFF101D18), onBackground = Color(0xFFE1EBE5),
    surface = Color(0xFF172820), onSurface = Color(0xFFE1EBE5),
    surfaceTint = Color.Transparent,
    surfaceBright = Color(0xFF30463B), surfaceDim = Color(0xFF101D18),
    surfaceContainerLowest = Color(0xFF101D18), surfaceContainerLow = Color(0xFF15251E),
    surfaceContainer = Color(0xFF172820), surfaceContainerHigh = Color(0xFF20362B), surfaceContainerHighest = Color(0xFF2A4235),
    inverseSurface = Color(0xFFE1EBE5), inverseOnSurface = Color(0xFF172820), inversePrimary = Color(0xFF1B5D4B),
    surfaceVariant = Color(0xFF233C30), onSurfaceVariant = Color(0xFFC0D0C6),
    outline = Color(0xFF9BAFA3), outlineVariant = Color(0xFF3C5448),
    error = Color(0xFFFFB4AA), onError = Color(0xFF591B15),
    errorContainer = Color(0xFF73241F), onErrorContainer = Color(0xFFFBE9E7)
)

@Composable
fun SisComeTheme(content: @Composable () -> Unit) {
    // El tema rodea todo el árbol Compose; los componentes heredan tipografía, formas y esquema del sistema.
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography, shapes = AppShapes, content = content)
}
