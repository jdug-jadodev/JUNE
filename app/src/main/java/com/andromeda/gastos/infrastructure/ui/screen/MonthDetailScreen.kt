package com.andromeda.gastos.infrastructure.ui.screen

import com.andromeda.gastos.infrastructure.ui.components.etiquetaDia
import com.andromeda.gastos.infrastructure.ui.components.TarjetaSuave
import com.andromeda.gastos.infrastructure.ui.components.EstadoVacio
import com.andromeda.gastos.infrastructure.ui.components.EncabezadoGrupo
import com.andromeda.gastos.domain.util.formatearPesos
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andromeda.gastos.domain.util.Ciclo
import com.andromeda.gastos.domain.util.etiquetaCiclo
import com.andromeda.gastos.infrastructure.ui.components.GastoCard
import com.andromeda.gastos.infrastructure.ui.components.PantallaCargando
import com.andromeda.gastos.infrastructure.ui.viewmodel.MonthDetailUiState
import com.andromeda.gastos.infrastructure.ui.viewmodel.MonthDetailViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailScreen(
    vm: MonthDetailViewModel,
    hogarId: String,
    inicio: String,
    fin: String,
    onVolver: () -> Unit,
) {
    val estado by vm.state.collectAsStateWithLifecycle()
    val ciclo = runCatching {
        Ciclo(inicio = LocalDate.parse(inicio), fin = LocalDate.parse(fin))
    }.getOrNull()

    LaunchedEffect(hogarId, inicio, fin) {
        vm.cargar(hogarId, inicio, fin)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ciclo?.let { etiquetaCiclo(it) } ?: "Ciclo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (val actual = estado) {
                MonthDetailUiState.Cargando -> PantallaCargando()
                is MonthDetailUiState.Error -> Centrado(actual.mensaje)
                is MonthDetailUiState.Listo -> {
                    if (actual.gastos.isEmpty()) {
                        EstadoVacio(
                            icono = Icons.AutoMirrored.Outlined.ReceiptLong,
                            titulo = "No hay gastos en este ciclo",
                        )
                    } else {
                        val porDia = actual.gastos.groupBy { it.fecha.take(10) }
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item(key = "total") {
                                TarjetaSuave {
                                    Text(
                                        text = "Total del ciclo",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = formatearPesos(actual.gastos.sumOf { it.montoCents }),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = if (actual.gastos.size == 1) "1 gasto" else "${actual.gastos.size} gastos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            porDia.forEach { (dia, gastos) ->
                                item(key = "dia-$dia") {
                                    EncabezadoGrupo(
                                        texto = etiquetaDia(dia),
                                        extremo = formatearPesos(gastos.sumOf { it.montoCents }),
                                    )
                                }
                                items(gastos, key = { it.id }) { gasto ->
                                    GastoCard(
                                        gasto = gasto,
                                        esAdmin = false,
                                        onEditar = {},
                                        onEliminar = {},
                                        mostrarFecha = false,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Centrado(texto: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
