package com.andromeda.gastos.domain.entity

data class AnalisisIA(
    val texto: String,
    val ciclos: List<String> = emptyList(),
    val generadoEl: String = "",
    val cacheado: Boolean = false,
)
