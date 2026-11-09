package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andromeda.gastos.R
import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.util.cicloActual
import com.andromeda.gastos.domain.util.etiquetaCiclo
import com.andromeda.gastos.domain.util.formatearPesos
import com.andromeda.gastos.infrastructure.ui.components.DegradadoCauce
import com.andromeda.gastos.infrastructure.ui.components.DialogoPassword
import com.andromeda.gastos.infrastructure.ui.components.EditarGastoDialog
import com.andromeda.gastos.infrastructure.ui.components.EncabezadoGrupo
import com.andromeda.gastos.infrastructure.ui.components.EstadoVacio
import com.andromeda.gastos.infrastructure.ui.components.GastoCard
import com.andromeda.gastos.infrastructure.ui.components.Manrope
import com.andromeda.gastos.infrastructure.ui.components.PantallaCargando
import com.andromeda.gastos.infrastructure.ui.components.etiquetaDia
import com.andromeda.gastos.infrastructure.ui.viewmodel.HomeUiState
import com.andromeda.gastos.infrastructure.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onAgregar: (String) -> Unit,
    onVoz: (String) -> Unit,
    onMiembros: (String) -> Unit,
    onAjustes: (String) -> Unit,
    onAnalisis: (String) -> Unit,
    onVerMeses: (String) -> Unit,
    onBuscar: (String) -> Unit,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        vm.cargar()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        vm.detener()
    }

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
                title = { MarcaBarra() },
                actions = {
                    if (hogarId != null) {
                        IconButton(onClick = { onBuscar(hogarId) }) {
                            Icon(Icons.Outlined.Search, contentDescription = "Buscar gastos")
                        }
                        IconButton(onClick = { onAnalisis(hogarId) }) {
                            Icon(Icons.Outlined.Insights, contentDescription = "Análisis")
                        }
                        IconButton(onClick = { onMiembros(hogarId) }) {
                            Icon(Icons.Outlined.People, contentDescription = "Miembros")
                        }
                        IconButton(onClick = { onAjustes(hogarId) }) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Ajustes")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (hogarId != null && (state as? HomeUiState.Listo)?.puedeEscribir == true) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    SmallFloatingActionButton(
                        onClick = { onVoz(hogarId) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Registrar por voz")
                    }
                    FloatingActionButton(
                        onClick = { onAgregar(hogarId) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
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
private fun MarcaBarra() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.ic_cauce_symbol),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "cauce",
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            letterSpacing = (-0.5).sp,
            color = MaterialTheme.colorScheme.onBackground,
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
    val ciclo = cicloActual(estado.hogar.cutoffDay)
    val gastosMes = estado.gastos
    val porDia = remember(gastosMes) { gastosMes.groupBy { it.fecha.take(10) } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "resumen") {
            TotalDelCiclo(
                etiqueta = etiquetaCiclo(ciclo),
                totalCents = gastosMes.sumOf { it.montoCents },
                cantidad = gastosMes.size,
                onVerMeses = { onVerMeses(estado.hogar.id) },
            )
        }

        if (!estado.puedeEscribir) {
            item(key = "solo-lectura") {
                Text(
                    text = "Solo lectura · el administrador puede darte escritura",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                )
            }
        }

        if (gastosMes.isEmpty()) {
            item(key = "vacio") {
                EstadoVacio(
                    icono = Icons.AutoMirrored.Outlined.ReceiptLong,
                    titulo = "Aún no hay gastos en este ciclo",
                    texto = "Registra el primero con + o por voz.",
                    modifier = Modifier.height(320.dp),
                )
            }
        } else {
            porDia.forEach { (dia, gastos) ->
                item(key = "dia-$dia") {
                    EncabezadoGrupo(
                        texto = etiquetaDia(dia),
                        extremo = formatearPesos(gastos.sumOf { it.montoCents }),
                    )
                }
                items(gastos, key = { it.id }) { gasto ->
                    GastoCard(
                        gasto = gasto,
                        esAdmin = estado.esAdmin,
                        onEditar = onEditar,
                        onEliminar = onEliminar,
                        mostrarFecha = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalDelCiclo(
    etiqueta: String,
    totalCents: Long,
    cantidad: Int,
    onVerMeses: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(DegradadoCauce)
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            Column {
                Text(
                    text = "CICLO ACTUAL · $etiqueta",
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 0.8.sp,
                    color = Color.White.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = formatearPesos(totalCents),
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    letterSpacing = (-0.5).sp,
                    color = Color.White,
                    maxLines = 1,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (cantidad == 1) "1 gasto registrado" else "$cantidad gastos registrados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }
        }
        FilledTonalButton(
            onClick = onVerMeses,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
        ) {
            Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ciclos anteriores")
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
        Image(
            painter = painterResource(R.drawable.ic_cauce_symbol),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text("Configura tu hogar", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "Crea un hogar nuevo o únete con un código de invitación.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del hogar") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.crearHogar(nombre) },
            enabled = nombre.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
        ) {
            Text("Crear hogar")
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 28.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it.uppercase() },
            label = { Text("Código de invitación") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.unirse(codigo) },
            enabled = codigo.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
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
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(72.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onReintentar) {
            Text("Reintentar")
        }
    }
}
