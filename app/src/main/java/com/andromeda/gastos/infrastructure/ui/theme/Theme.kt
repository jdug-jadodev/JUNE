package com.andromeda.gastos.infrastructure.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Azul = Color(0xFF3A5AFF)
private val AzulClaro = Color(0xFF9DB2FF)
private val FondoOscuro = Color(0xFF0E1116)
private val SuperficieOscura = Color(0xFF171B22)

@Composable
fun AndromedaTheme(content: @Composable () -> Unit) {
    val oscuro = isSystemInDarkTheme()

    val esquema = if (oscuro) {
        darkColorScheme(
            primary = AzulClaro,
            onPrimary = Color(0xFF00105C),
            primaryContainer = Color(0xFF1F3478),
            onPrimaryContainer = Color(0xFFDDE1FF),
            background = FondoOscuro,
            surface = SuperficieOscura,
        )
    } else {
        lightColorScheme(
            primary = Azul,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFDDE1FF),
            onPrimaryContainer = Color(0xFF00105C),
        )
    }

    MaterialTheme(colorScheme = esquema, content = content)
}
