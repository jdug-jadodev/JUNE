package com.andromeda.gastos.infrastructure.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andromeda.gastos.infrastructure.ui.components.Manrope

private val Teal = Color(0xFF0F766E)
private val TealClaro = Color(0xFF2ED3C0)
private val TealContenedor = Color(0xFFD7F0EC)
private val SobreTealContenedor = Color(0xFF063C38)
private val Navy = Color(0xFF0B1F3A)
private val Dorado = Color(0xFFE9B949)
private val FondoClaro = Color(0xFFFBF9F5)
private val FondoOscuro = Color(0xFF0E1116)
private val SuperficieOscura = Color(0xFF171B22)

private val EsquemaClaro = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealContenedor,
    onPrimaryContainer = SobreTealContenedor,
    secondary = Dorado,
    onSecondary = Navy,
    secondaryContainer = Color(0xFFE4F2EF),
    onSecondaryContainer = SobreTealContenedor,
    tertiary = Navy,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8EDCD),
    onTertiaryContainer = Color(0xFF3B2C00),
    background = FondoClaro,
    onBackground = Color(0xFF16202A),
    surface = FondoClaro,
    onSurface = Color(0xFF16202A),
    surfaceVariant = Color(0xFFEDF1EF),
    onSurfaceVariant = Color(0xFF5A6764),
    surfaceTint = Teal,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF4F3EF),
    surfaceContainerHigh = Color(0xFFF0EFEA),
    surfaceContainerHighest = Color(0xFFEBEBE5),
    outline = Color(0xFF8A9693),
    outlineVariant = Color(0xFFDDE3E0),
)

private val EsquemaOscuro = darkColorScheme(
    primary = TealClaro,
    onPrimary = Color(0xFF00201C),
    primaryContainer = Color(0xFF0B4F49),
    onPrimaryContainer = Color(0xFFC9F5EF),
    secondary = Dorado,
    onSecondary = Navy,
    secondaryContainer = Color(0xFF173A37),
    onSecondaryContainer = Color(0xFFC9F5EF),
    tertiary = Color(0xFFB9C8E0),
    onTertiary = Navy,
    tertiaryContainer = Color(0xFF4A3B12),
    onTertiaryContainer = Color(0xFFF6E3B0),
    background = FondoOscuro,
    onBackground = Color(0xFFE4E8E7),
    surface = FondoOscuro,
    onSurface = Color(0xFFE4E8E7),
    surfaceVariant = Color(0xFF242A31),
    onSurfaceVariant = Color(0xFFAEB9B7),
    surfaceTint = TealClaro,
    surfaceContainerLowest = Color(0xFF0A0D11),
    surfaceContainerLow = SuperficieOscura,
    surfaceContainer = Color(0xFF1A1F26),
    surfaceContainerHigh = Color(0xFF1F252D),
    surfaceContainerHighest = Color(0xFF252C35),
    outline = Color(0xFF7E8A88),
    outlineVariant = Color(0xFF323A43),
)

private val base = Typography()

private val Tipografia = Typography(
    displayLarge = base.displayLarge.manrope(FontWeight.Bold),
    displayMedium = base.displayMedium.manrope(FontWeight.Bold),
    displaySmall = base.displaySmall.manrope(FontWeight.Bold),
    headlineLarge = base.headlineLarge.manrope(FontWeight.Bold),
    headlineMedium = base.headlineMedium.manrope(FontWeight.Bold),
    headlineSmall = base.headlineSmall.manrope(FontWeight.Bold),
    titleLarge = base.titleLarge.manrope(FontWeight.Bold),
    titleMedium = base.titleMedium.manrope(FontWeight.SemiBold),
    titleSmall = base.titleSmall.manrope(FontWeight.SemiBold),
)

private fun TextStyle.manrope(peso: FontWeight) = copy(fontFamily = Manrope, fontWeight = peso)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun AndromedaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content,
    )
}
