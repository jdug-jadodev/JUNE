package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Gasto
import kotlinx.coroutines.flow.Flow

interface GastoRepositorioPort {
    fun observar(hogarId: String): Flow<List<Gasto>>
    suspend fun listar(hogarId: String): List<Gasto>
    suspend fun crear(hogarId: String, producto: String, montoCents: Long, fecha: String): Gasto
    suspend fun actualizar(
        hogarId: String,
        gastoId: String,
        producto: String,
        montoCents: Long,
        fecha: String,
        adminPassword: String,
    ): Gasto

    suspend fun eliminar(hogarId: String, gastoId: String, adminPassword: String)
}
