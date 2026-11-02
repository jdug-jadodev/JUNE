package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.entity.Miembro
import com.andromeda.gastos.domain.enums.Acceso
import com.andromeda.gastos.domain.port.portin.GestionarMiembrosPort
import com.andromeda.gastos.domain.port.portout.MiembroRepositorioPort

class GestionarMiembrosUseCase(
    private val repositorio: MiembroRepositorioPort,
) : GestionarMiembrosPort {

    override suspend fun listar(hogarId: String): List<Miembro> =
        repositorio.listar(hogarId)

    override suspend fun unirse(inviteCode: String): Hogar {
        require(inviteCode.isNotBlank()) { "El código de invitación es obligatorio" }
        return repositorio.unirse(inviteCode.trim())
    }

    override suspend fun eliminar(hogarId: String, miembroId: String) =
        repositorio.eliminar(hogarId, miembroId)

    override suspend fun cambiarAcceso(hogarId: String, miembroId: String, acceso: Acceso) =
        repositorio.cambiarAcceso(hogarId, miembroId, acceso)
}
