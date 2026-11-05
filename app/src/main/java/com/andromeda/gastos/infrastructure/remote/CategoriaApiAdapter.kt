package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.entity.Concepto
import com.andromeda.gastos.domain.port.portout.CategoriaRepositorioPort
import com.andromeda.gastos.infrastructure.remote.dto.ConceptoDto
import com.andromeda.gastos.infrastructure.remote.dto.CrearCategoriaRequest
import com.andromeda.gastos.infrastructure.remote.mapper.aDominio
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class CategoriaApiAdapter(
    private val api: ApiClient,
) : CategoriaRepositorioPort {

    override suspend fun listar(hogarId: String): List<Concepto> =
        api.http.get(api.url("/households/$hogarId/categories")) {
            autorizacion(api)
        }.decodificar<List<ConceptoDto>>().map { it.aDominio() }

    override suspend fun crear(hogarId: String, nombre: String): Concepto =
        api.http.post(api.url("/households/$hogarId/categories")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(CrearCategoriaRequest(nombre = nombre))
        }.decodificar<ConceptoDto>().aDominio()
}
