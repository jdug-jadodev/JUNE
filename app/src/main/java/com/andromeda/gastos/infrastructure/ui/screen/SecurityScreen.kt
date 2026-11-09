package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Key
import com.andromeda.gastos.infrastructure.ui.viewmodel.SettingsViewModel
import com.andromeda.gastos.infrastructure.ui.components.SeccionTarjeta
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.infrastructure.ui.components.CampoPassword
import com.andromeda.gastos.infrastructure.ui.viewmodel.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    container: AppContainer,
    hogarId: String,
    onVolver: () -> Unit,
) {
    val vm: SecurityViewModel = viewModel(factory = SecurityViewModel.factory(container))
    val state by vm.state.collectAsStateWithLifecycle()
    val ajustesVm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
    val ajustes by ajustesVm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val portapapeles = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        vm.cargar()
    }

    LaunchedEffect(hogarId) {
        ajustesVm.cargarRol(hogarId)
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
                title = { Text("Seguridad de la cuenta") },
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
            SeccionTarjeta(
                titulo = "Contraseña de administrador",
                icono = Icons.Outlined.Key,
                descripcion = if (ajustes.esAdmin) {
                    "Solo quien la conozca podrá editar y eliminar gastos."
                } else {
                    null
                },
            ) {
                if (ajustes.esAdmin) {
                    CampoPassword(
                        value = ajustes.password,
                        onValueChange = ajustesVm::onPassword,
                        label = "Nueva contraseña (mín. 8)",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    BotonCargando(
                        texto = "Guardar contraseña",
                        cargando = ajustes.guardando,
                        habilitado = !ajustes.guardando && ajustes.password.length >= 8,
                        onClick = { ajustesVm.guardarPassword(hogarId) },
                    )
                } else if (!ajustes.cargandoRol) {
                    Text(
                        text = "Solo el administrador del hogar puede cambiar la contraseña.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SeccionTarjeta(
                titulo = "Pregunta de seguridad",
                icono = Icons.AutoMirrored.Outlined.HelpOutline,
                descripcion = "Escríbela tú mismo. La respuesta se guarda cifrada.",
            ) {
                state.preguntaActual?.let {
                    Text(
                        text = "Pregunta actual: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedTextField(
                    value = state.pregunta,
                    onValueChange = vm::onPregunta,
                    label = { Text("Tu pregunta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                CampoPassword(
                    value = state.respuesta,
                    onValueChange = vm::onRespuesta,
                    label = "Respuesta",
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.fillMaxWidth(),
                )
                BotonCargando(
                    texto = "Guardar pregunta",
                    cargando = state.guardandoPregunta,
                    habilitado = !state.guardandoPregunta,
                    onClick = vm::guardarPregunta,
                )
            }

            SeccionTarjeta(
                titulo = "Llave de recuperación",
                icono = Icons.Outlined.VpnKey,
                descripcion = "Es un código de un solo uso. Guárdalo en un lugar seguro; " +
                    "si lo pierdes, genera otra llave.",
            ) {
                if (state.tieneCodigo) {
                    Text(
                        text = "Ya tienes una llave generada.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                BotonCargando(
                    texto = if (state.tieneCodigo) "Generar nueva llave" else "Generar llave",
                    cargando = state.generandoCodigo,
                    habilitado = !state.generandoCodigo,
                    onClick = vm::generarCodigo,
                )
            }

            SeccionTarjeta(
                titulo = "Cambiar mi contraseña",
                icono = Icons.Outlined.Lock,
            ) {
                CampoPassword(
                    value = state.passwordActual,
                    onValueChange = vm::onPasswordActual,
                    label = "Contraseña actual",
                    modifier = Modifier.fillMaxWidth(),
                )
                CampoPassword(
                    value = state.passwordNueva,
                    onValueChange = vm::onPasswordNueva,
                    label = "Nueva contraseña (mín. 8)",
                    modifier = Modifier.fillMaxWidth(),
                )
                CampoPassword(
                    value = state.passwordConfirmacion,
                    onValueChange = vm::onPasswordConfirmacion,
                    label = "Confirmar nueva contraseña",
                    modifier = Modifier.fillMaxWidth(),
                )
                BotonCargando(
                    texto = "Cambiar contraseña",
                    cargando = state.cambiandoPassword,
                    habilitado = !state.cambiandoPassword,
                    onClick = vm::cambiarPassword,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    state.codigoGenerado?.let { codigo ->
        AlertDialog(
            onDismissRequest = vm::cerrarCodigo,
            title = { Text("Guarda esta llave") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "No se volverá a mostrar. Úsala si olvidas tu contraseña.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = codigo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        portapapeles.setText(AnnotatedString(codigo))
                        vm.cerrarCodigo()
                    },
                ) {
                    Text("Copiar y cerrar")
                }
            },
            dismissButton = {
                TextButton(onClick = vm::cerrarCodigo) {
                    Text("Cerrar")
                }
            },
        )
    }
}

@Composable
private fun BotonCargando(
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
