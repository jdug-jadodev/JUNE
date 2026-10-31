package com.andromeda.gastos.domain.rule

import com.andromeda.gastos.domain.enums.Rol

object PermisoReglas {
    fun esAdmin(rol: Rol): Boolean = rol == Rol.ADMIN

    fun puedeEditar(rol: Rol, passwordValida: Boolean): Boolean =
        rol == Rol.ADMIN && passwordValida

    fun puedeEliminar(rol: Rol, passwordValida: Boolean): Boolean =
        rol == Rol.ADMIN && passwordValida
}
