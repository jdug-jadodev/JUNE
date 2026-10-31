package com.andromeda.gastos.domain.entity

data class Tokens(
    val accessToken: String,
    val refreshToken: String,
)

data class Sesion(
    val usuario: Usuario,
    val tokens: Tokens,
)
