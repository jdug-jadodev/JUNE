package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Gasto

interface RegistrarGastoPort {
    suspend fun ejecutar(
        hogarId: String,
        producto: String,
        montoCents: Long,
        fecha: String,
    ): Gasto
}
