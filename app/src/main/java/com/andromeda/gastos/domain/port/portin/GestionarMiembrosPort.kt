package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.entity.Miembro
import com.andromeda.gastos.domain.enums.Acceso

interface GestionarMiembrosPort {
    suspend fun listar(hogarId: String): List<Miembro>
    suspend fun unirse(inviteCode: String): Hogar
    suspend fun eliminar(hogarId: String, miembroId: String)
    suspend fun cambiarAcceso(hogarId: String, miembroId: String, acceso: Acceso)
}
