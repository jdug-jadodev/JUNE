package com.andromeda.gastos.infrastructure.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.AnalisisIA
import com.andromeda.gastos.domain.entity.ProgresoCicloIA
import com.andromeda.gastos.domain.entity.SugerenciaIA
import com.andromeda.gastos.domain.util.Ciclo
import com.andromeda.gastos.domain.util.etiquetaCiclo
import com.andromeda.gastos.domain.util.formatearPesos
import com.andromeda.gastos.infrastructure.ui.components.Manrope
import com.andromeda.gastos.infrastructure.ui.viewmodel.AnalisisIAViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

/** Colores de marca usados en el degradado principal (mismos tonos del tema). */
private val TealMarca = Color(0xFF0F766E)
private val NavyMarca = Color(0xFF0B1F3A)
private val DegradadoMarca = Brush.linearGradient(listOf(TealMarca, NavyMarca))

private val PasosAnalisis = listOf(
    "Revisando los gastos del ciclo",
    "Comparando con tu ingreso",
    "Buscando patrones por categoría",
    "Calculando montos sugeridos",
    "Preparando tus consejos",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalisisIAScreen(
    container: AppContainer,
    hogarId: String,
    inicio: String?,
    fin: String?,
    onVolver: () -> Unit,
) {
    val vm: AnalisisIAViewModel = viewModel(
        key = "analisis-ia-$hogarId-$inicio",
        factory = AnalisisIAViewModel.factory(container),
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val etiqueta = remember(inicio, fin) { etiquetaPeriodo(inicio, fin) }

    LaunchedEffect(hogarId, inicio) { vm.iniciar(hogarId, inicio) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Análisis con IA",
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (etiqueta != null) {
                            Text(
                                text = etiqueta,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        val fase = when {
            state.cargando -> FaseIA.Cargando
            state.analisis != null -> FaseIA.Resultado
            state.error != null -> FaseIA.Error
            else -> FaseIA.Cargando
        }
        AnimatedContent(
            targetState = fase,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(200)) },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            label = "fase-ia",
        ) { actual ->
            when (actual) {
                FaseIA.Cargando -> PantallaAnalizando()
                FaseIA.Error -> ErrorIA(
                    mensaje = state.error.orEmpty(),
                    onReintentar = { vm.analizar(hogarId, inicio) },
                )
                FaseIA.Resultado -> state.analisis?.let { analisis ->
                    ResultadoIA(
                        analisis = analisis,
                        etiqueta = etiqueta,
                        personasLocales = state.personasLocales,
                        onEditarPersonas = vm::abrirDialogoPersonas,
                        onReanalizar = { vm.analizar(hogarId, inicio) },
                    )
                }
            }
        }

        if (state.dialogoPersonas) {
            val personas = state.analisis?.personasEstimadas?.takeIf { it in 1..15 }
                ?: state.personasLocales
                ?: 4
            DialogoPersonas(
                valorActual = personas,
                esAjuste = state.personasLocales != null,
                onGuardar = { vm.guardarPersonas(hogarId, inicio, it) },
                onAutomatico = { vm.guardarPersonas(hogarId, inicio, null) },
                onCerrar = vm::cerrarDialogoPersonas,
            )
        }
    }
}

private enum class FaseIA { Cargando, Resultado, Error }

// ---------------------------------------------------------------------------
// Estado de carga
// ---------------------------------------------------------------------------

@Composable
private fun PantallaAnalizando() {
    var paso by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (paso < PasosAnalisis.lastIndex) {
            delay(1400)
            paso++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        OrbeIA(tamano = 132)
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Analizando tu ciclo",
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Esto toma unos segundos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PasosAnalisis.forEachIndexed { indice, texto ->
                PasoAnalisis(
                    texto = texto,
                    estado = when {
                        indice < paso -> EstadoPaso.Hecho
                        indice == paso -> EstadoPaso.Activo
                        else -> EstadoPaso.Pendiente
                    },
                )
            }
        }
    }
}

private enum class EstadoPaso { Hecho, Activo, Pendiente }

@Composable
private fun PasoAnalisis(texto: String, estado: EstadoPaso) {
    val alfa by animateFloatAsState(
        targetValue = if (estado == EstadoPaso.Pendiente) 0.4f else 1f,
        animationSpec = tween(300),
        label = "alfa-paso",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier.size(22.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (estado) {
                EstadoPaso.Hecho -> Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                }
                EstadoPaso.Activo -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                EstadoPaso.Pendiente -> Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (estado == EstadoPaso.Activo) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = alfa),
        )
    }
}

@Composable
private fun OrbeIA(tamano: Int) {
    val transicion = rememberInfiniteTransition(label = "orbe")
    val giro by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "giro",
    )
    val pulso by transicion.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "pulso",
    )
    val acento = MaterialTheme.colorScheme.secondary
    val primario = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.size(tamano.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(giro),
        ) {
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(Color.Transparent, primario, acento, Color.Transparent),
                ),
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Box(
            modifier = Modifier
                .size((tamano - 28).dp)
                .scale(pulso)
                .clip(CircleShape)
                .background(DegradadoMarca),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "IA",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                letterSpacing = 1.sp,
                color = Color.White,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Resultado
// ---------------------------------------------------------------------------

@Composable
private fun ResultadoIA(
    analisis: AnalisisIA,
    etiqueta: String?,
    personasLocales: Int?,
    onEditarPersonas: () -> Unit,
    onReanalizar: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { HeroCiclo(analisis.progreso, etiqueta) }

        item {
            LoQueAnalice(
                analisis = analisis,
                etiqueta = etiqueta,
                personasLocales = personasLocales,
                onEditarPersonas = onEditarPersonas,
            )
        }

        if (analisis.estado.isNotBlank()) {
            item {
                Seccion(titulo = "Diagnóstico") {
                    TarjetaAcento(
                        icono = Icons.Outlined.Insights,
                        colorAcento = MaterialTheme.colorScheme.primary,
                        texto = analisis.estado,
                    )
                }
            }
        }

        if (analisis.sugerencias.isNotEmpty()) {
            item {
                Seccion(
                    titulo = "Montos sugeridos",
                    subtitulo = "Lo que la IA recomienda destinar por categoría",
                ) {
                    ListaSugerencias(analisis.sugerencias)
                }
            }
        }

        if (analisis.recomendacion.isNotBlank()) {
            item {
                Seccion(titulo = "Consejo clave") {
                    ConsejoClave(analisis.recomendacion)
                }
            }
        }

        item { PieResultado(cacheado = analisis.cacheado, onReanalizar = onReanalizar) }
    }
}

@Composable
private fun HeroCiclo(progreso: ProgresoCicloIA?, etiqueta: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(DegradadoMarca)
            .padding(22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "TU CICLO",
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.5.sp,
                    color = Color.White.copy(alpha = 0.75f),
                )
            }

            if (progreso == null) {
                Text(
                    text = etiqueta ?: "Análisis listo",
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White,
                )
                return@Column
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Has gastado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                    Text(
                        text = formatearPesos(progreso.totalCents),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp,
                        color = Color.White,
                        maxLines = 1,
                    )
                    Text(
                        text = if (progreso.diasRestantes > 0) {
                            "Faltan ${progreso.diasRestantes} días para cerrar"
                        } else {
                            "Ciclo cerrado"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
                Spacer(Modifier.width(12.dp))
                AnilloDias(progreso)
            }

            if (progreso.ingresoMensualCents > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fraccion = (progreso.porcentajeIngreso / 100f).coerceIn(0f, 1f)
                    val animada by animateFloatAsState(
                        targetValue = fraccion,
                        animationSpec = tween(900, easing = FastOutSlowInEasing),
                        label = "barra-ingreso",
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Del ingreso mensual",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                        Text(
                            text = "${progreso.porcentajeIngreso}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animada)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondary),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnilloDias(progreso: ProgresoCicloIA) {
    val fraccion = if (progreso.diasTotales > 0) {
        (progreso.diasTranscurridos.toFloat() / progreso.diasTotales).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animada by animateFloatAsState(
        targetValue = fraccion,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "anillo-dias",
    )
    val acento = MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier.size(84.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val grosor = 7.dp.toPx()
            drawArc(
                color = Color.White.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = grosor),
            )
            drawArc(
                color = acento,
                startAngle = -90f,
                sweepAngle = 360f * animada,
                useCenter = false,
                style = Stroke(width = grosor, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${progreso.diasTranscurridos}",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
            )
            Text(
                text = "de ${progreso.diasTotales} días",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun LoQueAnalice(
    analisis: AnalisisIA,
    etiqueta: String?,
    personasLocales: Int?,
    onEditarPersonas: () -> Unit,
) {
    val progreso = analisis.progreso
    val personas = analisis.personasEstimadas.takeIf { it in 1..15 } ?: personasLocales ?: 0
    val datos = buildList {
        add(DatoAnalizado(Icons.Outlined.CalendarMonth, "Periodo", etiqueta ?: "Ciclo actual"))
        if (progreso != null) {
            add(
                DatoAnalizado(
                    Icons.Outlined.Payments,
                    "Gastos revisados",
                    formatearPesos(progreso.totalCents),
                ),
            )
            if (progreso.ingresoMensualCents > 0) {
                add(
                    DatoAnalizado(
                        Icons.Outlined.AccountBalanceWallet,
                        "Ingreso mensual",
                        formatearPesos(progreso.ingresoMensualCents),
                    ),
                )
            }
        }
        if (analisis.margenLibreCents > 0) {
            add(
                DatoAnalizado(
                    Icons.Outlined.Savings,
                    "Queda tras los fijos",
                    formatearPesos(analisis.margenLibreCents),
                ),
            )
        }
        if (analisis.salariosMinimos > 0) {
            add(
                DatoAnalizado(
                    Icons.Outlined.AccountBalance,
                    "Nivel de ingreso",
                    "${analisis.salariosMinimos} salarios mínimos",
                ),
            )
        }
        if (analisis.sugerencias.isNotEmpty()) {
            add(
                DatoAnalizado(
                    Icons.Outlined.Category,
                    "Categorías evaluadas",
                    "${analisis.sugerencias.size}",
                ),
            )
        }
    }

    Seccion(titulo = "Lo que analicé") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            datos.chunked(2).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    fila.forEach { dato ->
                        TileDato(dato, Modifier.weight(1f))
                    }
                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            if (personas > 0) {
                FilaPersonas(
                    personas = personas,
                    ajustado = personasLocales != null,
                    onEditar = onEditarPersonas,
                )
            }
        }
    }
}

private data class DatoAnalizado(val icono: ImageVector, val etiqueta: String, val valor: String)

@Composable
private fun TileDato(dato: DatoAnalizado, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(18.dp),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = dato.icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = dato.etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = dato.valor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun FilaPersonas(
    personas: Int,
    ajustado: Boolean,
    onEditar: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onEditar),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(18.dp),
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Personas en casa",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (ajustado) {
                        "Ajustado por ti · solo en este teléfono"
                    } else {
                        "Deducido de tus gastos · toca para ajustar"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = personas.toString(),
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Editar personas en casa",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun Seccion(
    titulo: String,
    subtitulo: String? = null,
    contenido: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = titulo,
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitulo != null) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        contenido()
    }
}

@Composable
private fun TarjetaAcento(icono: ImageVector, colorAcento: Color, texto: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(20.dp),
                )
                .padding(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorAcento.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorAcento,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ListaSugerencias(sugerencias: List<SugerenciaIA>) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            Modifier.border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp),
            ),
        ) {
            sugerencias.forEachIndexed { indice, s ->
                if (indice > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 70.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )
                }
                FilaSugerencia(s)
            }
        }
    }
}

@Composable
private fun FilaSugerencia(s: SugerenciaIA) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = s.categoria.trim().take(1).uppercase(),
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = s.categoria,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (s.nota.isNotBlank()) {
                Text(
                    text = s.nota,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatearPesos(s.montoCents),
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (s.periodicidad == "MENSUAL") "al mes" else "a la semana",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConsejoClave(texto: String) {
    val dorado = MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(dorado.copy(alpha = 0.14f))
            .border(1.dp, dorado.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(dorado),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PieResultado(cacheado: Boolean, onReanalizar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onReanalizar,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Volver a analizar")
        }
        Text(
            text = buildString {
                append("Generado con IA a partir de tus gastos. Úsalo como guía.")
                if (cacheado) append("\nMostrando un análisis reciente guardado.")
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ---------------------------------------------------------------------------
// Personas en casa (ajuste local)
// ---------------------------------------------------------------------------

@Composable
private fun DialogoPersonas(
    valorActual: Int,
    esAjuste: Boolean,
    onGuardar: (Int) -> Unit,
    onAutomatico: () -> Unit,
    onCerrar: () -> Unit,
) {
    var valor by remember(valorActual) { mutableIntStateOf(valorActual.coerceIn(1, 15)) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                text = "Personas en casa",
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Las deducimos de tus gastos. Si no coincide, ajústalo: " +
                        "los montos sugeridos se recalculan para esa cantidad.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { if (valor > 1) valor-- },
                        enabled = valor > 1,
                    ) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Quitar una persona")
                    }
                    Text(
                        text = valor.toString(),
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 56.dp),
                    )
                    IconButton(
                        onClick = { if (valor < 15) valor++ },
                        enabled = valor < 15,
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Agregar una persona")
                    }
                }
                Text(
                    text = "Solo se guarda en este teléfono, no en la nube.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (esAjuste) {
                    TextButton(onClick = onAutomatico) {
                        Text("Usar la deducción automática")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(valor) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        },
    )
}

// ---------------------------------------------------------------------------
// Error
// ---------------------------------------------------------------------------

@Composable
private fun ErrorIA(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "No pudimos completar el análisis",
            fontFamily = Manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onReintentar, shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

private fun etiquetaPeriodo(inicio: String?, fin: String?): String? {
    if (inicio == null || fin == null) return null
    return runCatching {
        etiquetaCiclo(Ciclo(LocalDate.parse(inicio), LocalDate.parse(fin)))
    }.getOrNull()
}
