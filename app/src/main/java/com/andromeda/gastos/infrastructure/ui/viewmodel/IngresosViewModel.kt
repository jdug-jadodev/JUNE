package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Ingreso
import com.andromeda.gastos.domain.entity.Periodicidad
import com.andromeda.gastos.domain.port.portin.GestionarIngresosPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngresosUiState(
    val cargando: Boolean = true,
    val ingresos: List<Ingreso> = emptyList(),
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null,
)

class IngresosViewModel(
    private val gestionarIngresos: GestionarIngresosPort,
) : ViewModel() {

    private val _state = MutableStateFlow(IngresosUiState())
    val state = _state.asStateFlow()

    fun cargar(hogarId: String) {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            runCatching { gestionarIngresos.listar(hogarId) }
                .onSuccess { ingresos ->
                    _state.update { it.copy(cargando = false, ingresos = ingresos) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(cargando = false, error = error.message ?: "No se pudo cargar")
                    }
                }
        }
    }

    fun crear(hogarId: String, nombre: String?, montoCents: Long, periodicidad: Periodicidad) {
        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null, mensaje = null) }
            runCatching { gestionarIngresos.crear(hogarId, nombre, montoCents, periodicidad) }
                .onSuccess {
                    _state.update { it.copy(guardando = false, mensaje = "Ingreso guardado") }
                    cargar(hogarId)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(guardando = false, error = error.message ?: "No se pudo guardar")
                    }
                }
        }
    }

    fun actualizar(
        hogarId: String,
        ingresoId: String,
        nombre: String?,
        montoCents: Long,
        periodicidad: Periodicidad,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null, mensaje = null) }
            runCatching {
                gestionarIngresos.actualizar(hogarId, ingresoId, nombre, montoCents, periodicidad)
            }
                .onSuccess {
                    _state.update { it.copy(guardando = false, mensaje = "Ingreso actualizado") }
                    cargar(hogarId)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(guardando = false, error = error.message ?: "No se pudo actualizar")
                    }
                }
        }
    }

    fun eliminar(hogarId: String, ingresoId: String) {
        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null, mensaje = null) }
            runCatching { gestionarIngresos.eliminar(hogarId, ingresoId) }
                .onSuccess {
                    _state.update { it.copy(guardando = false, mensaje = "Ingreso eliminado") }
                    cargar(hogarId)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(guardando = false, error = error.message ?: "No se pudo eliminar")
                    }
                }
        }
    }

    fun limpiarMensaje() = _state.update { it.copy(mensaje = null, error = null) }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                IngresosViewModel(
                    gestionarIngresos = container.gestionarIngresos,
                )
            }
        }
    }
}
