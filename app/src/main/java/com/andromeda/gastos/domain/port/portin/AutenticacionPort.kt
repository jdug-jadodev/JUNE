package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Sesion

interface AutenticacionPort {
    suspend fun registrar(email: String, password: String, nombre: String): Sesion
    suspend fun login(email: String, password: String): Sesion
    suspend fun logout()
    suspend fun sesionActual(): Sesion?
}
