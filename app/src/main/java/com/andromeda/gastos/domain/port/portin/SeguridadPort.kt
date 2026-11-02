package com.andromeda.gastos.domain.port.portin

data class EstadoSeguridad(
    val pregunta: String?,
    val tieneCodigo: Boolean,
)

interface SeguridadPort {
    suspend fun estado(): EstadoSeguridad
    suspend fun configurar(pregunta: String, respuesta: String)
    suspend fun generarCodigo(): String
    suspend fun preguntaPorEmail(email: String): String?
    suspend fun recuperarConPregunta(
        email: String,
        respuesta: String,
        passwordNueva: String,
    )

    suspend fun recuperarConCodigo(
        email: String,
        codigo: String,
        passwordNueva: String,
    )

    suspend fun cambiarPassword(passwordActual: String, passwordNueva: String)
}
