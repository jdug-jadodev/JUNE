package com.andromeda.gastos.infrastructure.ui.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andromeda.gastos.domain.util.mesDe
import com.andromeda.gastos.domain.util.nombreMes
import com.andromeda.gastos.infrastructure.ui.components.GastoCard
import com.andromeda.gastos.infrastructure.ui.components.PantallaCargando
import com.andromeda.gastos.infrastructure.ui.viewmodel.HomeUiState
import com.andromeda.gastos.infrastructure.ui.viewmodel.HomeViewModel
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDetailScreen(
    vm: HomeViewModel,
    year: Int,
    month: Int,
    onVolver: () -> Unit,
) {
    val estado by vm.state.collectAsStateWithLifecycle()
    val mes = runCatching { YearMonth.of(year, month) }.getOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mes?.let { nombreMes(it) } ?: "Mes") },
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
                    val gastosMes = actual.gastos.filter { mesDe(it.fecha) == mes }
                    if (gastosMes.isEmpty()) {
                        Centrado("No hay gastos en este mes.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(gastosMes, key = { it.id }) { gasto ->
                                GastoCard(
                                    gasto = gasto,
                                    esAdmin = false,
                                    onEditar = {},
                                    onEliminar = {},
                                )
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
