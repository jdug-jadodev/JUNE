package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Ingreso
import com.andromeda.gastos.domain.entity.Periodicidad
import com.andromeda.gastos.domain.port.portin.GestionarIngresosPort
import com.andromeda.gastos.domain.port.portout.IngresoRepositorioPort

class GestionarIngresosUseCase(
    private val repositorio: IngresoRepositorioPort,
) : GestionarIngresosPort {

    override suspend fun listar(hogarId: String): List<Ingreso> =
        repositorio.listar(hogarId)

    override suspend fun crear(
        hogarId: String,
        nombre: String?,
        montoCents: Long,
        periodicidad: Periodicidad,
    ): Ingreso = repositorio.crear(hogarId, nombre, montoCents, periodicidad)

    override suspend fun actualizar(
        hogarId: String,
        ingresoId: String,
        nombre: String?,
        montoCents: Long,
        periodicidad: Periodicidad,
    ): Ingreso = repositorio.actualizar(hogarId, ingresoId, nombre, montoCents, periodicidad)

    override suspend fun eliminar(hogarId: String, ingresoId: String) =
        repositorio.eliminar(hogarId, ingresoId)
}
