package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.entity.Ingreso
import com.andromeda.gastos.domain.entity.Periodicidad
import com.andromeda.gastos.domain.port.portout.IngresoRepositorioPort
import com.andromeda.gastos.infrastructure.remote.dto.IngresoDto
import com.andromeda.gastos.infrastructure.remote.dto.IngresoRequest
import com.andromeda.gastos.infrastructure.remote.mapper.aDominio
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class IngresoApiAdapter(
    private val api: ApiClient,
) : IngresoRepositorioPort {

    override suspend fun listar(hogarId: String): List<Ingreso> =
        api.http.get(api.url("/households/$hogarId/incomes")) {
            autorizacion(api)
        }.decodificar<List<IngresoDto>>().map { it.aDominio() }

    override suspend fun crear(
        hogarId: String,
        nombre: String?,
        montoCents: Long,
        periodicidad: Periodicidad,
    ): Ingreso =
        api.http.post(api.url("/households/$hogarId/incomes")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(IngresoRequest(nombre = nombre, montoCents = montoCents, periodicidad = periodicidad.name))
        }.decodificar<IngresoDto>().aDominio()

    override suspend fun actualizar(
        hogarId: String,
        ingresoId: String,
        nombre: String?,
        montoCents: Long,
        periodicidad: Periodicidad,
    ): Ingreso =
        api.http.patch(api.url("/households/$hogarId/incomes/$ingresoId")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(IngresoRequest(nombre = nombre, montoCents = montoCents, periodicidad = periodicidad.name))
        }.decodificar<IngresoDto>().aDominio()

    override suspend fun eliminar(hogarId: String, ingresoId: String) {
        api.http.delete(api.url("/households/$hogarId/incomes/$ingresoId")) {
            autorizacion(api)
        }.verificar()
    }
}
