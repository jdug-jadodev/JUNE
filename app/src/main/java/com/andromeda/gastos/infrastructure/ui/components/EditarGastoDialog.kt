package com.andromeda.gastos.infrastructure.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.util.MontoParser

@Composable
fun EditarGastoDialog(
    gasto: Gasto,
    onConfirmar: (String, Long, String, String) -> Unit,
    onCancelar: () -> Unit,
) {
    var producto by remember { mutableStateOf(gasto.producto) }
    var monto by remember { mutableStateOf((gasto.montoCents / 100).toString()) }
    var fecha by remember { mutableStateOf(gasto.fecha) }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar gasto") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = producto,
                    onValueChange = { producto = it },
                    label = { Text("Producto") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Monto en pesos") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (YYYY-MM-DD)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña de administrador") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val montoCents = MontoParser.aCentavos(monto)
                    if (montoCents != null) {
                        onConfirmar(producto, montoCents, fecha, password)
                    }
                },
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        },
    )
}
