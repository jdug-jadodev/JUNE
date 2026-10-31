package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Sesion

interface AutenticacionRemotaPort {
    suspend fun registrar(email: String, password: String, nombre: String): Sesion
    suspend fun login(email: String, password: String): Sesion
    suspend fun refrescar(refreshToken: String): Sesion
}
