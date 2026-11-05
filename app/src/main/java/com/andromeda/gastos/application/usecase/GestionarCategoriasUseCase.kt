package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Concepto
import com.andromeda.gastos.domain.port.portin.GestionarCategoriasPort
import com.andromeda.gastos.domain.port.portout.CategoriaRepositorioPort

class GestionarCategoriasUseCase(
    private val repositorio: CategoriaRepositorioPort,
) : GestionarCategoriasPort {

    override suspend fun listar(hogarId: String): List<Concepto> =
        repositorio.listar(hogarId)

    override suspend fun crear(hogarId: String, nombre: String): Concepto =
        repositorio.crear(hogarId, nombre)
}
