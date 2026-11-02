package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.port.portin.EstadoSeguridad
import com.andromeda.gastos.domain.port.portin.SeguridadPort
import com.andromeda.gastos.domain.port.portout.SeguridadRemotaPort

class SeguridadUseCase(
    private val remoto: SeguridadRemotaPort,
) : SeguridadPort {

    override suspend fun estado(): EstadoSeguridad = remoto.estado()

    override suspend fun configurar(pregunta: String, respuesta: String) {
        require(pregunta.trim().length >= 5) { "La pregunta debe tener al menos 5 caracteres" }
        require(respuesta.trim().length >= 2) { "La respuesta debe tener al menos 2 caracteres" }
        remoto.configurar(pregunta.trim(), respuesta.trim())
    }

    override suspend fun generarCodigo(): String = remoto.generarCodigo()

    override suspend fun preguntaPorEmail(email: String): String? =
        remoto.preguntaPorEmail(email.trim())

    override suspend fun recuperarConPregunta(
        email: String,
        respuesta: String,
        passwordNueva: String,
    ) {
        require(passwordNueva.length >= 8) { "La nueva contraseña debe tener al menos 8 caracteres" }
        remoto.recuperarConPregunta(email.trim(), respuesta.trim(), passwordNueva)
    }

    override suspend fun recuperarConCodigo(
        email: String,
        codigo: String,
        passwordNueva: String,
    ) {
        require(passwordNueva.length >= 8) { "La nueva contraseña debe tener al menos 8 caracteres" }
        remoto.recuperarConCodigo(email.trim(), codigo.trim(), passwordNueva)
    }

    override suspend fun cambiarPassword(passwordActual: String, passwordNueva: String) {
        require(passwordNueva.length >= 8) { "La nueva contraseña debe tener al menos 8 caracteres" }
        remoto.cambiarPassword(passwordActual, passwordNueva)
    }
}
