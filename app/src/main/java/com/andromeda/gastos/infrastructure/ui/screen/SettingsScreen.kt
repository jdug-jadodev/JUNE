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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.util.nombreMes
import com.andromeda.gastos.infrastructure.ui.viewmodel.SettingsViewModel
import com.andromeda.gastos.infrastructure.widget.VoiceWidgetProvider
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    hogarId: String,
    onVolver: () -> Unit,
    onLogout: () -> Unit,
) {
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val contexto = LocalContext.current

    LaunchedEffect(hogarId) {
        vm.cargarRol(hogarId)
        vm.cargarMeses(hogarId)
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
            if (state.esAdmin) {
                Text(
                    text = "Contraseña de administrador",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Solo quien la conozca podrá editar y eliminar gastos.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::onPassword,
                    label = { Text("Nueva contraseña (mín. 8)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
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

            Text(
                text = "Exportar gastos",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Selecciona los meses que quieres exportar o marca todos.",
                style = MaterialTheme.typography.bodySmall,
            )

            if (state.cargandoMeses) {
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
                    Text("Todos los meses")
                }

                state.meses.forEach { mes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = mes in state.mesesSeleccionados,
                            onCheckedChange = { vm.toggleMes(mes) },
                        )
                        Text(nombreMes(YearMonth.parse(mes)))
                    }
                }

                Button(
                    onClick = { vm.exportar(hogarId) },
                    enabled = !state.guardando &&
                        (state.todosSeleccionados || state.mesesSeleccionados.isNotEmpty()),
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
