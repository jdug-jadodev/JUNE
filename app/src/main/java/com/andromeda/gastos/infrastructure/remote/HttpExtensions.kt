package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.exception.AppException
import com.andromeda.gastos.infrastructure.remote.dto.ErrorDto
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

suspend fun HttpResponse.verificar() {
    if (!status.isSuccess()) {
        val mensaje = runCatching { body<ErrorDto>().message }.getOrNull()
        throw AppException(mensaje ?: "Error ${status.value}")
    }
}

suspend inline fun <reified T> HttpResponse.decodificar(): T {
    verificar()
    return body()
}

suspend fun HttpRequestBuilder.autorizacion(api: ApiClient) {
    api.token()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
}
