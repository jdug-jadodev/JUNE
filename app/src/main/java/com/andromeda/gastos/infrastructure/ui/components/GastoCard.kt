package com.andromeda.gastos.infrastructure.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.util.formatearFecha
import com.andromeda.gastos.domain.util.formatearMonto

@Composable
fun GastoCard(
    gasto: Gasto,
    esAdmin: Boolean,
    onEditar: (Gasto) -> Unit,
    onEliminar: (Gasto) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(gasto.producto, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${formatearFecha(gasto.fecha)} · ${gasto.creadoPorNombre}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = formatearMonto(gasto.montoCents),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (esAdmin) {
                IconButton(onClick = { onEditar(gasto) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { onEliminar(gasto) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
