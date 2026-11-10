package com.andromeda.gastos.domain.entity

/**
 * Frecuencia con la que entra el ingreso. El factor convierte el monto a su
 * equivalente mensual (30 días o 52 semanas) para poder compararlo con los
 * gastos del ciclo.
 */
enum class Periodicidad(val etiqueta: String, val factorMensual: Double) {
    MENSUAL("Mensual", 1.0),
    QUINCENAL("Quincenal", 2.0),
    SEMANAL("Semanal", 52.0 / 12.0),
    DIARIO("Diario", 30.0),
}

data class Ingreso(
    val id: String,
    val hogarId: String,
    val nombre: String?,
    val montoCents: Long,
    val periodicidad: Periodicidad,
    val createdAt: String = "",
    val updatedAt: String = "",
)

/** Equivalente mensual estimado de un ingreso. */
fun Ingreso.equivalenteMensualCents(): Long =
    equivalenteMensualCents(montoCents, periodicidad)

/** Equivalente mensual estimado de un monto con una periodicidad dada. */
fun equivalenteMensualCents(montoCents: Long, periodicidad: Periodicidad): Long =
    (montoCents * periodicidad.factorMensual).toLong()
