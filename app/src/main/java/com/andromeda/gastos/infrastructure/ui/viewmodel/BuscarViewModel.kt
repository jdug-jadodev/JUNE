package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import com.andromeda.gastos.infrastructure.ui.components.normalizarBusqueda
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonaFiltro(val id: String, val nombre: String)

data class BuscarUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val todos: List<Gasto> = emptyList(),
    val personas: List<PersonaFiltro> = emptyList(),
    val texto: String = "",
    val personaId: String? = null,
) {
    val resultados: List<Gasto>
        get() {
            val consulta = normalizarBusqueda(texto)
            return todos.filter { gasto ->
                (personaId == null || gasto.creadoPor == personaId) &&
                    (consulta.isEmpty() || normalizarBusqueda(gasto.producto).contains(consulta))
            }
        }

    val filtrando: Boolean get() = texto.isNotBlank() || personaId != null
}

class BuscarViewModel(
    private val consultarGastos: ConsultarGastosPort,
) : ViewModel() {

    private val _state = MutableStateFlow(BuscarUiState())
    val state = _state.asStateFlow()

    private var hogarCargado: String? = null

    fun cargar(hogarId: String, forzar: Boolean = false) {
        if (!forzar && hogarCargado == hogarId) return
        hogarCargado = hogarId
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            runCatching { consultarGastos.listar(hogarId) }
                .onSuccess { gastos ->
                    val ordenados = gastos.sortedWith(
                        compareByDescending<Gasto> { it.fecha }.thenByDescending { it.createdAt },
                    )
                    val personas = ordenados
                        .groupBy { it.creadoPor }
                        .map { (id, lista) -> PersonaFiltro(id, lista.first().creadoPorNombre) }
                        .sortedBy { it.nombre.lowercase() }
                    _state.update {
                        it.copy(cargando = false, todos = ordenados, personas = personas)
                    }
                }
                .onFailure { error ->
                    hogarCargado = null
                    _state.update {
                        it.copy(cargando = false, error = error.message ?: "No se pudieron cargar los gastos")
                    }
                }
        }
    }

    fun cambiarTexto(texto: String) {
        _state.update { it.copy(texto = texto) }
    }

    fun elegirPersona(id: String?) {
        _state.update { it.copy(personaId = if (it.personaId == id) null else id) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { BuscarViewModel(consultarGastos = container.consultarGastos) }
        }
    }
}
