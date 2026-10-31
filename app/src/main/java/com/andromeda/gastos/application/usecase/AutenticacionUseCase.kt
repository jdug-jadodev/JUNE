package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.domain.port.portin.AutenticacionPort
import com.andromeda.gastos.domain.port.portout.AutenticacionRemotaPort
import com.andromeda.gastos.domain.port.portout.SesionLocalPort

class AutenticacionUseCase(
    private val remoto: AutenticacionRemotaPort,
    private val local: SesionLocalPort,
) : AutenticacionPort {

    override suspend fun registrar(email: String, password: String, nombre: String): Sesion {
        val sesion = remoto.registrar(email, password, nombre)
        local.guardar(sesion)
        return sesion
    }

    override suspend fun login(email: String, password: String): Sesion {
        val sesion = remoto.login(email, password)
        local.guardar(sesion)
        return sesion
    }

    override suspend fun logout() {
        local.limpiar()
    }

    override suspend fun sesionActual(): Sesion? = local.obtener()
}
