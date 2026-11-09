package com.andromeda.gastos.domain.port.portout

/**
 * Preferencias que viven solo en este teléfono y nunca se envían a la base de datos.
 */
interface AjustesHogarLocalPort {
    /** Personas en casa ajustadas por la persona, o null si se usa la deducción automática. */
    suspend fun obtenerPersonas(hogarId: String): Int?

    /** Guarda el ajuste local. null restablece la deducción automática. */
    suspend fun guardarPersonas(hogarId: String, personas: Int?)
}
