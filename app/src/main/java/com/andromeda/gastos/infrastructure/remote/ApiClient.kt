package com.andromeda.gastos.infrastructure.remote

import com.andromeda.gastos.domain.port.portout.SesionLocalPort
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiClient(
    private val baseUrl: String,
    private val sesion: SesionLocalPort,
) {
    val http: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                    isLenient = true
                },
            )
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }

    fun url(path: String): String = "$baseUrl$path"

    suspend fun token(): String? = sesion.obtener()?.tokens?.accessToken
}
