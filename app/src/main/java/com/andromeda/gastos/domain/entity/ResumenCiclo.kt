package com.andromeda.gastos.domain.entity

data class ResumenCiclo(
    val inicio: String,
    val fin: String,
    val cantidad: Int,
    val totalCents: Long,
)
