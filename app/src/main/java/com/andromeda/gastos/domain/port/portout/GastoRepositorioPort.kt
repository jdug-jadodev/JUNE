package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.entity.ResumenMes
import kotlinx.coroutines.flow.Flow

interface GastoRepositorioPort {
    fun observar(
        hogarId: String,
        desde: String? = null,
        hasta: String? = null,
    ): Flow<List<Gasto>>

    suspend fun listar(
        hogarId: String,
        desde: String? = null,
        hasta: String? = null,
    ): List<Gasto>

    suspend fun listarMeses(hogarId: String): List<ResumenMes>

    suspend fun listarCiclos(hogarId: String): List<ResumenCiclo>

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
