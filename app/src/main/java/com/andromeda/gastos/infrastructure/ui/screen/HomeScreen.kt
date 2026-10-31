package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.util.formatearMonto
import com.andromeda.gastos.domain.util.mesDe
import com.andromeda.gastos.domain.util.nombreMes
import com.andromeda.gastos.infrastructure.ui.components.DialogoPassword
import com.andromeda.gastos.infrastructure.ui.components.EditarGastoDialog
import com.andromeda.gastos.infrastructure.ui.components.GastoCard
import com.andromeda.gastos.infrastructure.ui.components.MensajeError
import com.andromeda.gastos.infrastructure.ui.components.PantallaCargando
import com.andromeda.gastos.infrastructure.ui.viewmodel.HomeUiState
import com.andromeda.gastos.infrastructure.ui.viewmodel.HomeViewModel
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onAgregar: (String) -> Unit,
    onVoz: (String) -> Unit,
    onMiembros: (String) -> Unit,
    onAjustes: (String) -> Unit,
    onVerMeses: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    var gastoEditar by remember { mutableStateOf<Gasto?>(null) }
    var gastoEliminar by remember { mutableStateOf<Gasto?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            vm.limpiarMensaje()
        }
    }

    val hogarId = (state as? HomeUiState.Listo)?.hogar?.id

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Andrómeda") },
                actions = {
                    if (hogarId != null) {
                        IconButton(onClick = { onMiembros(hogarId) }) {
                            Icon(Icons.Default.People, contentDescription = "Miembros")
                        }
                        IconButton(onClick = { onAjustes(hogarId) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (hogarId != null) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FloatingActionButton(onClick = { onVoz(hogarId) }) {
                        Icon(Icons.Default.Mic, contentDescription = "Registrar por voz")
                    }
                    FloatingActionButton(onClick = { onAgregar(hogarId) }) {
                        Icon(Icons.Default.Add, contentDescription = "Registrar manualmente")
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (val actual = state) {
                HomeUiState.Cargando -> PantallaCargando()
                HomeUiState.SinHogar -> Onboarding(vm)
                is HomeUiState.Error -> ErrorConReintento(actual.mensaje, vm::cargar)
                is HomeUiState.Listo -> ListaGastos(
                    estado = actual,
                    onVerMeses = onVerMeses,
                    onEditar = { gastoEditar = it },
                    onEliminar = { gastoEliminar = it },
                )
            }
        }
    }

    gastoEliminar?.let { gasto ->
        DialogoPassword(
            titulo = "Eliminar \"${gasto.producto}\"",
            onConfirmar = { password ->
                vm.eliminar(gasto.id, password)
                gastoEliminar = null
            },
            onCancelar = { gastoEliminar = null },
        )
    }

    gastoEditar?.let { gasto ->
        EditarGastoDialog(
            gasto = gasto,
            onConfirmar = { producto, montoCents, fecha, password ->
                vm.actualizar(gasto.id, producto, montoCents, fecha, password)
                gastoEditar = null
            },
            onCancelar = { gastoEditar = null },
        )
    }
}

@Composable
private fun ListaGastos(
    estado: HomeUiState.Listo,
    onVerMeses: (String) -> Unit,
    onEditar: (Gasto) -> Unit,
    onEliminar: (Gasto) -> Unit,
) {
    val mesActual = YearMonth.now()
    val gastosMes = estado.gastos.filter { mesDe(it.fecha) == mesActual }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = estado.hogar.nombre,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Mes actual",
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = nombreMes(mesActual),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            TextButton(onClick = { onVerMeses(estado.hogar.id) }) {
                Icon(Icons.Default.Folder, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Meses anteriores")
            }
        }

        TotalDelMes(
            totalCents = gastosMes.sumOf { it.montoCents },
            cantidad = gastosMes.size,
        )

        if (gastosMes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Aún no hay gastos este mes.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(gastosMes, key = { it.id }) { gasto ->
                    GastoCard(
                        gasto = gasto,
                        esAdmin = estado.esAdmin,
                        onEditar = onEditar,
                        onEliminar = onEliminar,
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalDelMes(
    totalCents: Long,
    cantidad: Int,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Total gastado este mes",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = formatearMonto(totalCents),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = if (cantidad == 1) "1 gasto" else "$cantidad gastos",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun Onboarding(vm: HomeViewModel) {
    var nombre by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Configura tu hogar", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Crea un hogar nuevo o únete con un código de invitación.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del hogar") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { vm.crearHogar(nombre) },
            enabled = nombre.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Crear hogar")
        }

        HorizontalDivider(Modifier.padding(vertical = 24.dp))

        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it.uppercase() },
            label = { Text("Código de invitación") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { vm.unirse(codigo) },
            enabled = codigo.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Unirme")
        }
    }
}

@Composable
private fun ErrorConReintento(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(mensaje, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}
