package com.andromeda.gastos.domain.port.portout

interface VozPort {
    suspend fun hablar(texto: String)
    suspend fun escuchar(): String
    fun detener()
}
