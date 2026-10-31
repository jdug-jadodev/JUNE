package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.port.portin.EliminarGastoPort
import com.andromeda.gastos.domain.port.portout.GastoRepositorioPort

class EliminarGastoUseCase(
    private val repositorio: GastoRepositorioPort,
) : EliminarGastoPort {

    override suspend fun ejecutar(hogarId: String, gastoId: String, adminPassword: String) {
        require(adminPassword.isNotBlank()) { "Ingresa la contraseña de administrador" }
        repositorio.eliminar(hogarId, gastoId, adminPassword)
    }
}
