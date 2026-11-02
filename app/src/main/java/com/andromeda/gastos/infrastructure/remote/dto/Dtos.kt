package com.andromeda.gastos.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: String,
    val email: String,
    val nombre: String,
    val createdAt: String = "",
)

@Serializable
data class AuthResponseDto(
    val usuario: UsuarioDto,
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class HogarDto(
    val id: String,
    val nombre: String,
    val ownerId: String,
    val inviteCode: String,
    val cutoffDay: Int = 1,
    val createdAt: String = "",
)

@Serializable
data class MiembroDto(
    val id: String,
    val hogarId: String,
    val usuarioId: String,
    val nombre: String? = null,
    val email: String? = null,
    val rol: String,
    val joinedAt: String = "",
)

@Serializable
data class GastoDto(
    val id: String,
    val hogarId: String,
    val producto: String,
    val montoCents: Long,
    val fecha: String,
    val creadoPor: String,
    val creadoPorNombre: String,
    val createdAt: String = "",
    val updatedAt: String = "",
)

@Serializable
data class ResumenMesDto(
    val mes: String,
    val cantidad: Int,
    val totalCents: Long,
)

@Serializable
data class ResumenCicloDto(
    val inicio: String,
    val fin: String,
    val cantidad: Int,
    val totalCents: Long,
)

@Serializable
data class RegistroRequest(
    val email: String,
    val password: String,
    val nombre: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class CrearHogarRequest(
    val nombre: String,
)

@Serializable
data class UnirseRequest(
    val inviteCode: String,
)

@Serializable
data class ConfigurarPasswordRequest(
    val password: String,
)

@Serializable
data class ConfigurarCorteRequest(
    val cutoffDay: Int,
)

@Serializable
data class GastoRequest(
    val producto: String,
    val montoCents: Long,
    val fecha: String,
)

@Serializable
data class ErrorDto(
    val code: String? = null,
    val message: String? = null,
)
