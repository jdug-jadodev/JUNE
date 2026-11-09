package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Analisis
import com.andromeda.gastos.domain.entity.AnalisisIA
import com.andromeda.gastos.domain.entity.Concepto
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.port.portin.AnalizarConIAPort
import com.andromeda.gastos.domain.port.portin.ClasificarTerminoPort
import com.andromeda.gastos.domain.port.portin.ConsultarAnalisisPort
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import com.andromeda.gastos.domain.port.portin.GestionarCategoriasPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalisisUiState(
    val cargando: Boolean = true,
    val ciclos: List<ResumenCiclo> = emptyList(),
    val seleccionado: Int = 0,
    val analisis: Analisis? = null,
    val conceptos: List<Concepto> = emptyList(),
    val terminoEditando: String? = null,
    val nombreEditando: String? = null,
    val guardando: Boolean = false,
    val creandoCategoria: Boolean = false,
    val categoriaCreada: Concepto? = null,
    val errorCategoria: String? = null,
    val mensaje: String? = null,
    val error: String? = null,
    val iaCargando: Boolean = false,
    val iaAnalisis: AnalisisIA? = null,
    val iaError: String? = null,
)

class AnalisisViewModel(
    private val consultarAnalisis: ConsultarAnalisisPort,
    private val consultarGastos: ConsultarGastosPort,
    private val clasificarTermino: ClasificarTerminoPort,
    private val gestionarCategorias: GestionarCategoriasPort,
    private val analizarConIA: AnalizarConIAPort,
) : ViewModel() {

    private val _state = MutableStateFlow(AnalisisUiState())
    val state = _state.asStateFlow()

    fun cargar(hogarId: String) {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            runCatching {
                val ciclos = consultarGastos.listarCiclos(hogarId)
                val base = if (_state.value.conceptos.isEmpty()) {
                    consultarAnalisis.conceptos()
                } else {
                    _state.value.conceptos.filterNot { it.id.startsWith("cat:") }
                }
                val personalizadas = gestionarCategorias.listar(hogarId)
                ciclos to (base + personalizadas)
            }
                .onSuccess { (ciclos, conceptos) ->
                    _state.update {
                        it.copy(cargando = false, ciclos = ciclos, conceptos = conceptos)
                    }
                    if (ciclos.isNotEmpty()) analizar(hogarId, 0)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(cargando = false, error = error.message ?: "No se pudo cargar")
                    }
                }
        }
    }

    fun seleccionar(hogarId: String, indice: Int) {
        _state.update { it.copy(seleccionado = indice) }
        analizar(hogarId, indice)
    }

    fun abrirCorreccion(termino: String, nombre: String) {
        _state.update {
            it.copy(
                terminoEditando = termino,
                nombreEditando = nombre,
                errorCategoria = null,
                categoriaCreada = null,
            )
        }
    }

    fun cerrarCorreccion() {
        _state.update {
            it.copy(
                terminoEditando = null,
                nombreEditando = null,
                errorCategoria = null,
                categoriaCreada = null,
            )
        }
    }

    fun crearCategoria(hogarId: String, nombre: String) {
        val limpio = nombre.trim()
        if (limpio.length < 2) {
            _state.update { it.copy(errorCategoria = "Escribe un nombre de al menos 2 letras") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(creandoCategoria = true, errorCategoria = null) }
            runCatching { gestionarCategorias.crear(hogarId, limpio) }
                .onSuccess { categoria ->
                    _state.update {
                        it.copy(
                            creandoCategoria = false,
                            categoriaCreada = categoria,
                            conceptos = it.conceptos.filterNot { c -> c.id == categoria.id } + categoria,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            creandoCategoria = false,
                            errorCategoria = error.message ?: "No se pudo crear la categoría",
                        )
                    }
                }
        }
    }

    fun limpiarMensaje() {
        _state.update { it.copy(mensaje = null) }
    }

    fun generarAnalisisIA(hogarId: String) {
        val inicio = _state.value.ciclos.getOrNull(_state.value.seleccionado)?.inicio
        viewModelScope.launch {
            _state.update { it.copy(iaCargando = true, iaError = null, iaAnalisis = null) }
            runCatching { analizarConIA.analizarConIA(hogarId, inicio?.let { listOf(it) }) }
                .onSuccess { resultado ->
                    _state.update { it.copy(iaCargando = false, iaAnalisis = resultado) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(iaCargando = false, iaError = error.message ?: "No se pudo analizar")
                    }
                }
        }
    }

    fun clasificar(hogarId: String, termino: String, conceptoId: String, nota: String?) {
        viewModelScope.launch {
            _state.update { it.copy(guardando = true) }
            runCatching { clasificarTermino.clasificar(hogarId, termino, conceptoId, nota) }
                .onSuccess {
                    val nombre = _state.value.nombreEditando ?: termino
                    _state.update {
                        it.copy(
                            guardando = false,
                            terminoEditando = null,
                            nombreEditando = null,
                            mensaje = "Listo. Ahora «$nombre» entra en tu análisis.",
                        )
                    }
                    analizar(hogarId, _state.value.seleccionado)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            guardando = false,
                            mensaje = error.message ?: "No se pudo guardar",
                        )
                    }
                }
        }
    }

    private fun analizar(hogarId: String, indice: Int) {
        val inicio = _state.value.ciclos.getOrNull(indice)?.inicio ?: return
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            runCatching { consultarAnalisis.analizar(hogarId, listOf(inicio)) }
                .onSuccess { analisis ->
                    _state.update { it.copy(cargando = false, analisis = analisis) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(cargando = false, error = error.message ?: "No se pudo analizar")
                    }
                }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AnalisisViewModel(
                    consultarAnalisis = container.consultarAnalisis,
                    consultarGastos = container.consultarGastos,
                    clasificarTermino = container.clasificarTermino,
                    gestionarCategorias = container.gestionarCategorias,
                    analizarConIA = container.analizarConIA,
                )
            }
        }
    }
}
