package com.andromeda.gastos.infrastructure.local

import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.entity.Miembro
import com.andromeda.gastos.domain.port.portout.CacheDatosPort

/**
 * Caché en memoria para datos que cambian poco (hogar y miembros).
 * Los gastos no se cachean porque deben refrescarse en tiempo real.
 */
class CacheDatos : CacheDatosPort {

    private val lock = Any()
    private var hogares: List<Hogar>? = null
    private var hogaresHasta: Long = 0L
    private val miembros = mutableMapOf<String, EntradaMiembros>()

    fun hogares(): List<Hogar>? = synchronized(lock) {
        val datos = hogares
        if (datos != null && System.currentTimeMillis() <= hogaresHasta) {
            datos
        } else {
            hogares = null
            hogaresHasta = 0L
            null
        }
    }

    fun guardarHogares(lista: List<Hogar>) {
        synchronized(lock) {
            hogares = lista
            hogaresHasta = System.currentTimeMillis() + TTL_HOGARES_MS
        }
    }

    fun invalidarHogares() {
        synchronized(lock) {
            hogares = null
            hogaresHasta = 0L
        }
    }

    fun miembros(hogarId: String): List<Miembro>? = synchronized(lock) {
        val entrada = miembros[hogarId]
        if (entrada != null && System.currentTimeMillis() <= entrada.hasta) {
            entrada.lista
        } else {
            miembros.remove(hogarId)
            null
        }
    }

    fun guardarMiembros(hogarId: String, lista: List<Miembro>) {
        synchronized(lock) {
            miembros[hogarId] = EntradaMiembros(
                lista = lista,
                hasta = System.currentTimeMillis() + TTL_MIEMBROS_MS,
            )
        }
    }

    fun invalidarMiembros(hogarId: String) {
        synchronized(lock) {
            miembros.remove(hogarId)
        }
    }

    override fun invalidarTodo() {
        synchronized(lock) {
            hogares = null
            hogaresHasta = 0L
            miembros.clear()
        }
    }

    private data class EntradaMiembros(
        val lista: List<Miembro>,
        val hasta: Long,
    )

    companion object {
        private const val TTL_HOGARES_MS = 5 * 60 * 1000L
        private const val TTL_MIEMBROS_MS = 2 * 60 * 1000L
    }
}
