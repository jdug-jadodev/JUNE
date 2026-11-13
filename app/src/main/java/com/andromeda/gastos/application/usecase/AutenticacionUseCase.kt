package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.domain.port.portin.AutenticacionPort
import com.andromeda.gastos.domain.port.portout.AutenticacionRemotaPort
import com.andromeda.gastos.domain.port.portout.CacheDatosPort
import com.andromeda.gastos.domain.port.portout.SesionLocalPort
import com.andromeda.gastos.exception.AppException

class AutenticacionUseCase(
    private val remoto: AutenticacionRemotaPort,
    private val local: SesionLocalPort,
    private val cache: CacheDatosPort,
) : AutenticacionPort {

    override suspend fun registrar(email: String, password: String, nombre: String): Sesion {
        val sesion = remoto.registrar(email, password, nombre)
        cache.invalidarTodo()
        local.guardar(sesion)
        return sesion
    }

    override suspend fun login(email: String, password: String): Sesion {
        val sesion = remoto.login(email, password)
        cache.invalidarTodo()
        local.guardar(sesion)
        return sesion
    }

    override suspend fun logout() {
        local.limpiar()
        cache.invalidarTodo()
    }

    override suspend fun sesionActual(): Sesion? = local.obtener()

    /**
     * Devuelve la sesión lista para usar: renueva el token de acceso con el
     * refresh token guardado. El access token dura 7 días, así que sin esto la
     * app se queda "muerta" con "Token inválido" aunque el refresh siga vivo.
     *
     * - Si la renovación funciona: guarda los tokens nuevos y devuelve la sesión.
     * - Si el backend rechaza el refresh (401): borra la sesión local.
     * - Si no hay red: conserva la sesión guardada para no expulsar sin motivo.
     */
    override suspend fun sesionValida(): Sesion? {
        val actual = local.obtener() ?: return null
        return try {
            val renovada = remoto.refrescar(actual.tokens.refreshToken)
            cache.invalidarTodo()
            local.guardar(renovada)
            renovada
        } catch (e: AppException) {
            if (e.codigo == 401) {
                local.limpiar()
                cache.invalidarTodo()
                null
            } else {
                actual
            }
        } catch (_: Exception) {
            actual
        }
    }
}
