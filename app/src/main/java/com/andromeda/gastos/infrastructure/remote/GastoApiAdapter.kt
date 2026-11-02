package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.entity.ResumenMes
import com.andromeda.gastos.domain.port.portout.GastoRepositorioPort
import com.andromeda.gastos.infrastructure.remote.dto.GastoDto
import com.andromeda.gastos.infrastructure.remote.dto.GastoRequest
import com.andromeda.gastos.infrastructure.remote.dto.ResumenCicloDto
import com.andromeda.gastos.infrastructure.remote.dto.ResumenMesDto
import com.andromeda.gastos.infrastructure.remote.mapper.aDominio
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val INTERVALO_POLLING_MS = 7_000L

class GastoApiAdapter(
    private val api: ApiClient,
) : GastoRepositorioPort {

    override fun observar(
        hogarId: String,
        desde: String?,
        hasta: String?,
    ): Flow<List<Gasto>> = flow {
        var primeraVez = true
        while (true) {
            try {
                emit(listar(hogarId, desde, hasta))
                primeraVez = false
            } catch (error: Exception) {
                if (primeraVez) throw error
            }
            delay(INTERVALO_POLLING_MS)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun listar(
        hogarId: String,
        desde: String?,
        hasta: String?,
    ): List<Gasto> =
        api.http.get(api.url("/households/$hogarId/expenses${consulta(desde, hasta)}")) {
            autorizacion(api)
        }.decodificar<List<GastoDto>>().map { it.aDominio() }

    override suspend fun listarMeses(hogarId: String): List<ResumenMes> =
        api.http.get(api.url("/households/$hogarId/expenses/months")) {
            autorizacion(api)
        }.decodificar<List<ResumenMesDto>>().map { it.aDominio() }

    override suspend fun listarCiclos(hogarId: String): List<ResumenCiclo> =
        api.http.get(api.url("/households/$hogarId/cycles")) {
            autorizacion(api)
        }.decodificar<List<ResumenCicloDto>>().map { it.aDominio() }

    override suspend fun crear(
        hogarId: String,
        producto: String,
        montoCents: Long,
        fecha: String,
    ): Gasto =
        api.http.post(api.url("/households/$hogarId/expenses")) {
            autorizacion(api)
            contentType(ContentType.Application.Json)
            setBody(GastoRequest(producto = producto, montoCents = montoCents, fecha = fecha))
        }.decodificar<GastoDto>().aDominio()

    override suspend fun actualizar(
        hogarId: String,
        gastoId: String,
        producto: String,
        montoCents: Long,
        fecha: String,
        adminPassword: String,
    ): Gasto =
        api.http.patch(api.url("/households/$hogarId/expenses/$gastoId")) {
            autorizacion(api)
            header("X-Admin-Password", adminPassword)
            contentType(ContentType.Application.Json)
            setBody(GastoRequest(producto = producto, montoCents = montoCents, fecha = fecha))
        }.decodificar<GastoDto>().aDominio()

    override suspend fun eliminar(hogarId: String, gastoId: String, adminPassword: String) {
        api.http.delete(api.url("/households/$hogarId/expenses/$gastoId")) {
            autorizacion(api)
            header("X-Admin-Password", adminPassword)
        }.verificar()
    }

    private fun consulta(desde: String?, hasta: String?): String {
        val partes = buildList {
            desde?.let { add("desde=$it") }
            hasta?.let { add("hasta=$it") }
        }
        return if (partes.isEmpty()) "" else "?" + partes.joinToString("&")
    }
}
