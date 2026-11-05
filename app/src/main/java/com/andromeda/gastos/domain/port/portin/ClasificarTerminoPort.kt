package com.andromeda.gastos.domain.port.portin

interface ClasificarTerminoPort {
    suspend fun clasificar(
        hogarId: String,
        termino: String,
        conceptoId: String,
        nota: String?,
    )
}
