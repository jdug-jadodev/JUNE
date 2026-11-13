package com.andromeda.gastos.infrastructure.remote

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Avisa cuando el backend rechaza el token de acceso (401) en una petición
 * autenticada. La app intenta renovar la sesión y, si no puede, vuelve al login
 * en vez de quedarse con pantallas que no cargan.
 */
object SesionExpiradaBus {
    private val _eventos = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val eventos = _eventos.asSharedFlow()

    fun notificar() {
        _eventos.tryEmit(Unit)
    }
}
