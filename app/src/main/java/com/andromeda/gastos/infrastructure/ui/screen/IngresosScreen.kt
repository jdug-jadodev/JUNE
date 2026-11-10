package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Surface
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material.icons.outlined.CalendarMonth
import com.andromeda.gastos.domain.util.etiquetaCiclo
import com.andromeda.gastos.domain.util.cicloActual
import com.andromeda.gastos.infrastructure.ui.viewmodel.SettingsUiState
import com.andromeda.gastos.infrastructure.ui.viewmodel.SettingsViewModel
import com.andromeda.gastos.infrastructure.ui.components.SeccionTarjeta
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import com.andromeda.gastos.infrastructure.ui.components.EstadoVacio
import com.andromeda.gastos.infrastructure.ui.components.TarjetaSuave
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Ingreso
import com.andromeda.gastos.domain.entity.Periodicidad
import com.andromeda.gastos.domain.entity.equivalenteMensualCents
import com.andromeda.gastos.domain.util.MontoParser
import com.andromeda.gastos.domain.util.formatearMonto
import com.andromeda.gastos.infrastructure.ui.viewmodel.IngresosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngresosScreen(
    container: AppContainer,
    hogarId: String,
    onVolver: () -> Unit,
) {
    val vm: IngresosViewModel = viewModel(factory = IngresosViewModel.factory(container))
    val state by vm.state.collectAsStateWithLifecycle()
    val ajustesVm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
    val ajustes by ajustesVm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var editando by remember { mutableStateOf<Ingreso?>(null) }
    var mostrandoDialogo by remember { mutableStateOf(false) }

    LaunchedEffect(hogarId) {
        vm.cargar(hogarId)
        ajustesVm.cargarRol(hogarId)
        ajustesVm.cargarCorte(hogarId)
    }

    LaunchedEffect(state.mensaje, state.error) {
        (state.mensaje ?: state.error)?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    LaunchedEffect(ajustes.mensaje, ajustes.error) {
        (ajustes.mensaje ?: ajustes.error)?.let {
            snackbar.showSnackbar(it)
            ajustesVm.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ciclo e ingresos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "ciclo") {
                SeccionCiclo(
                    estado = ajustes,
                    onCorte = ajustesVm::onCorte,
                    onGuardar = { ajustesVm.guardarCorte(hogarId) },
                )
            }

            item(key = "ingresos") {
                SeccionTarjeta(
                    titulo = "Ingresos del hogar",
                    icono = Icons.Outlined.AccountBalanceWallet,
                    descripcion = if (ajustes.esAdmin) {
                        "La IA los usa para comparar tus gastos contra lo que entra. " +
                            "También puedes registrar ingresos semanales o diarios."
                    } else {
                        null
                    },
                ) {
                    when {
                        ajustes.cargandoRol -> Unit
                        !ajustes.esAdmin -> Text(
                            text = "Solo el administrador del hogar puede gestionar los ingresos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        else -> {
                            when {
                                state.cargando -> CircularProgressIndicator(Modifier.size(24.dp))
                                state.ingresos.isEmpty() -> Text(
                                    text = "Todavía no hay ingresos.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                else -> state.ingresos.forEach { ingreso ->
                                    FilaIngreso(
                                        ingreso = ingreso,
                                        onEditar = {
                                            editando = ingreso
                                            mostrandoDialogo = true
                                        },
                                        onEliminar = { vm.eliminar(hogarId, ingreso.id) },
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = {
                                    editando = null
                                    mostrandoDialogo = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Agregar ingreso")
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrandoDialogo) {
        DialogoIngreso(
            ingreso = editando,
            guardando = state.guardando,
            onCancelar = { mostrandoDialogo = false },
            onGuardar = { nombre, montoCents, periodicidad ->
                if (editando == null) {
                    vm.crear(hogarId, nombre, montoCents, periodicidad)
                } else {
                    vm.actualizar(hogarId, editando!!.id, nombre, montoCents, periodicidad)
                }
                mostrandoDialogo = false
            },
        )
    }
}

@Composable
private fun SeccionCiclo(
    estado: SettingsUiState,
    onCorte: (String) -> Unit,
    onGuardar: () -> Unit,
) {
    val diaValido = estado.corteTexto.toIntOrNull()?.let { it in 1..31 } == true
    val diaPrevio = estado.corteTexto.toIntOrNull()?.takeIf { it in 1..31 } ?: estado.cutoffDay

    SeccionTarjeta(
        titulo = "Ciclo de gastos",
        icono = Icons.Outlined.CalendarMonth,
        descripcion = "Elige el día en que empieza tu ciclo. Por ejemplo, 29 significa " +
            "del 29 de un mes al 28 del siguiente.",
    ) {
        Text(
            text = "Ciclo actual: ${etiquetaCiclo(cicloActual(diaPrevio))}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        if (estado.esAdmin) {
            OutlinedTextField(
                value = estado.corteTexto,
                onValueChange = onCorte,
                label = { Text("Día de corte (1–31)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onGuardar,
                enabled = diaValido && !estado.guardandoCorte,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                if (estado.guardandoCorte) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Guardar ciclo")
                }
            }
        } else if (!estado.cargandoRol) {
            Text(
                text = "Solo el administrador del hogar puede cambiar el día de corte.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FilaIngreso(
    ingreso: Ingreso,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = ingreso.nombre ?: "Ingreso",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = formatearMonto(ingreso.montoCents),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = ingreso.periodicidad.etiqueta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "≈ ${formatearMonto(ingreso.equivalenteMensualCents())} al mes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onEditar) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DialogoIngreso(
    ingreso: Ingreso?,
    guardando: Boolean,
    onCancelar: () -> Unit,
    onGuardar: (nombre: String?, montoCents: Long, periodicidad: Periodicidad) -> Unit,
) {
    var nombre by remember { mutableStateOf(ingreso?.nombre ?: "") }
    var montoTexto by remember {
        mutableStateOf(ingreso?.montoCents?.let { (it / 100).toString() } ?: "")
    }
    var periodicidad by remember {
        mutableStateOf(ingreso?.periodicidad ?: Periodicidad.MENSUAL)
    }

    val montoCents = MontoParser.aCentavos(montoTexto)

    Dialog(onDismissRequest = onCancelar) {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = if (ingreso == null) "Agregar ingreso" else "Editar ingreso",
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = montoTexto,
                    onValueChange = { montoTexto = it },
                    label = { Text("Monto") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = {
                        if (montoCents != null) {
                            Text(formatearMonto(montoCents))
                            Text(
                                "≈ ${formatearMonto(equivalenteMensualCents(montoCents, periodicidad))} al mes",
                            )
                        } else {
                            Text("Ej. 2.000.000 o 2 millones")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Periodicidad.entries.forEach { opcion ->
                        FilterChip(
                            selected = periodicidad == opcion,
                            onClick = { periodicidad = opcion },
                            label = { Text(opcion.etiqueta) },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onCancelar,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(),
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { montoCents?.let { onGuardar(nombre.trim().ifBlank { null }, it, periodicidad) } },
                        enabled = montoCents != null && !guardando,
                        modifier = Modifier.weight(1f),
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}
