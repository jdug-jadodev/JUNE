package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Sesion

interface SesionLocalPort {
    suspend fun guardar(sesion: Sesion)
    suspend fun obtener(): Sesion?
    suspend fun limpiar()
}
