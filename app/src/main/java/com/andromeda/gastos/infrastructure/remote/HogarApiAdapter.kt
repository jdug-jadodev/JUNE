package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.port.portout.HogarRepositorioPort
import com.andromeda.gastos.infrastructure.remote.dto.ConfigurarPasswordRequest
import com.andromeda.gastos.infrastructure.remote.dto.CrearHogarRequest
import com.andromeda.gastos.infrastructure.remote.dto.HogarDto
import com.andromeda.gastos.infrastructure.remote.mapper.aDominio
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class HogarApiAdapter(
    private val api: ApiClient,
) : HogarRepositorioPort {

    override suspend fun crear(nombre: String): Hogar =
        api.http.post(api.url("/households")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(CrearHogarRequest(nombre = nombre))
        }.decodificar<HogarDto>().aDominio()

    override suspend fun listar(): List<Hogar> =
        api.http.get(api.url("/households")) {
            autorizacion(api)
        }.decodificar<List<HogarDto>>().map { it.aDominio() }

    override suspend fun obtener(hogarId: String): Hogar =
        api.http.get(api.url("/households/$hogarId")) {
            autorizacion(api)
        }.decodificar<HogarDto>().aDominio()

    override suspend fun configurarPassword(hogarId: String, password: String) {
        api.http.post(api.url("/households/$hogarId/admin-password")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(ConfigurarPasswordRequest(password = password))
        }.verificar()
    }
}
