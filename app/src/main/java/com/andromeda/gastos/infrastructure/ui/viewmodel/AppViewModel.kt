package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.domain.port.portin.AutenticacionPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Cargando : AuthState
    data object SinSesion : AuthState
    data class ConSesion(val sesion: Sesion) : AuthState
}

class AppViewModel(
    private val autenticacion: AutenticacionPort,
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Cargando)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val sesion = runCatching { autenticacion.sesionActual() }.getOrNull()
            _state.value = if (sesion != null) AuthState.ConSesion(sesion) else AuthState.SinSesion
        }
    }

    fun onSesion(sesion: Sesion) {
        _state.value = AuthState.ConSesion(sesion)
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { autenticacion.logout() }
            _state.value = AuthState.SinSesion
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { AppViewModel(container.autenticacion) }
        }
    }
}
