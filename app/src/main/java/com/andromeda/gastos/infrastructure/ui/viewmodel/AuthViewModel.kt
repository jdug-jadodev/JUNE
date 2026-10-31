package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.domain.port.portin.AutenticacionPort
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val nombre: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val autenticacion: AutenticacionPort,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    private val _sesionEvento = MutableSharedFlow<Sesion>(extraBufferCapacity = 1)
    val sesionEvento = _sesionEvento.asSharedFlow()

    fun onEmail(valor: String) = _state.update { it.copy(email = valor, error = null) }
    fun onPassword(valor: String) = _state.update { it.copy(password = valor, error = null) }
    fun onNombre(valor: String) = _state.update { it.copy(nombre = valor, error = null) }

    fun login() {
        val actual = _state.value
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            runCatching { autenticacion.login(actual.email.trim(), actual.password) }
                .onSuccess { sesion ->
                    _state.value = AuthUiState()
                    _sesionEvento.emit(sesion)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            cargando = false,
                            error = error.message ?: "No se pudo iniciar sesión",
                        )
                    }
                }
        }
    }

    fun registrar() {
        val actual = _state.value
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            runCatching {
                autenticacion.registrar(
                    email = actual.email.trim(),
                    password = actual.password,
                    nombre = actual.nombre.trim(),
                )
            }.onSuccess { sesion ->
                _state.value = AuthUiState()
                _sesionEvento.emit(sesion)
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        cargando = false,
                        error = error.message ?: "No se pudo crear la cuenta",
                    )
                }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AuthViewModel(container.autenticacion) }
        }
    }
}
