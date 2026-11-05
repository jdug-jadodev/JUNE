package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Concepto

interface CategoriaRepositorioPort {
    suspend fun listar(hogarId: String): List<Concepto>

    suspend fun crear(hogarId: String, nombre: String): Concepto
}
