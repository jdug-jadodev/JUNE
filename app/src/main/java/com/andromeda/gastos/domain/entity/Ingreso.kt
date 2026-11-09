package com.andromeda.gastos.domain.entity

enum class Periodicidad(val etiqueta: String) {
    MENSUAL("Mensual"),
    QUINCENAL("Quincenal"),
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
