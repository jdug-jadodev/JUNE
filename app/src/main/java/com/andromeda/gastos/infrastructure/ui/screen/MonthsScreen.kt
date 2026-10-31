package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.util.formatearMonto
import com.andromeda.gastos.domain.util.mesDe
import com.andromeda.gastos.domain.util.nombreMes
import com.andromeda.gastos.infrastructure.ui.components.PantallaCargando
import com.andromeda.gastos.infrastructure.ui.viewmodel.HomeUiState
import com.andromeda.gastos.infrastructure.ui.viewmodel.HomeViewModel
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthsScreen(
    vm: HomeViewModel,
    onAbrirMes: (Int, Int) -> Unit,
    onVolver: () -> Unit,
) {
    val estado by vm.state.collectAsStateWithLifecycle()
    val mesActual = YearMonth.now()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meses anteriores") },
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
                HomeUiState.Cargando -> PantallaCargando()
                HomeUiState.SinHogar -> Centrado("Configura tu hogar primero.")
                is HomeUiState.Error -> Centrado(actual.mensaje)
                is HomeUiState.Listo -> {
                    val porMes = actual.gastos
                        .mapNotNull { gasto -> mesDe(gasto.fecha)?.let { mes -> mes to gasto } }
                        .filter { (mes, _) -> mes != mesActual }
                        .groupBy({ it.first }, { it.second })
                        .entries
                        .sortedByDescending { it.key }

                    if (porMes.isEmpty()) {
                        Centrado("No hay gastos de meses anteriores.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(porMes, key = { it.key.toString() }) { (mes, gastos) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAbrirMes(mes.year, mes.monthValue) },
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            Icons.Default.Folder,
                                            contentDescription = null,
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = nombreMes(mes),
                                                style = MaterialTheme.typography.titleMedium,
                                            )
                                            Text(
                                                text = "${gastos.size} gastos",
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                        Text(
                                            text = formatearMonto(gastos.sumOf { it.montoCents }),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
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
}

@Composable
private fun Centrado(texto: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(texto)
    }
}
