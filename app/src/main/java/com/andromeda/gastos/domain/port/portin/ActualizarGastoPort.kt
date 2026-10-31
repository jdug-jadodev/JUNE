package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Gasto

interface ActualizarGastoPort {
    suspend fun ejecutar(
        hogarId: String,
        gastoId: String,
        producto: String,
        montoCents: Long,
        fecha: String,
        adminPassword: String,
    ): Gasto
}
