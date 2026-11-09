package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import com.andromeda.gastos.infrastructure.ui.components.EstadoVacio
import com.andromeda.gastos.infrastructure.ui.components.TarjetaSuave
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    val snackbar = remember { SnackbarHostState() }

    var editando by remember { mutableStateOf<Ingreso?>(null) }
    var mostrandoDialogo by remember { mutableStateOf(false) }

    LaunchedEffect(hogarId) {
        vm.cargar(hogarId)
    }

    LaunchedEffect(state.mensaje, state.error) {
        (state.mensaje ?: state.error)?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingresos del hogar") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editando = null
                    mostrandoDialogo = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar ingreso")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = "Estos son los ingresos del hogar. La IA los usará para comparar " +
                    "tus gastos contra lo que entra.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            when {
                state.cargando -> CircularProgressIndicator(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp),
                )

                state.ingresos.isEmpty() -> EstadoVacio(
                    icono = Icons.Outlined.AccountBalanceWallet,
                    titulo = "Todavía no hay ingresos",
                    texto = "Toca el botón + para agregar el primero.",
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.ingresos, key = { it.id }) { ingreso ->
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
private fun FilaIngreso(
    ingreso: Ingreso,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
) {
    TarjetaSuave(relleno = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 6.dp, top = 14.dp, bottom = 14.dp),
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
                        } else {
                            Text("Ej. 2.000.000 o 2 millones")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Periodicidad.values().forEach { opcion ->
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
