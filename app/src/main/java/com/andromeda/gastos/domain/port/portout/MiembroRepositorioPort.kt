package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.entity.Miembro

interface MiembroRepositorioPort {
    suspend fun listar(hogarId: String): List<Miembro>
    suspend fun unirse(inviteCode: String): Hogar
    suspend fun eliminar(hogarId: String, miembroId: String)
}
