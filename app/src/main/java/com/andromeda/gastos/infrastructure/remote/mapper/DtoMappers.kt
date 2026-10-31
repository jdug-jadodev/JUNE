package com.andromeda.gastos.infrastructure.remote.mapper

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.entity.Miembro
import com.andromeda.gastos.domain.entity.ResumenMes
import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.domain.entity.Tokens
import com.andromeda.gastos.domain.entity.Usuario
import com.andromeda.gastos.domain.enums.Rol
import com.andromeda.gastos.infrastructure.remote.dto.AuthResponseDto
import com.andromeda.gastos.infrastructure.remote.dto.GastoDto
import com.andromeda.gastos.infrastructure.remote.dto.HogarDto
import com.andromeda.gastos.infrastructure.remote.dto.MiembroDto
import com.andromeda.gastos.infrastructure.remote.dto.ResumenMesDto

fun AuthResponseDto.aDominio(): Sesion = Sesion(
    usuario = Usuario(id = usuario.id, email = usuario.email, nombre = usuario.nombre),
    tokens = Tokens(accessToken = accessToken, refreshToken = refreshToken),
)

fun HogarDto.aDominio(): Hogar = Hogar(
    id = id,
    nombre = nombre,
    ownerId = ownerId,
    inviteCode = inviteCode,
    createdAt = createdAt,
)

fun MiembroDto.aDominio(): Miembro = Miembro(
    id = id,
    hogarId = hogarId,
    usuarioId = usuarioId,
    nombre = nombre.orEmpty(),
    email = email.orEmpty(),
    rol = runCatching { Rol.valueOf(rol) }.getOrDefault(Rol.MIEMBRO),
    joinedAt = joinedAt,
)

fun GastoDto.aDominio(): Gasto = Gasto(
    id = id,
    hogarId = hogarId,
    producto = producto,
    montoCents = montoCents,
    fecha = fecha,
    creadoPor = creadoPor,
    creadoPorNombre = creadoPorNombre,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ResumenMesDto.aDominio(): ResumenMes = ResumenMes(
    mes = mes,
    cantidad = cantidad,
    totalCents = totalCents,
)
