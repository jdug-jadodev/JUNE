package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.entity.ResumenMes
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import com.andromeda.gastos.domain.port.portout.GastoRepositorioPort
import kotlinx.coroutines.flow.Flow

class ConsultarGastosUseCase(
    private val repositorio: GastoRepositorioPort,
) : ConsultarGastosPort {

    override fun observar(
        hogarId: String,
        desde: String?,
        hasta: String?,
    ): Flow<List<Gasto>> = repositorio.observar(hogarId, desde, hasta)

    override suspend fun listar(
        hogarId: String,
        desde: String?,
        hasta: String?,
    ): List<Gasto> = repositorio.listar(hogarId, desde, hasta)

    override suspend fun listarMeses(hogarId: String): List<ResumenMes> =
        repositorio.listarMeses(hogarId)
}
