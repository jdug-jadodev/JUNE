package com.andromeda.gastos.domain.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatearMonto(montoCents: Long): String {
    val formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"))
    formato.maximumFractionDigits = if (montoCents % 100 == 0L) 0 else 2
    formato.minimumFractionDigits = 0
    return "$ " + formato.format(montoCents / 100.0) + " COP"
}

fun formatearMontoVoz(montoCents: Long): String {
    val pesos = montoCents / 100
    val centavos = montoCents % 100
    return if (centavos == 0L) {
        "$pesos pesos colombianos"
    } else {
        "$pesos pesos con $centavos centavos"
    }
}

fun formatearFecha(iso: String): String {
    return runCatching {
        LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrDefault(iso)
}
