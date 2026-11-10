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
    val personasEstimadas: Int = 0,
    /** Nivel de ingreso del hogar: MUY_BAJO, BAJO, MEDIO o ALTO. */
    val nivelIngreso: String = "MEDIO",
    /** Ingreso mensual expresado en salarios mínimos. */
    val salariosMinimos: Double = 0.0,
    /** Dinero que queda al mes después de los gastos fijos del ciclo. */
    val margenLibreCents: Long = 0,
    val estado: String = "",
    val sugerencias: List<SugerenciaIA> = emptyList(),
    val recomendacion: String = "",
    val cacheado: Boolean = false,
)
