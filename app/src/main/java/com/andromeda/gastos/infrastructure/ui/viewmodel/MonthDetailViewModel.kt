package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MonthDetailUiState {
    data object Cargando : MonthDetailUiState
    data class Listo(val gastos: List<Gasto>) : MonthDetailUiState
    data class Error(val mensaje: String) : MonthDetailUiState
}

class MonthDetailViewModel(
    private val consultarGastos: ConsultarGastosPort,
) : ViewModel() {

    private val _state = MutableStateFlow<MonthDetailUiState>(MonthDetailUiState.Cargando)
    val state = _state.asStateFlow()

    fun cargar(hogarId: String, inicio: String, fin: String) {
        viewModelScope.launch {
            _state.value = MonthDetailUiState.Cargando
            runCatching { consultarGastos.listar(hogarId, inicio, fin) }
                .onSuccess {
                    _state.value = MonthDetailUiState.Listo(it)
                }
                .onFailure {
                    _state.value = MonthDetailUiState.Error(
                        it.message ?: "No se pudo cargar el ciclo",
                    )
                }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MonthDetailViewModel(consultarGastos = container.consultarGastos)
            }
        }
    }
}
