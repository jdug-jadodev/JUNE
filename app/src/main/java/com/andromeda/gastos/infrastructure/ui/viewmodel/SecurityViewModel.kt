package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.port.portin.SeguridadPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SecurityUiState(
    val cargando: Boolean = true,
    val preguntaActual: String? = null,
    val tieneCodigo: Boolean = false,
    val pregunta: String = "",
    val respuesta: String = "",
    val guardandoPregunta: Boolean = false,
    val generandoCodigo: Boolean = false,
    val codigoGenerado: String? = null,
    val cambiandoPassword: Boolean = false,
    val passwordActual: String = "",
    val passwordNueva: String = "",
    val passwordConfirmacion: String = "",
    val mensaje: String? = null,
    val error: String? = null,
)

class SecurityViewModel(
    private val seguridad: SeguridadPort,
) : ViewModel() {

    private val _state = MutableStateFlow(SecurityUiState())
    val state = _state.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            runCatching { seguridad.estado() }
                .onSuccess { estado ->
                    _state.update {
                        it.copy(
                            cargando = false,
                            preguntaActual = estado.pregunta,
                            tieneCodigo = estado.tieneCodigo,
                            pregunta = estado.pregunta.orEmpty(),
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            cargando = false,
                            error = error.message ?: "No se pudo cargar la seguridad",
                        )
                    }
                }
        }
    }

    fun onPregunta(valor: String) = _state.update {
        it.copy(pregunta = valor, error = null, mensaje = null)
    }

    fun onRespuesta(valor: String) = _state.update {
        it.copy(respuesta = valor, error = null, mensaje = null)
    }

    fun guardarPregunta() {
        val actual = _state.value
        if (actual.pregunta.trim().length < 5) {
            _state.update { it.copy(error = "La pregunta debe tener al menos 5 caracteres") }
            return
        }
        if (actual.respuesta.trim().length < 2) {
            _state.update { it.copy(error = "La respuesta debe tener al menos 2 caracteres") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(guardandoPregunta = true, error = null, mensaje = null) }
            runCatching { seguridad.configurar(actual.pregunta, actual.respuesta) }
                .onSuccess {
                    _state.update {
                        it.copy(
                            guardandoPregunta = false,
                            preguntaActual = actual.pregunta.trim(),
                            respuesta = "",
                            mensaje = "Pregunta de seguridad guardada",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            guardandoPregunta = false,
                            error = error.message ?: "No se pudo guardar la pregunta",
                        )
                    }
                }
        }
    }

    fun generarCodigo() {
        viewModelScope.launch {
            _state.update { it.copy(generandoCodigo = true, error = null, mensaje = null) }
            runCatching { seguridad.generarCodigo() }
                .onSuccess { codigo ->
                    _state.update {
                        it.copy(
                            generandoCodigo = false,
                            codigoGenerado = codigo,
                            tieneCodigo = true,
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            generandoCodigo = false,
                            error = error.message ?: "No se pudo generar la llave",
                        )
                    }
                }
        }
    }

    fun cerrarCodigo() = _state.update { it.copy(codigoGenerado = null) }

    fun onPasswordActual(valor: String) = _state.update {
        it.copy(passwordActual = valor, error = null, mensaje = null)
    }

    fun onPasswordNueva(valor: String) = _state.update {
        it.copy(passwordNueva = valor, error = null, mensaje = null)
    }

    fun onPasswordConfirmacion(valor: String) = _state.update {
        it.copy(passwordConfirmacion = valor, error = null, mensaje = null)
    }

    fun cambiarPassword() {
        val actual = _state.value
        if (actual.passwordActual.isBlank()) {
            _state.update { it.copy(error = "Escribe tu contraseña actual") }
            return
        }
        if (actual.passwordNueva.length < 8) {
            _state.update { it.copy(error = "La nueva contraseña debe tener al menos 8 caracteres") }
            return
        }
        if (actual.passwordNueva != actual.passwordConfirmacion) {
            _state.update { it.copy(error = "Las contraseñas nuevas no coinciden") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(cambiandoPassword = true, error = null, mensaje = null) }
            runCatching {
                seguridad.cambiarPassword(actual.passwordActual, actual.passwordNueva)
            }.onSuccess {
                _state.update {
                    it.copy(
                        cambiandoPassword = false,
                        passwordActual = "",
                        passwordNueva = "",
                        passwordConfirmacion = "",
                        mensaje = "Contraseña actualizada",
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        cambiandoPassword = false,
                        error = error.message ?: "No se pudo cambiar la contraseña",
                    )
                }
            }
        }
    }

    fun limpiarMensaje() = _state.update { it.copy(mensaje = null, error = null) }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SecurityViewModel(seguridad = container.seguridad)
            }
        }
    }
}
