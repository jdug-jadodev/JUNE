package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Miembro
import com.andromeda.gastos.domain.enums.Acceso
import com.andromeda.gastos.domain.enums.Rol
import com.andromeda.gastos.infrastructure.ui.components.MensajeError
import com.andromeda.gastos.infrastructure.ui.components.PantallaCargando
import com.andromeda.gastos.infrastructure.ui.viewmodel.MembersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    container: AppContainer,
    hogarId: String,
    onVolver: () -> Unit,
) {
    val vm: MembersViewModel = viewModel(factory = MembersViewModel.factory(container))
    val state by vm.state.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val portapapeles = LocalClipboardManager.current

    var miembroEliminar by remember { mutableStateOf<Miembro?>(null) }

    LaunchedEffect(hogarId) {
        vm.cargar(hogarId)
    }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.hogar?.nombre ?: "Miembros") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.cargando) {
            PantallaCargando()
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                state.hogar?.let { hogar ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Código de invitación",
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Text(
                                text = hogar.inviteCode,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Button(
                                onClick = {
                                    portapapeles.setText(AnnotatedString(hogar.inviteCode))
                                },
                            ) {
                                Text("Copiar código")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Familia", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (state.esAdmin) {
                        "Activa o desactiva la escritura de cada miembro."
                    } else {
                        "El administrador controla quién puede registrar gastos."
                    },
                    style = MaterialTheme.typography.bodySmall,
                )

                LazyColumn(Modifier.weight(1f)) {
                    items(state.miembros, key = { it.id }) { miembro ->
                        val esAdministrador = miembro.rol == Rol.ADMIN
                        val puedeEscribir = esAdministrador || miembro.acceso == Acceso.ESCRITURA

                        ListItem(
                            headlineContent = {
                                Text(miembro.nombre.ifBlank { miembro.email })
                            },
                            supportingContent = {
                                Text(
                                    "${miembro.email} · " + when {
                                        esAdministrador -> "Administrador"
                                        puedeEscribir -> "Puede escribir"
                                        else -> "Solo lectura"
                                    },
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (state.esAdmin && !esAdministrador) {
                                        Switch(
                                            checked = puedeEscribir,
                                            onCheckedChange = { activo ->
                                                vm.cambiarAcceso(
                                                    hogarId = hogarId,
                                                    miembroId = miembro.id,
                                                    acceso = if (activo) {
                                                        Acceso.ESCRITURA
                                                    } else {
                                                        Acceso.LECTURA
                                                    },
                                                )
                                            },
                                        )
                                    }
                                    if (state.esAdmin && miembro.usuarioId != state.miUsuarioId) {
                                        IconButton(onClick = { miembroEliminar = miembro }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                        }
                                    }
                                }
                            },
                        )
                    }
                }

                MensajeError(state.error)
            }
        }
    }

    miembroEliminar?.let { miembro ->
        AlertDialog(
            onDismissRequest = { miembroEliminar = null },
            title = { Text("Eliminar miembro") },
            text = { Text("¿Eliminar a ${miembro.nombre} del hogar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.eliminar(hogarId, miembro.id)
                        miembroEliminar = null
                    },
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { miembroEliminar = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}
