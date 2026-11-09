package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.entity.Analisis
import com.andromeda.gastos.domain.entity.AnalisisIA
import com.andromeda.gastos.domain.entity.Concepto
import com.andromeda.gastos.domain.port.portout.AnalisisRepositorioPort
import com.andromeda.gastos.domain.port.portout.AnalisisIARepositorioPort
import com.andromeda.gastos.infrastructure.remote.dto.AiInsightsRequest
import com.andromeda.gastos.infrastructure.remote.dto.AnalisisDto
import com.andromeda.gastos.infrastructure.remote.dto.AnalisisIADto
import com.andromeda.gastos.infrastructure.remote.dto.ClasificacionRequest
import com.andromeda.gastos.infrastructure.remote.dto.ConceptoDto
import com.andromeda.gastos.infrastructure.remote.mapper.aDominio
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AnalisisApiAdapter(
    private val api: ApiClient,
) : AnalisisRepositorioPort, AnalisisIARepositorioPort {

    override suspend fun analizar(hogarId: String, ciclos: List<String>?): Analisis {
        val consulta = if (ciclos.isNullOrEmpty()) {
            ""
        } else {
            "?ciclos=" + ciclos.joinToString(",")
        }

        return api.http.get(api.url("/households/$hogarId/insights$consulta")) {
            autorizacion(api)
        }.decodificar<AnalisisDto>().aDominio()
    }

    override suspend fun conceptos(): List<Concepto> =
        api.http.get(api.url("/concepts")) {
            autorizacion(api)
        }.decodificar<List<ConceptoDto>>().map { it.aDominio() }

    override suspend fun clasificar(
        hogarId: String,
        termino: String,
        conceptoId: String,
        nota: String?,
    ) {
        api.http.post(api.url("/households/$hogarId/classification")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(ClasificacionRequest(termino = termino, conceptoId = conceptoId, nota = nota))
        }.verificar()
    }

    override suspend fun analizarConIA(
        hogarId: String,
        ciclos: List<String>?,
        personas: Int?,
    ): AnalisisIA =
        api.http.post(api.url("/households/$hogarId/ai-insights")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(AiInsightsRequest(ciclos = ciclos, personas = personas))
        }.decodificar<AnalisisIADto>().aDominio()
}
