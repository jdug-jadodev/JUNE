package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Hogar

interface GestionarHogarPort {
    suspend fun crear(nombre: String): Hogar
    suspend fun listar(): List<Hogar>
    suspend fun obtener(hogarId: String): Hogar
    suspend fun configurarPassword(hogarId: String, password: String)
}
