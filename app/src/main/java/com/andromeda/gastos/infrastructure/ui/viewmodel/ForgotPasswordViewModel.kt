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

data class ForgotPasswordUiState(
    val email: String = "",
    val usandoCodigo: Boolean = false,
    val pregunta: String? = null,
    val respuesta: String = "",
    val codigo: String = "",
    val passwordNueva: String = "",
    val confirmacion: String = "",
    val buscando: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val listo: Boolean = false,
)

class ForgotPasswordViewModel(
    private val seguridad: SeguridadPort,
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state = _state.asStateFlow()

    fun onEmail(valor: String) = _state.update {
        it.copy(email = valor, pregunta = null, error = null)
    }

    fun onRespuesta(valor: String) = _state.update { it.copy(respuesta = valor, error = null) }

    fun onCodigo(valor: String) = _state.update { it.copy(codigo = valor, error = null) }

    fun onPasswordNueva(valor: String) = _state.update {
        it.copy(passwordNueva = valor, error = null)
    }

    fun onConfirmacion(valor: String) = _state.update {
        it.copy(confirmacion = valor, error = null)
    }

    fun usarCodigo(usar: Boolean) = _state.update {
        it.copy(usandoCodigo = usar, error = null)
    }

    fun buscarPregunta() {
        val email = _state.value.email.trim()
        if (email.isBlank()) {
            _state.update { it.copy(error = "Escribe tu email") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(buscando = true, error = null, pregunta = null) }
            runCatching { seguridad.preguntaPorEmail(email) }
                .onSuccess { pregunta ->
                    _state.update {
                        if (pregunta.isNullOrBlank()) {
                            it.copy(
                                buscando = false,
                                error = "Esta cuenta no tiene pregunta de seguridad. " +
                                    "Usa la llave de recuperación o pide a un administrador.",
                            )
                        } else {
                            it.copy(buscando = false, pregunta = pregunta)
                        }
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            buscando = false,
                            error = error.message ?: "No se pudo buscar la pregunta",
                        )
                    }
                }
        }
    }

    fun recuperar() {
        val actual = _state.value
        val email = actual.email.trim()
        if (email.isBlank()) {
            _state.update { it.copy(error = "Escribe tu email") }
            return
        }
        if (actual.passwordNueva.length < 8) {
            _state.update { it.copy(error = "La nueva contraseña debe tener al menos 8 caracteres") }
            return
        }
        if (actual.passwordNueva != actual.confirmacion) {
            _state.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }
        if (!actual.usandoCodigo) {
            if (actual.pregunta.isNullOrBlank()) {
                _state.update { it.copy(error = "Busca tu pregunta primero") }
                return
            }
            if (actual.respuesta.isBlank()) {
                _state.update { it.copy(error = "Escribe tu respuesta") }
                return
            }
        } else if (actual.codigo.isBlank()) {
            _state.update { it.copy(error = "Escribe tu llave de recuperación") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            val resultado = runCatching {
                if (actual.usandoCodigo) {
                    seguridad.recuperarConCodigo(email, actual.codigo, actual.passwordNueva)
                } else {
                    seguridad.recuperarConPregunta(
                        email,
                        actual.respuesta,
                        actual.passwordNueva,
                    )
                }
            }

            resultado
                .onSuccess { _state.update { it.copy(cargando = false, listo = true) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            cargando = false,
                            error = error.message ?: "No se pudo recuperar la cuenta",
                        )
                    }
                }
        }
    }

    fun limpiarError() = _state.update { it.copy(error = null) }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ForgotPasswordViewModel(seguridad = container.seguridad)
            }
        }
    }
}
