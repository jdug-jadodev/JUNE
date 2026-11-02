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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.util.Ciclo
import com.andromeda.gastos.domain.util.cicloActual
import com.andromeda.gastos.domain.util.etiquetaCiclo
import com.andromeda.gastos.domain.util.formatearMonto
import com.andromeda.gastos.infrastructure.ui.components.PantallaCargando
import com.andromeda.gastos.infrastructure.ui.viewmodel.MonthsUiState
import com.andromeda.gastos.infrastructure.ui.viewmodel.MonthsViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthsScreen(
    vm: MonthsViewModel,
    hogarId: String,
    onAbrirCiclo: (String, String) -> Unit,
    onVolver: () -> Unit,
) {
    val estado by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(hogarId) {
        vm.cargar(hogarId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ciclos anteriores") },
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
                MonthsUiState.Cargando -> PantallaCargando()
                is MonthsUiState.Error -> Centrado(actual.mensaje)
                is MonthsUiState.Listo -> {
                    val cicloEnCurso = cicloActual(actual.cutoffDay)
                    val anteriores = actual.ciclos
                        .mapNotNull { resumen -> aCiclo(resumen)?.let { it to resumen } }
                        .filter { (ciclo, _) -> ciclo.inicio.isBefore(cicloEnCurso.inicio) }
                        .sortedByDescending { (ciclo, _) -> ciclo.inicio }

                    if (anteriores.isEmpty()) {
                        Centrado("No hay ciclos anteriores.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(anteriores, key = { it.first.inicioIso }) { (ciclo, resumen) ->
                                CarpetaCiclo(
                                    ciclo = ciclo,
                                    resumen = resumen,
                                    onAbrir = { onAbrirCiclo(ciclo.inicioIso, ciclo.finIso) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun aCiclo(resumen: ResumenCiclo): Ciclo? = runCatching {
    Ciclo(
        inicio = LocalDate.parse(resumen.inicio),
        fin = LocalDate.parse(resumen.fin),
    )
}.getOrNull()

@Composable
private fun CarpetaCiclo(
    ciclo: Ciclo,
    resumen: ResumenCiclo,
    onAbrir: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAbrir),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Folder, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = etiquetaCiclo(ciclo),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${resumen.cantidad} gastos",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = formatearMonto(resumen.totalCents),
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

@Composable
private fun Centrado(texto: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(texto)
    }
}
