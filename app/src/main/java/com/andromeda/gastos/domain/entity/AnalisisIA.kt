package com.andromeda.gastos.domain.entity

data class ProgresoCicloIA(
    val diasTranscurridos: Int,
    val diasTotales: Int,
    val diasRestantes: Int,
    val totalCents: Long,
    val ingresoMensualCents: Long,
    val porcentajeIngreso: Int,
)

data class SugerenciaIA(
    val categoria: String,
    val montoCents: Long,
    val periodicidad: String,
    val nota: String,
)

data class AnalisisIA(
    val ciclos: List<String> = emptyList(),
    val progreso: ProgresoCicloIA? = null,
    val estado: String = "",
    val sugerencias: List<SugerenciaIA> = emptyList(),
    val recomendacion: String = "",
    val cacheado: Boolean = false,
)
