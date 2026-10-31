package com.andromeda.gastos.domain.entity

data class Gasto(
    val id: String,
    val hogarId: String,
    val producto: String,
    val montoCents: Long,
    val fecha: String,
    val creadoPor: String,
    val creadoPorNombre: String,
    val createdAt: String,
    val updatedAt: String,
)
