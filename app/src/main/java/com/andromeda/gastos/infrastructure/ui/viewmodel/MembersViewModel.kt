package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.entity.Miembro
import com.andromeda.gastos.domain.enums.Acceso
import com.andromeda.gastos.domain.enums.Rol
import com.andromeda.gastos.domain.port.portin.AutenticacionPort
import com.andromeda.gastos.domain.port.portin.GestionarHogarPort
import com.andromeda.gastos.domain.port.portin.GestionarMiembrosPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MembersUiState(
    val hogar: Hogar? = null,
    val miembros: List<Miembro> = emptyList(),
    val esAdmin: Boolean = false,
    val miUsuarioId: String? = null,
    val cargando: Boolean = true,
    val error: String? = null,
)

class MembersViewModel(
    private val autenticacion: AutenticacionPort,
    private val gestionarHogar: GestionarHogarPort,
    private val gestionarMiembros: GestionarMiembrosPort,
) : ViewModel() {

    private val _state = MutableStateFlow(MembersUiState())
    val state = _state.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    fun cargar(hogarId: String) {
        viewModelScope.launch {
            _state.value = MembersUiState(cargando = true)
            try {
                val sesion = autenticacion.sesionActual()
                val miUsuarioId = sesion?.usuario?.id
                val hogar = gestionarHogar.obtener(hogarId)
                val miembros = gestionarMiembros.listar(hogarId)
                val esAdmin = miembros.any {
                    it.usuarioId == miUsuarioId && it.rol == Rol.ADMIN
                }
                _state.value = MembersUiState(
                    hogar = hogar,
                    miembros = miembros,
                    esAdmin = esAdmin,
                    miUsuarioId = miUsuarioId,
                    cargando = false,
                )
            } catch (error: Exception) {
                _state.value = MembersUiState(
                    cargando = false,
                    error = error.message ?: "No se pudieron cargar los miembros",
                )
            }
        }
    }

    fun eliminar(hogarId: String, miembroId: String) {
        viewModelScope.launch {
            try {
                gestionarMiembros.eliminar(hogarId, miembroId)
                _mensaje.value = "Miembro eliminado"
                cargar(hogarId)
            } catch (error: Exception) {
                _mensaje.value = error.message ?: "No se pudo eliminar el miembro"
            }
        }
    }

    fun cambiarAcceso(hogarId: String, miembroId: String, acceso: Acceso) {
        viewModelScope.launch {
            try {
                gestionarMiembros.cambiarAcceso(hogarId, miembroId, acceso)
                _mensaje.value = if (acceso == Acceso.LECTURA) {
                    "Miembro en solo lectura"
                } else {
                    "Miembro puede escribir"
                }
                cargar(hogarId)
            } catch (error: Exception) {
                _mensaje.value = error.message ?: "No se pudo cambiar el permiso"
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MembersViewModel(
                    autenticacion = container.autenticacion,
                    gestionarHogar = container.gestionarHogar,
                    gestionarMiembros = container.gestionarMiembros,
                )
            }
        }
    }
}
