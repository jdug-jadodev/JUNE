package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Hogar

interface HogarRepositorioPort {
    suspend fun crear(nombre: String): Hogar
    suspend fun listar(): List<Hogar>
    suspend fun obtener(hogarId: String): Hogar
    suspend fun configurarPassword(hogarId: String, password: String)
    suspend fun configurarCorte(hogarId: String, cutoffDay: Int): Hogar
}
