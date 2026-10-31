package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import com.andromeda.gastos.domain.util.rangoDelMes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth

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

    fun cargar(hogarId: String, year: Int, month: Int) {
        viewModelScope.launch {
            _state.value = MonthDetailUiState.Cargando
            runCatching {
                val mes = YearMonth.of(year, month)
                val (desde, hasta) = rangoDelMes(mes)
                consultarGastos.listar(hogarId, desde, hasta)
            }.onSuccess {
                _state.value = MonthDetailUiState.Listo(it)
            }.onFailure {
                _state.value = MonthDetailUiState.Error(
                    it.message ?: "No se pudo cargar el mes",
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
