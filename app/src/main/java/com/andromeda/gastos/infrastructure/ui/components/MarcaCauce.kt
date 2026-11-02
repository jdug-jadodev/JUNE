package com.andromeda.gastos.infrastructure.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.andromeda.gastos.R

@OptIn(ExperimentalTextApi::class)
val Manrope = FontFamily(
    Font(
        R.font.manrope,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.manrope,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

@Composable
fun MarcaCauce(
    modifier: Modifier = Modifier,
    tamanoIcono: Dp = 104.dp,
    mostrarTagline: Boolean = true,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_cauce_symbol),
            contentDescription = "Cauce",
            modifier = Modifier.size(tamanoIcono),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "cauce",
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 46.sp,
            letterSpacing = (-1.5).sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (mostrarTagline) {
            Text(
                text = "FINANZAS DE FAMILIA",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.6.sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
