package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.entity.ResumenMes
import kotlinx.coroutines.flow.Flow

interface ConsultarGastosPort {
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
}
