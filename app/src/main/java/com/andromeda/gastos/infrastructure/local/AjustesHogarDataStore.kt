package com.andromeda.gastos.infrastructure.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.andromeda.gastos.domain.port.portout.AjustesHogarLocalPort
import kotlinx.coroutines.flow.first

private val Context.ajustesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "cauce_ajustes",
)

/**
 * Ajustes del hogar que se quedan en el teléfono. Hoy: cuántas personas viven en
 * casa. No viaja a nuestra base de datos; solo se envía en la petición de
 * análisis para que la IA lo tenga en cuenta en ese momento.
 */
class AjustesHogarDataStore(private val context: Context) : AjustesHogarLocalPort {

    override suspend fun obtenerPersonas(hogarId: String): Int? {
        val prefs = context.ajustesDataStore.data.first()
        return prefs[clavePersonas(hogarId)]
    }

    override suspend fun guardarPersonas(hogarId: String, personas: Int?) {
        context.ajustesDataStore.edit { prefs ->
            if (personas == null) {
                prefs.remove(clavePersonas(hogarId))
            } else {
                prefs[clavePersonas(hogarId)] = personas.coerceIn(1, 15)
            }
        }
    }

    private fun clavePersonas(hogarId: String) = intPreferencesKey("personas_hogar_$hogarId")
}
