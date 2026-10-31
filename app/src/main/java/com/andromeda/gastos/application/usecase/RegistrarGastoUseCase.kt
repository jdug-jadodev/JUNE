package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.port.portin.RegistrarGastoPort
import com.andromeda.gastos.domain.port.portout.GastoRepositorioPort
import com.andromeda.gastos.domain.rule.GastoReglas

class RegistrarGastoUseCase(
    private val repositorio: GastoRepositorioPort,
) : RegistrarGastoPort {

    override suspend fun ejecutar(
        hogarId: String,
        producto: String,
        montoCents: Long,
        fecha: String,
    ): Gasto {
        GastoReglas.validar(producto, montoCents, fecha)
        return repositorio.crear(hogarId, producto.trim(), montoCents, fecha)
    }
}
