package com.andromeda.gastos.domain.entity

data class Concepto(
    val id: String,
    val nombre: String,
    val categoria: String,
    val categoriaNombre: String,
    val esGusto: Boolean,
)
