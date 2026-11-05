package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Analisis
import com.andromeda.gastos.domain.entity.CarreraDia
import com.andromeda.gastos.domain.entity.CategoriaAnalisis
import com.andromeda.gastos.domain.entity.Concepto
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.util.Ciclo
import com.andromeda.gastos.domain.util.etiquetaCiclo
import com.andromeda.gastos.domain.util.formatearMonto
import com.andromeda.gastos.domain.util.formatearPesos
import com.andromeda.gastos.infrastructure.ui.components.PantallaCargando
import com.andromeda.gastos.infrastructure.ui.viewmodel.AnalisisViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalisisScreen(
    container: AppContainer,
    hogarId: String,
    onVolver: () -> Unit,
) {
    val vm: AnalisisViewModel = viewModel(factory = AnalisisViewModel.factory(container))
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.cargar(hogarId)
    }

    LaunchedEffect(state.mensaje) {
        val mensaje = state.mensaje ?: return@LaunchedEffect
        snackbar.showSnackbar(mensaje)
        vm.limpiarMensaje()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Análisis") },
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
                .fillMaxSize(),
        ) {
            if (state.ciclos.isNotEmpty()) {
                SelectorCiclos(
                    ciclos = state.ciclos,
                    seleccionado = state.seleccionado,
                    onSeleccionar = { vm.seleccionar(hogarId, it) },
                )
            }

            when {
                state.cargando -> PantallaCargando()
                state.error != null -> ErrorAnalisis(state.error!!) { vm.cargar(hogarId) }
                state.analisis == null -> SinDatos()
                else -> ContenidoAnalisis(
                    analisis = state.analisis!!,
                    onCorregir = vm::abrirCorreccion,
                )
            }
        }
    }

    val terminoEditando = state.terminoEditando
    if (terminoEditando != null) {
        DialogoCorreccion(
            termino = state.nombreEditando ?: terminoEditando,
            conceptos = state.conceptos,
            guardando = state.guardando,
            creandoCategoria = state.creandoCategoria,
            categoriaCreada = state.categoriaCreada,
            errorCategoria = state.errorCategoria,
            onCancelar = vm::cerrarCorreccion,
            onGuardar = { conceptoId, nota ->
                vm.clasificar(hogarId, terminoEditando, conceptoId, nota)
            },
            onCrearCategoria = { nombre -> vm.crearCategoria(hogarId, nombre) },
        )
    }
}

@Composable
private fun SelectorCiclos(
    ciclos: List<ResumenCiclo>,
    seleccionado: Int,
    onSeleccionar: (Int) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(ciclos) { indice, ciclo ->
            FilterChip(
                selected = indice == seleccionado,
                onClick = { onSeleccionar(indice) },
                label = { Text(etiquetaResumen(ciclo)) },
            )
        }
    }
}

@Composable
private fun ContenidoAnalisis(
    analisis: Analisis,
    onCorregir: (termino: String, nombre: String) -> Unit,
) {
    if (analisis.resumen.cantidad == 0) {
        SinDatos()
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ResumenAnalisisCard(analisis) }
        item {
            SeccionCard(titulo = "En qué se fue") {
                Text(
                    text = "Estas categorías se asignan solas y pueden equivocarse. " +
                        "Tócalas para revisarlas y corregirlas si hace falta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CategoriasExpandibles(
                    categorias = analisis.categorias,
                    onCorregir = onCorregir,
                )
            }
        }
        if (analisis.sinClasificar.isNotEmpty()) {
            item {
                SinClasificarCard(
                    items = analisis.sinClasificar,
                    onCorregir = onCorregir,
                )
            }
        }
        if (analisis.gustos.isNotEmpty()) {
            item {
                GusticosCard(analisis, onCorregir)
            }
        }
        item {
            SeccionCard(titulo = "La carrera del ciclo") {
                GraficoCarrera(analisis)
            }
        }
    }
}

@Composable
private fun ResumenAnalisisCard(analisis: Analisis) {
    val resumen = analisis.resumen
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Total del ciclo", style = MaterialTheme.typography.titleSmall)
            Text(
                text = formatearMonto(resumen.totalCents),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${resumen.cantidad} gastos · ${resumen.diasConRegistro} días registrando",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SinClasificarCard(
    items: List<com.andromeda.gastos.domain.entity.SinClasificarAnalisis>,
    onCorregir: (termino: String, nombre: String) -> Unit,
) {
    SeccionCard(titulo = "Sin clasificar") {
        Text(
            text = "No sabemos dónde poner estos gastos. Toca «¿Qué es?» y nos ayudas " +
                "a ubicarlos mejor.",
            style = MaterialTheme.typography.bodySmall,
        )
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.ejemplos.firstOrNull()?.nombre ?: item.termino,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${item.cantidad} ${if (item.cantidad == 1) "gasto" else "gastos"}" +
                            " · ${formatearPesos(item.totalCents)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        onCorregir(
                            item.termino,
                            item.ejemplos.firstOrNull()?.nombre ?: item.termino,
                        )
                    },
                ) {
                    Text("¿Qué es?")
                }
            }
        }
    }
}

@Composable
private fun SeccionCard(
    titulo: String,
    contenido: @Composable () -> Unit,
) {
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}

@Composable
private fun CategoriasExpandibles(
    categorias: List<CategoriaAnalisis>,
    onCorregir: (termino: String, nombre: String) -> Unit,
) {
    var expandidas by remember { mutableStateOf(setOf<String>()) }
    val maximo = (categorias.maxOfOrNull { it.totalCents } ?: 0L).coerceAtLeast(1L)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        categorias.forEach { categoria ->
            val abierta = categoria.categoria in expandidas
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            expandidas = if (abierta) {
                                expandidas - categoria.categoria
                            } else {
                                expandidas + categoria.categoria
                            }
                        }
                        .padding(vertical = 6.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = categoria.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatearPesos(categoria.totalCents),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = if (abierta) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = if (abierta) "Contraer" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((categoria.totalCents.toFloat() / maximo).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }

                if (abierta) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 4.dp),
                    ) {
                        categoria.ejemplos.forEach { ejemplo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onCorregir(ejemplo.termino, ejemplo.nombre) }
                                    .padding(vertical = 4.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "• ${ejemplo.nombre}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = formatearPesos(ejemplo.totalCents),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GusticosCard(
    analisis: Analisis,
    onCorregir: (termino: String, nombre: String) -> Unit,
) {
    val totalCents = analisis.gustos.sumOf { it.totalCents }
    val cantidad = analisis.gustos.sumOf { it.cantidad }
    val items = analisis.gustos
        .flatMap { it.ejemplos }
        .sortedByDescending { it.totalCents }

    SeccionCard(titulo = "Tus gusticos") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = formatearMonto(totalCents),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$cantidad ${if (cantidad == 1) "gustico" else "gusticos"} en este ciclo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (items.isEmpty()) {
                return@Column
            }
            Column {
                items.forEach { ejemplo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCorregir(ejemplo.termino, ejemplo.nombre) }
                            .padding(vertical = 5.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "• ${ejemplo.nombre}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = formatearPesos(ejemplo.totalCents),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoCorreccion(
    termino: String,
    conceptos: List<Concepto>,
    guardando: Boolean,
    creandoCategoria: Boolean,
    categoriaCreada: Concepto?,
    errorCategoria: String?,
    onCancelar: () -> Unit,
    onGuardar: (conceptoId: String, nota: String?) -> Unit,
    onCrearCategoria: (nombre: String) -> Unit,
) {
    var busqueda by remember { mutableStateOf("") }
    var seleccionado by remember { mutableStateOf<String?>(null) }
    var nota by remember { mutableStateOf("") }
    var mostrarCrear by remember { mutableStateOf(false) }
    var nuevaCategoria by remember { mutableStateOf("") }

    val filtrados = remember(busqueda, conceptos) {
        val texto = busqueda.trim().lowercase()
        val base = if (texto.isEmpty()) {
            conceptos
        } else {
            conceptos.filter {
                it.nombre.lowercase().contains(texto) ||
                    it.categoriaNombre.lowercase().contains(texto)
            }
        }
        // Las categorías propias van primero para que sea fácil encontrarlas.
        base.sortedWith(compareBy({ it.categoriaNombre != "Personalizada" }, { it.nombre }))
    }

    LaunchedEffect(categoriaCreada?.id) {
        val nueva = categoriaCreada ?: return@LaunchedEffect
        seleccionado = nueva.id
        mostrarCrear = false
        nuevaCategoria = ""
    }

    Dialog(
        onDismissRequest = onCancelar,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("¿Qué es?") },
                        navigationIcon = {
                            IconButton(onClick = onCancelar) {
                                Icon(Icons.Filled.Close, contentDescription = "Cerrar")
                            }
                        },
                    )
                },
                bottomBar = {
                    Surface(tonalElevation = 3.dp) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .imePadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = nota,
                                onValueChange = { if (it.length <= 240) nota = it },
                                label = { Text("Descripción (opcional)") },
                                supportingText = {
                                    Text("Con una descripción el análisis es más preciso.")
                                },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Button(
                                onClick = {
                                    onGuardar(seleccionado!!, nota.trim().ifBlank { null })
                                },
                                enabled = seleccionado != null && !guardando && !creandoCategoria,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                            ) {
                                if (guardando) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("Guardar")
                                }
                            }
                        }
                    }
                },
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = termino,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Se aplicará a todos los gastos que tengan ese nombre.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        label = { Text("Buscar categoría") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedButton(
                        onClick = { mostrarCrear = !mostrarCrear },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (mostrarCrear) {
                                "Cancelar nueva categoría"
                            } else {
                                "+ Crear mi categoría"
                            },
                        )
                    }

                    if (mostrarCrear) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = nuevaCategoria,
                                onValueChange = { if (it.length <= 40) nuevaCategoria = it },
                                label = { Text("Nombre de la categoría") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            Button(
                                onClick = { onCrearCategoria(nuevaCategoria) },
                                enabled = nuevaCategoria.trim().length >= 2 && !creandoCategoria,
                            ) {
                                if (creandoCategoria) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text("Crear")
                                }
                            }
                        }
                    }

                    if (errorCategoria != null) {
                        Text(
                            text = errorCategoria,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(filtrados, key = { it.id }) { concepto ->
                            val activo = concepto.id == seleccionado
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { seleccionado = concepto.id }
                                    .background(
                                        if (activo) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            Color.Transparent
                                        },
                                    )
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = activo,
                                    onClick = { seleccionado = concepto.id },
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = concepto.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = if (concepto.categoriaNombre == "Personalizada") {
                                            "Mi categoría"
                                        } else {
                                            concepto.categoriaNombre
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GraficoCarrera(analisis: Analisis) {
    val actual = analisis.carrera.find { it.etiqueta == "actual" }?.dias.orEmpty()
    val anterior = analisis.carrera.find { it.etiqueta == "anterior" }?.dias.orEmpty()

    if (actual.isEmpty() && anterior.isEmpty()) return

    val teal = MaterialTheme.colorScheme.primary
    val gris = Color(0xFF9AA3AF)
    val maximo = ((actual + anterior).maxOfOrNull { it.acumuladoCents } ?: 0L).coerceAtLeast(1L)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val ancho = size.width
        val alto = size.height
        val pad = 8.dp.toPx()

        fun puntos(dias: List<CarreraDia>): List<Offset> {
            if (dias.isEmpty()) return emptyList()
            val ultimoIndice = (dias.size - 1).coerceAtLeast(1)
            return dias.mapIndexed { i, d ->
                val x = pad + (ancho - 2 * pad) * (i.toFloat() / ultimoIndice)
                val y = alto - pad - (alto - 2 * pad) * (d.acumuladoCents.toFloat() / maximo)
                Offset(x, y)
            }
        }

        drawLine(
            color = gris.copy(alpha = 0.3f),
            start = Offset(pad, alto - pad),
            end = Offset(ancho - pad, alto - pad),
            strokeWidth = 2f,
        )

        val puntosAnterior = puntos(anterior)
        if (puntosAnterior.size >= 2) {
            val path = Path().apply {
                moveTo(puntosAnterior[0].x, puntosAnterior[0].y)
                puntosAnterior.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, gris, style = Stroke(width = 2.5f))
        }

        val puntosActual = puntos(actual)
        if (puntosActual.size >= 2) {
            val path = Path().apply {
                moveTo(puntosActual[0].x, puntosActual[0].y)
                puntosActual.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, teal, style = Stroke(width = 3f))
            drawCircle(teal, radius = 4.dp.toPx(), center = puntosActual.last())
        }
    }

    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Leyenda("Este ciclo", teal)
        Leyenda("Ciclo anterior", gris)
    }
}

@Composable
private fun Leyenda(etiqueta: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(etiqueta, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SinDatos() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Todavía no hay gastos en este ciclo.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ErrorAnalisis(mensaje: String, onReintentar: () -> Unit) {
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

private fun etiquetaResumen(r: ResumenCiclo): String = runCatching {
    etiquetaCiclo(Ciclo(LocalDate.parse(r.inicio), LocalDate.parse(r.fin)))
}.getOrDefault(r.inicio)
