package com.andromeda.gastos.domain.port.portin

interface EliminarGastoPort {
    suspend fun ejecutar(
        hogarId: String,
        gastoId: String,
        adminPassword: String,
    )
}
