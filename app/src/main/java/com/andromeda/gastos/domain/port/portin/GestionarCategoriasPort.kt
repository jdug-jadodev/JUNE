package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Concepto

interface GestionarCategoriasPort {
    suspend fun listar(hogarId: String): List<Concepto>

    suspend fun crear(hogarId: String, nombre: String): Concepto
}
