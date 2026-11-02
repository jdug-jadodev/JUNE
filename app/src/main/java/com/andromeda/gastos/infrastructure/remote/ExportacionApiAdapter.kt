package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.port.portout.ExportacionRemotaPort
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class ExportacionApiAdapter(
    private val api: ApiClient,
) : ExportacionRemotaPort {

    override suspend fun descargarCsv(hogarId: String, ciclos: List<String>?): String {
        val consulta = if (ciclos.isNullOrEmpty()) {
            ""
        } else {
            "?ciclos=" + ciclos.joinToString(",")
        }

        val respuesta = api.http.get(api.url("/households/$hogarId/export.csv$consulta")) {
            autorizacion(api)
        }
        respuesta.verificar()
        return respuesta.bodyAsText()
    }
}
