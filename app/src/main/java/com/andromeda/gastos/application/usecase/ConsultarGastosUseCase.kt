package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import com.andromeda.gastos.domain.port.portout.GastoRepositorioPort
import kotlinx.coroutines.flow.Flow

class ConsultarGastosUseCase(
    private val repositorio: GastoRepositorioPort,
) : ConsultarGastosPort {

    override fun observar(hogarId: String): Flow<List<Gasto>> =
        repositorio.observar(hogarId)

    override suspend fun listar(hogarId: String): List<Gasto> =
        repositorio.listar(hogarId)
}
