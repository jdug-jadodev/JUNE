package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.port.portin.RegistrarGastoPorVozPort
import com.andromeda.gastos.domain.port.portin.RegistrarGastoPort
import com.andromeda.gastos.domain.port.portout.VozPort
import com.andromeda.gastos.domain.util.MontoParser
import com.andromeda.gastos.domain.util.formatearMonto
import com.andromeda.gastos.domain.util.formatearMontoVoz
import java.text.Normalizer
import java.time.LocalDate

class RegistrarGastoPorVozUseCase(
    private val voz: VozPort,
    private val registrarGasto: RegistrarGastoPort,
) : RegistrarGastoPorVozPort {

    override suspend fun ejecutar(hogarId: String, onMensaje: (String) -> Unit): Gasto? {
        var producto = pedirProducto(onMensaje) ?: return null
        var montoCents = pedirMonto(onMensaje) ?: return null

        repeat(4) {
            onMensaje(
                "¿Guardo la compra de $producto por ${formatearMonto(montoCents)}?\n" +
                    "Puedes decir: sí, corrige el valor o corrige la compra.",
            )
            voz.hablar(
                "¿Guardo la compra de $producto por ${formatearMontoVoz(montoCents)}?",
            )
            val respuesta = normalizar(voz.escuchar())

            when {
                esCorreccionValor(respuesta) -> {
                    montoCents = pedirMonto(onMensaje) ?: return null
                }

                esCorreccionCompra(respuesta) -> {
                    producto = pedirProducto(onMensaje) ?: return null
                }

                esConfirmacion(respuesta) -> {
                    val gasto = registrarGasto.ejecutar(
                        hogarId = hogarId,
                        producto = producto,
                        montoCents = montoCents,
                        fecha = LocalDate.now().toString(),
                    )
                    onMensaje("Registrado.")
                    voz.hablar("Registrado.")
                    return gasto
                }

                esCancelacion(respuesta) -> {
                    onMensaje("Cancelado.")
                    voz.hablar("Cancelado.")
                    return null
                }

                else -> {
                    onMensaje("No entendí. Di sí, corrige el valor o corrige la compra.")
                    voz.hablar("No entendí. ¿Guardo la compra?")
                }
            }
        }

        onMensaje("No se pudo confirmar. Intenta de nuevo.")
        voz.hablar("No se pudo confirmar. Intenta de nuevo.")
        return null
    }

    private suspend fun pedirProducto(onMensaje: (String) -> Unit): String? {
        repeat(2) { intento ->
            val pregunta = if (intento == 0) {
                "¿Qué compraste?"
            } else {
                "No te escuché. ¿Qué compraste?"
            }
            onMensaje(pregunta)
            voz.hablar(pregunta)
            val producto = voz.escuchar().trim()
            if (producto.isNotBlank()) return producto
        }

        onMensaje("No se escuchó el producto.")
        voz.hablar("No se escuchó el producto. Intenta de nuevo.")
        return null
    }

    private suspend fun pedirMonto(onMensaje: (String) -> Unit): Long? {
        repeat(2) { intento ->
            onMensaje("¿Cuánto costó en pesos? (ej. siete mil)")
            voz.hablar(
                if (intento == 0) {
                    "¿Cuánto costó en pesos?"
                } else {
                    "No entendí. ¿Cuánto costó en pesos?"
                },
            )
            val monto = MontoParser.aCentavos(voz.escuchar())
            if (monto != null) return monto
        }

        onMensaje("No se entendió el monto.")
        voz.hablar("No se entendió el monto. Intenta de nuevo.")
        return null
    }

    private fun esConfirmacion(texto: String): Boolean =
        contienePalabra(texto, "si") ||
            listOf("correcto", "dale", "ok", "confirmo", "registra", "registralo")
                .any { texto.contains(it) }

    private fun esCancelacion(texto: String): Boolean =
        contienePalabra(texto, "no") ||
            listOf("cancela", "cancelar", "olvidalo", "olvidar")
                .any { texto.contains(it) }

    private fun esCorreccionValor(texto: String): Boolean =
        listOf(
            "corrige el valor",
            "corrige el precio",
            "cambia el valor",
            "cambia el precio",
            "otro valor",
            "otro precio",
            "corregir valor",
            "corregir precio",
        ).any { texto.contains(it) }

    private fun esCorreccionCompra(texto: String): Boolean =
        listOf(
            "corrige la compra",
            "corrige el producto",
            "cambia la compra",
            "cambia el producto",
            "otra compra",
            "corregir compra",
            "corregir producto",
        ).any { texto.contains(it) }

    private fun contienePalabra(texto: String, palabra: String): Boolean =
        Regex("""\b${Regex.escape(palabra)}\b""").containsMatchIn(texto)

    private fun normalizar(texto: String): String {
        val sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
        return sinTildes.lowercase().trim()
    }
}
