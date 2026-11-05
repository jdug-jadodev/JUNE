package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Analisis
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.port.portin.ConsultarAnalisisPort
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalisisUiState(
    val cargando: Boolean = true,
    val ciclos: List<ResumenCiclo> = emptyList(),
    val seleccionado: Int = 0,
    val analisis: Analisis? = null,
    val error: String? = null,
)

class AnalisisViewModel(
    private val consultarAnalisis: ConsultarAnalisisPort,
    private val consultarGastos: ConsultarGastosPort,
) : ViewModel() {

    private val _state = MutableStateFlow(AnalisisUiState())
    val state = _state.asStateFlow()

    fun cargar(hogarId: String) {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            runCatching { consultarGastos.listarCiclos(hogarId) }
                .onSuccess { ciclos ->
                    _state.update { it.copy(cargando = false, ciclos = ciclos) }
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
                )
            }
        }
    }
}
