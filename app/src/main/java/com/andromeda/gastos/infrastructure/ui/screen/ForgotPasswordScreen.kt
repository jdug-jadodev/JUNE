package com.andromeda.gastos.infrastructure.ui.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.infrastructure.ui.components.CampoPassword
import com.andromeda.gastos.infrastructure.ui.components.MensajeError
import com.andromeda.gastos.infrastructure.ui.viewmodel.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    container: AppContainer,
    onVolver: () -> Unit,
) {
    val vm: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModel.factory(container),
    )
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
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
                .imePadding()
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Elige cómo quieres recuperar tu cuenta.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !state.usandoCodigo,
                    onClick = { vm.usarCodigo(false) },
                    label = { Text("Pregunta") },
                )
                FilterChip(
                    selected = state.usandoCodigo,
                    onClick = { vm.usarCodigo(true) },
                    label = { Text("Llave") },
                )
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = vm::onEmail,
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.usandoCodigo) {
                Text(
                    text = "Escribe la llave que guardaste cuando la generaste.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = state.codigo,
                    onValueChange = vm::onCodigo,
                    label = { Text("Llave de recuperación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Button(
                    onClick = vm::buscarPregunta,
                    enabled = !state.buscando && state.email.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.buscando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Buscar mi pregunta")
                    }
                }

                state.pregunta?.let { pregunta ->
                    Text(
                        text = pregunta,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    CampoPassword(
                        value = state.respuesta,
                        onValueChange = vm::onRespuesta,
                        label = "Respuesta",
                        keyboardType = KeyboardType.Text,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            CampoPassword(
                value = state.passwordNueva,
                onValueChange = vm::onPasswordNueva,
                label = "Nueva contraseña (mín. 8)",
                modifier = Modifier.fillMaxWidth(),
            )
            CampoPassword(
                value = state.confirmacion,
                onValueChange = vm::onConfirmacion,
                label = "Confirmar nueva contraseña",
                modifier = Modifier.fillMaxWidth(),
            )

            MensajeError(state.error)
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = vm::recuperar,
                enabled = !state.cargando,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.cargando) {
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

    if (state.listo) {
        AlertDialog(
            onDismissRequest = onVolver,
            title = { Text("Contraseña actualizada") },
            text = { Text("Ya puedes iniciar sesión con tu nueva contraseña.") },
            confirmButton = {
                TextButton(onClick = onVolver) {
                    Text("Volver a iniciar sesión")
                }
            },
        )
    }
}
