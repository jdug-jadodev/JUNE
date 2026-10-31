package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.ResumenMes
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MonthsUiState {
    data object Cargando : MonthsUiState
    data class Listo(val meses: List<ResumenMes>) : MonthsUiState
    data class Error(val mensaje: String) : MonthsUiState
}

class MonthsViewModel(
    private val consultarGastos: ConsultarGastosPort,
) : ViewModel() {

    private val _state = MutableStateFlow<MonthsUiState>(MonthsUiState.Cargando)
    val state = _state.asStateFlow()

    fun cargar(hogarId: String) {
        viewModelScope.launch {
            _state.value = MonthsUiState.Cargando
            runCatching { consultarGastos.listarMeses(hogarId) }
                .onSuccess { _state.value = MonthsUiState.Listo(it) }
                .onFailure {
                    _state.value = MonthsUiState.Error(
                        it.message ?: "No se pudieron cargar los meses",
                    )
                }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MonthsViewModel(consultarGastos = container.consultarGastos)
            }
        }
    }
}
