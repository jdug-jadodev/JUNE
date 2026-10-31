package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Gasto
import kotlinx.coroutines.flow.Flow

interface ConsultarGastosPort {
    fun observar(hogarId: String): Flow<List<Gasto>>
    suspend fun listar(hogarId: String): List<Gasto>
}
