package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Surface
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.FilledTonalButton
import com.andromeda.gastos.infrastructure.ui.components.TarjetaSuave
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontFamily
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
    val passwordTemporal by vm.passwordTemporal.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val portapapeles = LocalClipboardManager.current

    var miembroEliminar by remember { mutableStateOf<Miembro?>(null) }
    var menuAbierto by remember { mutableStateOf<String?>(null) }

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
                    TarjetaSuave(relleno = 18.dp) {
                        Text(
                            text = "Código de invitación",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hogar.inviteCode,
                                style = MaterialTheme.typography.headlineSmall,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                            )
                            FilledTonalButton(
                                onClick = {
                                    portapapeles.setText(AnnotatedString(hogar.inviteCode))
                                },
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Copiar")
                            }
                        }
                        Text(
                            text = "Compártelo para que tu familia se una al hogar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    "Familia",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Text(
                    text = if (state.esAdmin) {
                        "Activa o desactiva la escritura de cada miembro."
                    } else {
                        "El administrador controla quién puede registrar gastos."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.miembros, key = { it.id }) { miembro ->
                        val esAdministrador = miembro.rol == Rol.ADMIN
                        val puedeEscribir = esAdministrador || miembro.acceso == Acceso.ESCRITURA

                        TarjetaSuave(relleno = 0.dp) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = miembro.nombre.ifBlank { miembro.email }
                                                .take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }
                                }
                            },
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        Box {
                                            IconButton(
                                                onClick = { menuAbierto = miembro.id },
                                            ) {
                                                Icon(
                                                    Icons.Default.MoreVert,
                                                    contentDescription = "Más opciones",
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = menuAbierto == miembro.id,
                                                onDismissRequest = { menuAbierto = null },
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Restablecer contraseña") },
                                                    onClick = {
                                                        vm.restablecerPassword(hogarId, miembro.id)
                                                        menuAbierto = null
                                                    },
                                                )
                                                if (
                                                    miembro.usuarioId != state.miUsuarioId &&
                                                    miembro.usuarioId != state.hogar?.ownerId
                                                ) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                if (esAdministrador) {
                                                                    "Quitar administrador"
                                                                } else {
                                                                    "Hacer administrador"
                                                                },
                                                            )
                                                        },
                                                        onClick = {
                                                            vm.cambiarRol(
                                                                hogarId,
                                                                miembro.id,
                                                                if (esAdministrador) {
                                                                    Rol.MIEMBRO
                                                                } else {
                                                                    Rol.ADMIN
                                                                },
                                                            )
                                                            menuAbierto = null
                                                        },
                                                    )
                                                }
                                                if (miembro.usuarioId != state.miUsuarioId) {
                                                    DropdownMenuItem(
                                                        text = { Text("Eliminar") },
                                                        onClick = {
                                                            miembroEliminar = miembro
                                                            menuAbierto = null
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                        )
                        }
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

    passwordTemporal?.let { temporal ->
        AlertDialog(
            onDismissRequest = vm::limpiarPasswordTemporal,
            title = { Text("Contraseña temporal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Compártela con la persona. Deberá cambiarla al entrar.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = temporal,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        portapapeles.setText(AnnotatedString(temporal))
                        vm.limpiarPasswordTemporal()
                    },
                ) {
                    Text("Copiar y cerrar")
                }
            },
            dismissButton = {
                TextButton(onClick = vm::limpiarPasswordTemporal) {
                    Text("Cerrar")
                }
            },
        )
    }
}
