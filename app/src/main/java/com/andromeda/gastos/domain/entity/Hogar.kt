package com.andromeda.gastos.domain.entity

data class Hogar(
    val id: String,
    val nombre: String,
    val ownerId: String,
    val inviteCode: String,
    val cutoffDay: Int = 1,
    val createdAt: String,
    val plan: String = "FREE",
)
