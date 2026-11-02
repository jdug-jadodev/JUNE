package com.andromeda.gastos.infrastructure.ui.screen

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.util.Ciclo
import com.andromeda.gastos.domain.util.cicloActual
import com.andromeda.gastos.domain.util.etiquetaCiclo
import com.andromeda.gastos.infrastructure.ui.components.CampoPassword
import com.andromeda.gastos.infrastructure.ui.viewmodel.SettingsViewModel
import com.andromeda.gastos.infrastructure.widget.VoiceWidgetProvider
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    hogarId: String,
    onSeguridad: () -> Unit,
    onVolver: () -> Unit,
    onLogout: () -> Unit,
) {
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val contexto = LocalContext.current

    LaunchedEffect(hogarId) {
        vm.cargarRol(hogarId)
        vm.cargarCorte(hogarId)
        vm.cargarCiclos(hogarId)
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
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Ciclo de gastos",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Elige el día en que empieza tu ciclo. Por ejemplo, 29 significa " +
                    "del 29 de un mes al 28 del siguiente.",
                style = MaterialTheme.typography.bodySmall,
            )

            val diaValido = state.corteTexto.toIntOrNull()?.let { it in 1..31 } == true
            val diaPrevio = state.corteTexto.toIntOrNull()?.takeIf { it in 1..31 } ?: state.cutoffDay
            Text(
                text = "Ciclo actual: ${etiquetaCiclo(cicloActual(diaPrevio))}",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (state.esAdmin) {
                OutlinedTextField(
                    value = state.corteTexto,
                    onValueChange = vm::onCorte,
                    label = { Text("Día de corte (1–31)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { vm.guardarCorte(hogarId) },
                    enabled = diaValido && !state.guardandoCorte,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.guardandoCorte) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Guardar ciclo")
                    }
                }
            } else if (!state.cargandoRol) {
                Text(
                    text = "Solo el administrador del hogar puede cambiar el día de corte.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            HorizontalDivider()

            if (state.esAdmin) {
                Text(
                    text = "Contraseña de administrador",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Solo quien la conozca podrá editar y eliminar gastos.",
                    style = MaterialTheme.typography.bodySmall,
                )
                CampoPassword(
                    value = state.password,
                    onValueChange = vm::onPassword,
                    label = "Nueva contraseña (mín. 8)",
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { vm.guardarPassword(hogarId) },
                    enabled = !state.guardando && state.password.length >= 8,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Guardar contraseña")
                    }
                }
            } else if (!state.cargandoRol) {
                Text(
                    text = "Solo el administrador del hogar puede cambiar la contraseña.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            HorizontalDivider()

            Button(
                onClick = onSeguridad,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Seguridad de la cuenta")
            }

            HorizontalDivider()

            Text(
                text = "Exportar gastos",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Selecciona los ciclos que quieres exportar o marca todos.",
                style = MaterialTheme.typography.bodySmall,
            )

            if (state.cargandoCiclos) {
                CircularProgressIndicator(Modifier.size(24.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = state.todosSeleccionados,
                        onCheckedChange = { vm.seleccionarTodos(it) },
                    )
                    Text("Todos los ciclos")
                }

                state.ciclos.forEach { resumen ->
                    val ciclo = runCatching {
                        Ciclo(
                            inicio = LocalDate.parse(resumen.inicio),
                            fin = LocalDate.parse(resumen.fin),
                        )
                    }.getOrNull()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = resumen.inicio in state.ciclosSeleccionados,
                            onCheckedChange = { vm.toggleCiclo(resumen.inicio) },
                        )
                        Text(ciclo?.let { etiquetaCiclo(it) } ?: resumen.inicio)
                    }
                }

                Button(
                    onClick = { vm.exportar(hogarId) },
                    enabled = !state.guardando &&
                        (state.todosSeleccionados || state.ciclosSeleccionados.isNotEmpty()),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Exportar CSV")
                    }
                }
            }

            HorizontalDivider()

            Text(
                text = "Widget de voz",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Agrega el micrófono a tu pantalla de inicio.",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(
                onClick = {
                    val manager = AppWidgetManager.getInstance(contexto)
                    val provider = ComponentName(contexto, VoiceWidgetProvider::class.java)
                    if (manager.isRequestPinAppWidgetSupported) {
                        manager.requestPinAppWidget(provider, null, null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar widget al inicio")
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Servidor: ${com.andromeda.gastos.BuildConfig.API_BASE_URL}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
