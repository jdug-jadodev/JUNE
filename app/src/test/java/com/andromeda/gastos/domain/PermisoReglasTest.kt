package com.andromeda.gastos.domain

import com.andromeda.gastos.domain.enums.Rol
import com.andromeda.gastos.domain.rule.PermisoReglas
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermisoReglasTest {

    @Test
    fun `admin con contrasena valida puede editar y eliminar`() {
        assertTrue(PermisoReglas.puedeEditar(Rol.ADMIN, true))
        assertTrue(PermisoReglas.puedeEliminar(Rol.ADMIN, true))
    }

    @Test
    fun `admin sin contrasena valida no puede`() {
        assertFalse(PermisoReglas.puedeEditar(Rol.ADMIN, false))
        assertFalse(PermisoReglas.puedeEliminar(Rol.ADMIN, false))
    }

    @Test
    fun `un miembro nunca puede aunque la contrasena sea valida`() {
        assertFalse(PermisoReglas.puedeEditar(Rol.MIEMBRO, true))
        assertFalse(PermisoReglas.puedeEliminar(Rol.MIEMBRO, true))
    }
}
