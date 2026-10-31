package com.andromeda.gastos.infrastructure.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.domain.entity.Tokens
import com.andromeda.gastos.domain.entity.Usuario
import com.andromeda.gastos.domain.port.portout.SesionLocalPort
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "andromeda_sesion")

class SesionDataStore(private val context: Context) : SesionLocalPort {

    private object Claves {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val USUARIO_ID = stringPreferencesKey("usuario_id")
        val EMAIL = stringPreferencesKey("email")
        val NOMBRE = stringPreferencesKey("nombre")
    }

    override suspend fun guardar(sesion: Sesion) {
        context.dataStore.edit { prefs ->
            prefs[Claves.ACCESS] = sesion.tokens.accessToken
            prefs[Claves.REFRESH] = sesion.tokens.refreshToken
            prefs[Claves.USUARIO_ID] = sesion.usuario.id
            prefs[Claves.EMAIL] = sesion.usuario.email
            prefs[Claves.NOMBRE] = sesion.usuario.nombre
        }
    }

    override suspend fun obtener(): Sesion? {
        val prefs = context.dataStore.data.first()
        val access = prefs[Claves.ACCESS] ?: return null
        val refresh = prefs[Claves.REFRESH] ?: return null
        val id = prefs[Claves.USUARIO_ID] ?: return null
        val email = prefs[Claves.EMAIL] ?: return null
        val nombre = prefs[Claves.NOMBRE] ?: return null
        return Sesion(
            usuario = Usuario(id = id, email = email, nombre = nombre),
            tokens = Tokens(accessToken = access, refreshToken = refresh),
        )
    }

    override suspend fun limpiar() {
        context.dataStore.edit { it.clear() }
    }
}
