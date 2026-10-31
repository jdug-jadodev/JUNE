package com.andromeda.gastos.domain.util

import java.text.Normalizer

/**
 * Convierte lo que el usuario dice o escribe a centavos.
 * Acepta dígitos ("35.000", "35000", "35 mil") y palabras ("treinta y cinco mil").
 */
object MontoParser {

    private val unidades: Map<String, Long> = mapOf(
        "cero" to 0, "uno" to 1, "un" to 1, "una" to 1,
        "dos" to 2, "tres" to 3, "cuatro" to 4, "cinco" to 5,
        "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9,
        "diez" to 10, "once" to 11, "doce" to 12, "trece" to 13,
        "catorce" to 14, "quince" to 15, "dieciseis" to 16,
        "diecisiete" to 17, "dieciocho" to 18, "diecinueve" to 19,
        "veinte" to 20, "veintiuno" to 21, "veintiun" to 21,
        "veintidos" to 22, "veintitres" to 23, "veinticuatro" to 24,
        "veinticinco" to 25, "veintiseis" to 26, "veintisiete" to 27,
        "veintiocho" to 28, "veintinueve" to 29,
        "treinta" to 30, "cuarenta" to 40, "cincuenta" to 50,
        "sesenta" to 60, "setenta" to 70, "ochenta" to 80, "noventa" to 90,
        "cien" to 100, "ciento" to 100,
        "doscientos" to 200, "doscientas" to 200,
        "trescientos" to 300, "trescientas" to 300,
        "cuatrocientos" to 400, "cuatrocientas" to 400,
        "quinientos" to 500, "quinientas" to 500,
        "seiscientos" to 600, "seiscientas" to 600,
        "setecientos" to 700, "setecientas" to 700,
        "ochocientos" to 800, "ochocientas" to 800,
        "novecientos" to 900, "novecientas" to 900,
    )

    fun aCentavos(texto: String): Long? {
        val normalizado = normalizar(texto)
        if (normalizado.isBlank()) return null

        val digitos = Regex("""\d[\d.,]*""").find(normalizado)
        if (digitos != null) {
            var pesos = digitos.value.replace(".", "").replace(",", "").toLongOrNull() ?: return null
            if (normalizado.contains("mil")) pesos *= 1_000
            if (normalizado.contains("millon") || normalizado.contains("millones")) pesos *= 1_000_000
            return if (pesos > 0) pesos * 100 else null
        }

        var total = 0L
        var actual = 0L
        var encontrado = false

        for (palabra in normalizado.split(Regex("""\s+"""))) {
            when (palabra) {
                "mil" -> {
                    if (actual == 0L) actual = 1
                    total += actual * 1_000
                    actual = 0
                    encontrado = true
                }

                "millon", "millones" -> {
                    if (actual == 0L) actual = 1
                    total += actual * 1_000_000
                    actual = 0
                    encontrado = true
                }

                "y", "pesos", "peso", "cop", "con", "de" -> Unit

                else -> unidades[palabra]?.let {
                    actual += it
                    encontrado = true
                }
            }
        }

        val pesos = total + actual
        return if (encontrado && pesos > 0) pesos * 100 else null
    }

    private fun normalizar(texto: String): String {
        val sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
        return sinTildes.lowercase().trim()
    }
}
