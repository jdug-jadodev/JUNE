package com.andromeda.gastos.infrastructure.ui.screen

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
    onVolver: () -> Unit,
) {
    val vm: SecurityViewModel = viewModel(factory = SecurityViewModel.factory(container))
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val portapapeles = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        vm.cargar()
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
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Pregunta de seguridad",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Escríbela tú mismo. La respuesta se guarda cifrada.",
                style = MaterialTheme.typography.bodySmall,
            )
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
            Button(
                onClick = vm::guardarPregunta,
                enabled = !state.guardandoPregunta,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.guardandoPregunta) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Guardar pregunta")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text(
                text = "Llave de recuperación",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Es un código de un solo uso. Guárdalo en un lugar seguro; " +
                    "si lo pierdes, genera otra llave.",
                style = MaterialTheme.typography.bodySmall,
            )
            if (state.tieneCodigo) {
                Text(
                    text = "Ya tienes una llave generada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Button(
                onClick = vm::generarCodigo,
                enabled = !state.generandoCodigo,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.generandoCodigo) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(if (state.tieneCodigo) "Generar nueva llave" else "Generar llave")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text(
                text = "Cambiar mi contraseña",
                style = MaterialTheme.typography.titleMedium,
            )
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
            Button(
                onClick = vm::cambiarPassword,
                enabled = !state.cambiandoPassword,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.cambiandoPassword) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Cambiar contraseña")
                }
            }

            Spacer(Modifier.height(24.dp))
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
