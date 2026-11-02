package com.andromeda.gastos.infrastructure.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF0F766E)
private val TealClaro = Color(0xFF2ED3C0)
private val TealContenedor = Color(0xFFD7F0EC)
private val SobreTealContenedor = Color(0xFF063C38)
private val Navy = Color(0xFF0B1F3A)
private val Dorado = Color(0xFFE9B949)
private val FondoClaro = Color(0xFFFBF9F5)
private val FondoOscuro = Color(0xFF0E1116)
private val SuperficieOscura = Color(0xFF171B22)

@Composable
fun AndromedaTheme(content: @Composable () -> Unit) {
    val oscuro = isSystemInDarkTheme()

    val esquema = if (oscuro) {
        darkColorScheme(
            primary = TealClaro,
            onPrimary = Color(0xFF00201C),
            primaryContainer = Color(0xFF0B4F49),
            onPrimaryContainer = Color(0xFFC9F5EF),
            secondary = Dorado,
            onSecondary = Navy,
            background = FondoOscuro,
            surface = SuperficieOscura,
        )
    } else {
        lightColorScheme(
            primary = Teal,
            onPrimary = Color.White,
            primaryContainer = TealContenedor,
            onPrimaryContainer = SobreTealContenedor,
            secondary = Dorado,
            onSecondary = Navy,
            background = FondoClaro,
            surface = Color.White,
        )
    }

    MaterialTheme(colorScheme = esquema, content = content)
}
