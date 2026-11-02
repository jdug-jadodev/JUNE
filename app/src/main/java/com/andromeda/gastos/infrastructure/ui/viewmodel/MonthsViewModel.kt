package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import com.andromeda.gastos.domain.port.portin.GestionarHogarPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MonthsUiState {
    data object Cargando : MonthsUiState
    data class Listo(
        val ciclos: List<ResumenCiclo>,
        val cutoffDay: Int,
    ) : MonthsUiState

    data class Error(val mensaje: String) : MonthsUiState
}

class MonthsViewModel(
    private val consultarGastos: ConsultarGastosPort,
    private val gestionarHogar: GestionarHogarPort,
) : ViewModel() {

    private val _state = MutableStateFlow<MonthsUiState>(MonthsUiState.Cargando)
    val state = _state.asStateFlow()

    fun cargar(hogarId: String) {
        viewModelScope.launch {
            _state.value = MonthsUiState.Cargando
            runCatching {
                val hogar = gestionarHogar.obtener(hogarId)
                hogar.cutoffDay to consultarGastos.listarCiclos(hogarId)
            }.onSuccess { (cutoffDay, ciclos) ->
                _state.value = MonthsUiState.Listo(ciclos = ciclos, cutoffDay = cutoffDay)
            }.onFailure {
                _state.value = MonthsUiState.Error(
                    it.message ?: "No se pudieron cargar los ciclos",
                )
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MonthsViewModel(
                    consultarGastos = container.consultarGastos,
                    gestionarHogar = container.gestionarHogar,
                )
            }
        }
    }
}
