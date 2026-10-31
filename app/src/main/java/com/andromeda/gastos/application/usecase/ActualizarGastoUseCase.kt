package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.port.portin.ActualizarGastoPort
import com.andromeda.gastos.domain.port.portout.GastoRepositorioPort
import com.andromeda.gastos.domain.rule.GastoReglas

class ActualizarGastoUseCase(
    private val repositorio: GastoRepositorioPort,
) : ActualizarGastoPort {

    override suspend fun ejecutar(
        hogarId: String,
        gastoId: String,
        producto: String,
        montoCents: Long,
        fecha: String,
        adminPassword: String,
    ): Gasto {
        GastoReglas.validar(producto, montoCents, fecha)
        require(adminPassword.isNotBlank()) { "Ingresa la contraseña de administrador" }
        return repositorio.actualizar(
            hogarId = hogarId,
            gastoId = gastoId,
            producto = producto.trim(),
            montoCents = montoCents,
            fecha = fecha,
            adminPassword = adminPassword,
        )
    }
}
