package com.andromeda.gastos.domain.factory

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.rule.GastoReglas
import java.time.Instant
import java.util.UUID

object GastoFactory {
    fun nuevo(
        hogarId: String,
        producto: String,
        montoCents: Long,
        fecha: String,
        usuarioId: String,
        usuarioNombre: String,
    ): Gasto {
        GastoReglas.validar(producto, montoCents, fecha)
        val ahora = Instant.now().toString()
        return Gasto(
            id = UUID.randomUUID().toString(),
            hogarId = hogarId,
            producto = producto.trim(),
            montoCents = montoCents,
            fecha = fecha,
            creadoPor = usuarioId,
            creadoPorNombre = usuarioNombre,
            createdAt = ahora,
            updatedAt = ahora,
        )
    }
}
