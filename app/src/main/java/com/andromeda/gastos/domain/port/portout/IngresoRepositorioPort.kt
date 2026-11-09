package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Ingreso
import com.andromeda.gastos.domain.entity.Periodicidad

interface IngresoRepositorioPort {
    suspend fun listar(hogarId: String): List<Ingreso>

    suspend fun crear(
        hogarId: String,
        nombre: String?,
        montoCents: Long,
        periodicidad: Periodicidad,
    ): Ingreso

    suspend fun actualizar(
        hogarId: String,
        ingresoId: String,
        nombre: String?,
        montoCents: Long,
        periodicidad: Periodicidad,
    ): Ingreso

    suspend fun eliminar(hogarId: String, ingresoId: String)
}
