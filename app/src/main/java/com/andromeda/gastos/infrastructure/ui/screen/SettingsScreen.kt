package com.andromeda.gastos.infrastructure.ui.screen

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.util.Ciclo
import com.andromeda.gastos.domain.util.cicloActual
import com.andromeda.gastos.domain.util.etiquetaCiclo
import com.andromeda.gastos.infrastructure.ui.components.CampoPassword
import com.andromeda.gastos.infrastructure.ui.components.SeccionTarjeta
import com.andromeda.gastos.infrastructure.ui.components.TarjetaSuave
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
    onIngresos: () -> Unit,
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FilaNavegacion(
                titulo = "Ciclo e ingresos",
                descripcion = "Día de corte del ciclo y lo que entra al hogar.",
                icono = Icons.Outlined.CalendarMonth,
                onClick = onIngresos,
            )

            FilaNavegacion(
                titulo = "Seguridad de la cuenta",
                descripcion = "Contraseñas, pregunta y llave de recuperación.",
                icono = Icons.Outlined.Shield,
                onClick = onSeguridad,
            )

            SeccionTarjeta(
                titulo = "Exportar gastos",
                icono = Icons.Outlined.FileDownload,
                descripcion = "Selecciona los ciclos que quieres exportar o marca todos.",
            ) {
                if (state.cargandoCiclos) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Column {
                        FilaCheck(
                            texto = "Todos los ciclos",
                            marcado = state.todosSeleccionados,
                            onCambio = { vm.seleccionarTodos(it) },
                        )
                        state.ciclos.forEach { resumen ->
                            val ciclo = runCatching {
                                Ciclo(
                                    inicio = LocalDate.parse(resumen.inicio),
                                    fin = LocalDate.parse(resumen.fin),
                                )
                            }.getOrNull()
                            FilaCheck(
                                texto = ciclo?.let { etiquetaCiclo(it) } ?: resumen.inicio,
                                marcado = resumen.inicio in state.ciclosSeleccionados,
                                onCambio = { vm.toggleCiclo(resumen.inicio) },
                            )
                        }
                    }
                    BotonConCarga(
                        texto = "Exportar CSV",
                        cargando = state.guardando,
                        habilitado = !state.guardando &&
                            (state.todosSeleccionados || state.ciclosSeleccionados.isNotEmpty()),
                        onClick = { vm.exportar(hogarId) },
                    )
                }
            }

            SeccionTarjeta(
                titulo = "Widget de voz",
                icono = Icons.Outlined.Widgets,
                descripcion = "Agrega el micrófono a tu pantalla de inicio.",
            ) {
                FilledTonalButton(
                    onClick = {
                        val manager = AppWidgetManager.getInstance(contexto)
                        val provider = ComponentName(contexto, VoiceWidgetProvider::class.java)
                        if (manager.isRequestPinAppWidgetSupported) {
                            manager.requestPinAppWidget(provider, null, null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar widget al inicio")
                }
            }

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            Text(
                text = "Servidor: ${com.andromeda.gastos.BuildConfig.API_BASE_URL}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun BotonConCarga(
    texto: String,
    cargando: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(texto)
        }
    }
}

@Composable
private fun NotaSoloAdmin(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun FilaCheck(texto: String, marcado: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCambio(!marcado) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = marcado, onCheckedChange = onCambio)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FilaNavegacion(
    titulo: String,
    descripcion: String?,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    TarjetaSuave(onClick = onClick, relleno = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp),
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                if (descripcion != null) {
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
