package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.util.formatearPesos
import com.andromeda.gastos.domain.util.mesDe
import com.andromeda.gastos.domain.util.nombreMes
import com.andromeda.gastos.infrastructure.ui.components.EncabezadoGrupo
import com.andromeda.gastos.infrastructure.ui.components.EstadoVacio
import com.andromeda.gastos.infrastructure.ui.components.GastoCard
import com.andromeda.gastos.infrastructure.ui.components.PantallaCargando
import com.andromeda.gastos.infrastructure.ui.viewmodel.BuscarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarScreen(
    container: AppContainer,
    hogarId: String,
    onVolver: () -> Unit,
) {
    val vm: BuscarViewModel = viewModel(
        key = "buscar-$hogarId",
        factory = BuscarViewModel.factory(container),
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val foco = remember { FocusRequester() }
    val listaEstado = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(hogarId) {
        vm.cargar(hogarId)
        runCatching { foco.requestFocus() }
    }

    val resultados = remember(state.todos, state.texto, state.personaId) { state.resultados }
    val porMes = remember(resultados) {
        resultados.groupBy { gasto -> mesDe(gasto.fecha.take(10))?.let(::nombreMes) ?: "Sin fecha" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar gastos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            OutlinedTextField(
                value = state.texto,
                onValueChange = vm::cambiarTexto,
                placeholder = { Text("Nombre de la compra o pago") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.texto.isNotEmpty()) {
                        IconButton(onClick = { vm.cambiarTexto("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Borrar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(foco),
            )

            if (state.personas.size > 1) {
                Text(
                    text = "Registrado por",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = state.personaId == null,
                            onClick = { vm.elegirPersona(null) },
                            label = { Text("Todos") },
                        )
                    }
                    items(state.personas, key = { it.id }) { persona ->
                        FilterChip(
                            selected = state.personaId == persona.id,
                            onClick = { vm.elegirPersona(persona.id) },
                            label = { Text(persona.nombre) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                                )
                            },
                        )
                    }
                }
            } else {
                Box(Modifier.height(8.dp))
            }

            when {
                state.cargando -> PantallaCargando()
                state.error != null -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EstadoVacio(
                        icono = Icons.Outlined.CloudOff,
                        titulo = state.error.orEmpty(),
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = { vm.cargar(hogarId, forzar = true) },
                        modifier = Modifier.padding(bottom = 48.dp),
                    ) { Text("Reintentar") }
                }
                resultados.isEmpty() -> EstadoVacio(
                    icono = Icons.Outlined.SearchOff,
                    titulo = if (state.filtrando) "Sin resultados" else "Aún no hay gastos",
                    texto = if (state.filtrando) "Prueba con otra palabra o quita el filtro." else null,
                )
                else -> Column {
                    if (state.filtrando) {
                        ResumenBusqueda(
                            cantidad = resultados.size,
                            totalCents = resultados.sumOf { it.montoCents },
                        )
                    }
                    LaunchedEffect(state.texto, state.personaId) { listaEstado.scrollToItem(0) }
                    LazyColumn(
                        state = listaEstado,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                    porMes.forEach { (mes, gastos) ->
                        item(key = "mes-$mes") {
                            EncabezadoGrupo(
                                texto = mes,
                                extremo = formatearPesos(gastos.sumOf { it.montoCents }),
                            )
                        }
                        items(gastos, key = { it.id }) { gasto ->
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
private fun ResumenBusqueda(cantidad: Int, totalCents: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (cantidad == 1) "1 resultado" else "$cantidad resultados",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Total ${formatearPesos(totalCents)}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
