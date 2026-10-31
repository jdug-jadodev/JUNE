package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.domain.port.portout.AutenticacionRemotaPort
import com.andromeda.gastos.infrastructure.remote.dto.AuthResponseDto
import com.andromeda.gastos.infrastructure.remote.dto.LoginRequest
import com.andromeda.gastos.infrastructure.remote.dto.RefreshRequest
import com.andromeda.gastos.infrastructure.remote.dto.RegistroRequest
import com.andromeda.gastos.infrastructure.remote.mapper.aDominio
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiAdapter(
    private val api: ApiClient,
) : AutenticacionRemotaPort {

    override suspend fun registrar(email: String, password: String, nombre: String): Sesion =
        api.http.post(api.url("/auth/register")) {
            contentType(ContentType.Application.Json)
            setBody(RegistroRequest(email = email, password = password, nombre = nombre))
        }.decodificar<AuthResponseDto>().aDominio()

    override suspend fun login(email: String, password: String): Sesion =
        api.http.post(api.url("/auth/login")) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = email, password = password))
        }.decodificar<AuthResponseDto>().aDominio()

    override suspend fun refrescar(refreshToken: String): Sesion =
        api.http.post(api.url("/auth/refresh")) {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken = refreshToken))
        }.decodificar<AuthResponseDto>().aDominio()
}
