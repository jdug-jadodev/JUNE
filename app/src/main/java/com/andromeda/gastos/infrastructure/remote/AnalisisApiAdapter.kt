package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.entity.Analisis
import com.andromeda.gastos.domain.port.portout.AnalisisRepositorioPort
import com.andromeda.gastos.infrastructure.remote.dto.AnalisisDto
import com.andromeda.gastos.infrastructure.remote.mapper.aDominio
import io.ktor.client.request.get

class AnalisisApiAdapter(
    private val api: ApiClient,
) : AnalisisRepositorioPort {

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
}
