package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.entity.Miembro
import com.andromeda.gastos.domain.enums.Acceso
import com.andromeda.gastos.domain.port.portout.MiembroRepositorioPort
import com.andromeda.gastos.infrastructure.remote.dto.CambiarAccesoRequest
import com.andromeda.gastos.infrastructure.remote.dto.HogarDto
import com.andromeda.gastos.infrastructure.remote.dto.MiembroDto
import com.andromeda.gastos.infrastructure.remote.dto.UnirseRequest
import com.andromeda.gastos.infrastructure.remote.mapper.aDominio
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class MiembroApiAdapter(
    private val api: ApiClient,
) : MiembroRepositorioPort {

    override suspend fun listar(hogarId: String): List<Miembro> =
        api.http.get(api.url("/households/$hogarId/members")) {
            autorizacion(api)
        }.decodificar<List<MiembroDto>>().map { it.aDominio() }

    override suspend fun unirse(inviteCode: String): Hogar =
        api.http.post(api.url("/households/join")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(UnirseRequest(inviteCode = inviteCode))
        }.decodificar<HogarDto>().aDominio()

    override suspend fun eliminar(hogarId: String, miembroId: String) {
        api.http.delete(api.url("/households/$hogarId/members/$miembroId")) {
            autorizacion(api)
        }.verificar()
    }

    override suspend fun cambiarAcceso(hogarId: String, miembroId: String, acceso: Acceso) {
        api.http.patch(api.url("/households/$hogarId/members/$miembroId/access")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(CambiarAccesoRequest(acceso = acceso.name))
        }.verificar()
    }
}
