package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.port.portout.HogarRepositorioPort
import com.andromeda.gastos.infrastructure.local.CacheDatos
import com.andromeda.gastos.infrastructure.remote.dto.ConfigurarCorteRequest
import com.andromeda.gastos.infrastructure.remote.dto.ConfigurarPasswordRequest
import com.andromeda.gastos.infrastructure.remote.dto.CrearHogarRequest
import com.andromeda.gastos.infrastructure.remote.dto.HogarDto
import com.andromeda.gastos.infrastructure.remote.mapper.aDominio
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class HogarApiAdapter(
    private val api: ApiClient,
    private val cache: CacheDatos,
) : HogarRepositorioPort {

    override suspend fun crear(nombre: String): Hogar {
        val hogar = api.http.post(api.url("/households")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(CrearHogarRequest(nombre = nombre))
        }.decodificar<HogarDto>().aDominio()

        cache.invalidarTodo()
        return hogar
    }

    override suspend fun listar(): List<Hogar> {
        cache.hogares()?.let { return it }

        val hogares = api.http.get(api.url("/households")) {
            autorizacion(api)
        }.decodificar<List<HogarDto>>().map { it.aDominio() }

        cache.guardarHogares(hogares)
        return hogares
    }

    override suspend fun obtener(hogarId: String): Hogar {
        cache.hogares()?.firstOrNull { it.id == hogarId }?.let { return it }

        return api.http.get(api.url("/households/$hogarId")) {
            autorizacion(api)
        }.decodificar<HogarDto>().aDominio()
    }

    override suspend fun configurarPassword(hogarId: String, password: String) {
        api.http.post(api.url("/households/$hogarId/admin-password")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(ConfigurarPasswordRequest(password = password))
        }.verificar()
    }

    override suspend fun configurarCorte(hogarId: String, cutoffDay: Int): Hogar {
        val hogar = api.http.patch(api.url("/households/$hogarId/cutoff")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(ConfigurarCorteRequest(cutoffDay = cutoffDay))
        }.decodificar<HogarDto>().aDominio()

        cache.invalidarHogares()
        return hogar
    }
}
