package com.andromeda.gastos.domain.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

fun mesDe(fecha: String): YearMonth? =
    runCatching { YearMonth.from(LocalDate.parse(fecha)) }.getOrNull()

fun nombreMes(mes: YearMonth): String {
    val texto = mes.format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-CO")),
    )
    return texto.replaceFirstChar { caracter ->
        if (caracter.isLowerCase()) {
            caracter.titlecase(Locale.forLanguageTag("es-CO"))
        } else {
            caracter.toString()
        }
    }
}
