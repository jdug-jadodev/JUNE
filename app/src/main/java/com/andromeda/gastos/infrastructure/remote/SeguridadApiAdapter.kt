package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.port.portin.EstadoSeguridad
import com.andromeda.gastos.domain.port.portout.SeguridadRemotaPort
import com.andromeda.gastos.infrastructure.remote.dto.CambiarPasswordRequest
import com.andromeda.gastos.infrastructure.remote.dto.CodigoRecuperacionResponse
import com.andromeda.gastos.infrastructure.remote.dto.EstadoSeguridadDto
import com.andromeda.gastos.infrastructure.remote.dto.PreguntaRequest
import com.andromeda.gastos.infrastructure.remote.dto.PreguntaResponse
import com.andromeda.gastos.infrastructure.remote.dto.ResetCodigoRequest
import com.andromeda.gastos.infrastructure.remote.dto.ResetPreguntaRequest
import com.andromeda.gastos.infrastructure.remote.dto.SeguridadRequest
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SeguridadApiAdapter(
    private val api: ApiClient,
) : SeguridadRemotaPort {

    override suspend fun estado(): EstadoSeguridad {
        val dto = api.http.get(api.url("/auth/security")) {
            autorizacion(api)
        }.decodificar<EstadoSeguridadDto>()

        return EstadoSeguridad(
            pregunta = dto.pregunta,
            tieneCodigo = dto.tieneCodigo,
        )
    }

    override suspend fun configurar(pregunta: String, respuesta: String) {
        api.http.post(api.url("/auth/security")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(SeguridadRequest(pregunta = pregunta, respuesta = respuesta))
        }.verificar()
    }

    override suspend fun generarCodigo(): String =
        api.http.post(api.url("/auth/recovery-code")) {
            autorizacion(api)
        }.decodificar<CodigoRecuperacionResponse>().codigo

    override suspend fun preguntaPorEmail(email: String): String? =
        api.http.post(api.url("/auth/security-question")) {
            contentType(ContentType.Application.Json)
            setBody(PreguntaRequest(email = email))
        }.decodificar<PreguntaResponse>().pregunta

    override suspend fun recuperarConPregunta(
        email: String,
        respuesta: String,
        passwordNueva: String,
    ) {
        api.http.post(api.url("/auth/reset-password")) {
            contentType(ContentType.Application.Json)
            setBody(
                ResetPreguntaRequest(
                    email = email,
                    respuesta = respuesta,
                    passwordNueva = passwordNueva,
                ),
            )
        }.verificar()
    }

    override suspend fun recuperarConCodigo(
        email: String,
        codigo: String,
        passwordNueva: String,
    ) {
        api.http.post(api.url("/auth/reset-password-code")) {
            contentType(ContentType.Application.Json)
            setBody(
                ResetCodigoRequest(
                    email = email,
                    codigo = codigo,
                    passwordNueva = passwordNueva,
                ),
            )
        }.verificar()
    }

    override suspend fun cambiarPassword(passwordActual: String, passwordNueva: String) {
        api.http.post(api.url("/auth/change-password")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(
                CambiarPasswordRequest(
                    passwordActual = passwordActual,
                    passwordNueva = passwordNueva,
                ),
            )
        }.verificar()
    }
}
