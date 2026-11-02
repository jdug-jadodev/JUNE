package com.andromeda.gastos.domain.entity

import com.andromeda.gastos.domain.enums.Acceso
import com.andromeda.gastos.domain.enums.Rol

data class Miembro(
    val id: String,
    val hogarId: String,
    val usuarioId: String,
    val nombre: String,
    val email: String,
    val rol: Rol,
    val acceso: Acceso = Acceso.ESCRITURA,
    val joinedAt: String,
)
