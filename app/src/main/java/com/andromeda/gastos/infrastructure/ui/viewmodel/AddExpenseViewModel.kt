package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.port.portin.RegistrarGastoPorVozPort
import com.andromeda.gastos.domain.port.portin.RegistrarGastoPort
import com.andromeda.gastos.domain.util.MontoParser
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddExpenseUiState(
    val producto: String = "",
    val monto: String = "",
    val fecha: String = LocalDate.now().toString(),
    val guardando: Boolean = false,
    val error: String? = null,
    val guardado: Boolean = false,
    val vozActiva: Boolean = false,
    val mensajeVoz: String? = null,
)

class AddExpenseViewModel(
    private val registrarGasto: RegistrarGastoPort,
    private val registrarGastoPorVoz: RegistrarGastoPorVozPort,
) : ViewModel() {

    private val _state = MutableStateFlow(AddExpenseUiState())
    val state = _state.asStateFlow()

    fun onProducto(valor: String) = _state.update { it.copy(producto = valor, error = null) }
    fun onMonto(valor: String) = _state.update { it.copy(monto = valor, error = null) }
    fun onFecha(valor: String) = _state.update { it.copy(fecha = valor, error = null) }

    fun guardar(hogarId: String) {
        val actual = _state.value
        val montoCents = MontoParser.aCentavos(actual.monto)
        if (montoCents == null) {
            _state.update { it.copy(error = "Monto inválido. Ejemplo: 35000 o 35.000") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null) }
            runCatching {
                registrarGasto.ejecutar(hogarId, actual.producto, montoCents, actual.fecha)
            }.onSuccess {
                _state.update { it.copy(guardando = false, guardado = true) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        guardando = false,
                        error = error.message ?: "No se pudo registrar el gasto",
                    )
                }
            }
        }
    }

    fun iniciarVoz(hogarId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(vozActiva = true, error = null, mensajeVoz = "Escuchando…")
            }
            runCatching {
                registrarGastoPorVoz.ejecutar(hogarId) { mensaje ->
                    _state.update { it.copy(mensajeVoz = mensaje) }
                }
            }.onSuccess { gasto ->
                _state.update {
                    it.copy(
                        vozActiva = false,
                        guardado = gasto != null,
                        mensajeVoz = if (gasto != null) "Gasto registrado por voz" else null,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        vozActiva = false,
                        error = error.message ?: "No se pudo usar la voz",
                    )
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AddExpenseViewModel(
                    registrarGasto = container.registrarGasto,
                    registrarGastoPorVoz = container.registrarGastoPorVoz,
                )
            }
        }
    }
}
