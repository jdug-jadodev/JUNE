package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.domain.port.portin.AutenticacionPort
import com.andromeda.gastos.domain.port.portout.AutenticacionRemotaPort
import com.andromeda.gastos.domain.port.portout.CacheDatosPort
import com.andromeda.gastos.domain.port.portout.SesionLocalPort

class AutenticacionUseCase(
    private val remoto: AutenticacionRemotaPort,
    private val local: SesionLocalPort,
    private val cache: CacheDatosPort,
) : AutenticacionPort {

    override suspend fun registrar(email: String, password: String, nombre: String): Sesion {
        val sesion = remoto.registrar(email, password, nombre)
        cache.invalidarTodo()
        local.guardar(sesion)
        return sesion
    }

    override suspend fun login(email: String, password: String): Sesion {
        val sesion = remoto.login(email, password)
        cache.invalidarTodo()
        local.guardar(sesion)
        return sesion
    }

    override suspend fun logout() {
        local.limpiar()
        cache.invalidarTodo()
    }

    override suspend fun sesionActual(): Sesion? = local.obtener()
}
