package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.enums.Acceso
import com.andromeda.gastos.domain.enums.Rol
import com.andromeda.gastos.domain.port.portin.ActualizarGastoPort
import com.andromeda.gastos.domain.port.portin.AutenticacionPort
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import com.andromeda.gastos.domain.port.portin.EliminarGastoPort
import com.andromeda.gastos.domain.port.portin.GestionarHogarPort
import com.andromeda.gastos.domain.port.portin.GestionarMiembrosPort
import com.andromeda.gastos.domain.util.cicloActual
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Cargando : HomeUiState
    data object SinHogar : HomeUiState
    data class Listo(
        val hogar: Hogar,
        val gastos: List<Gasto>,
        val esAdmin: Boolean,
        val puedeEscribir: Boolean,
    ) : HomeUiState

    data class Error(val mensaje: String) : HomeUiState
}

class HomeViewModel(
    private val autenticacion: AutenticacionPort,
    private val gestionarHogar: GestionarHogarPort,
    private val gestionarMiembros: GestionarMiembrosPort,
    private val consultarGastos: ConsultarGastosPort,
    private val eliminarGasto: EliminarGastoPort,
    private val actualizarGasto: ActualizarGastoPort,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Cargando)
    val state = _state.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    private var polling: Job? = null

    @Volatile
    private var activo = true

    fun cargar() {
        activo = true
        polling?.cancel()
        viewModelScope.launch {
            if (!activo) return@launch
            _state.value = HomeUiState.Cargando
            try {
                val hogares = gestionarHogar.listar()
                if (!activo) return@launch
                if (hogares.isEmpty()) {
                    _state.value = HomeUiState.SinHogar
                } else {
                    observar(hogares.first())
                }
            } catch (error: Exception) {
                _state.value = HomeUiState.Error(error.message ?: "No se pudieron cargar los datos")
            }
        }
    }

    fun detener() {
        activo = false
        polling?.cancel()
        polling = null
    }

    private fun observar(hogar: Hogar) {
        if (!activo) return
        polling?.cancel()
        polling = viewModelScope.launch {
            if (!activo) return@launch
            try {
                val sesion = autenticacion.sesionActual()
                val miembros = gestionarMiembros.listar(hogar.id)
                val miembroActual = miembros.firstOrNull {
                    it.usuarioId == sesion?.usuario?.id
                }
                val esAdmin = miembroActual?.rol == Rol.ADMIN
                val puedeEscribir = esAdmin || miembroActual?.acceso == Acceso.ESCRITURA

                val ciclo = cicloActual(hogar.cutoffDay)

                consultarGastos.observar(hogar.id, ciclo.inicioIso, ciclo.finIso)
                    .catch { error ->
                        _state.value = HomeUiState.Error(error.message ?: "Error de conexión")
                    }
                    .collect { gastos ->
                        if (activo) {
                            _state.value = HomeUiState.Listo(
                                hogar = hogar,
                                gastos = gastos,
                                esAdmin = esAdmin,
                                puedeEscribir = puedeEscribir,
                            )
                        }
                    }
            } catch (error: Exception) {
                _state.value = HomeUiState.Error(error.message ?: "Error de conexión")
            }
        }
    }

    fun crearHogar(nombre: String) {
        viewModelScope.launch {
            try {
                gestionarHogar.crear(nombre)
                cargar()
            } catch (error: Exception) {
                _mensaje.value = error.message ?: "No se pudo crear el hogar"
            }
        }
    }

    fun unirse(codigo: String) {
        viewModelScope.launch {
            try {
                gestionarMiembros.unirse(codigo)
                cargar()
            } catch (error: Exception) {
                _mensaje.value = error.message ?: "No se pudo unir al hogar"
            }
        }
    }

    fun eliminar(gastoId: String, password: String) {
        val hogar = (_state.value as? HomeUiState.Listo)?.hogar ?: return
        viewModelScope.launch {
            try {
                eliminarGasto.ejecutar(hogar.id, gastoId, password)
                _mensaje.value = "Gasto eliminado"
            } catch (error: Exception) {
                _mensaje.value = error.message ?: "No se pudo eliminar el gasto"
            }
        }
    }

    fun actualizar(
        gastoId: String,
        producto: String,
        montoCents: Long,
        fecha: String,
        password: String,
    ) {
        val hogar = (_state.value as? HomeUiState.Listo)?.hogar ?: return
        viewModelScope.launch {
            try {
                actualizarGasto.ejecutar(hogar.id, gastoId, producto, montoCents, fecha, password)
                _mensaje.value = "Gasto actualizado"
            } catch (error: Exception) {
                _mensaje.value = error.message ?: "No se pudo actualizar el gasto"
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    autenticacion = container.autenticacion,
                    gestionarHogar = container.gestionarHogar,
                    gestionarMiembros = container.gestionarMiembros,
                    consultarGastos = container.consultarGastos,
                    eliminarGasto = container.eliminarGasto,
                    actualizarGasto = container.actualizarGasto,
                )
            }
        }
    }
}
