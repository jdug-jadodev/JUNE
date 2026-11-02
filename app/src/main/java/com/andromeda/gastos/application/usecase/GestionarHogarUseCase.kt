package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.port.portin.GestionarHogarPort
import com.andromeda.gastos.domain.port.portout.HogarRepositorioPort

class GestionarHogarUseCase(
    private val repositorio: HogarRepositorioPort,
) : GestionarHogarPort {

    override suspend fun crear(nombre: String): Hogar {
        require(nombre.isNotBlank()) { "El nombre del hogar es obligatorio" }
        return repositorio.crear(nombre.trim())
    }

    override suspend fun listar(): List<Hogar> = repositorio.listar()

    override suspend fun obtener(hogarId: String): Hogar = repositorio.obtener(hogarId)

    override suspend fun configurarPassword(hogarId: String, password: String) {
        require(password.length >= 8) { "La contraseña debe tener al menos 8 caracteres" }
        repositorio.configurarPassword(hogarId, password)
    }

    override suspend fun configurarCorte(hogarId: String, cutoffDay: Int): Hogar {
        require(cutoffDay in 1..31) { "El día de corte debe estar entre 1 y 31" }
        return repositorio.configurarCorte(hogarId, cutoffDay)
    }
}
