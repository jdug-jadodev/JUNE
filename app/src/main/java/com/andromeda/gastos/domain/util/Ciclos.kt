package com.andromeda.gastos.domain.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Ciclo(
    val inicio: LocalDate,
    val fin: LocalDate,
) {
    val inicioIso: String get() = inicio.toString()
    val finIso: String get() = fin.toString()
}

fun corteEfectivo(year: Int, month: Int, corte: Int): Int {
    val ultimo = YearMonth.of(year, month).lengthOfMonth()
    return minOf(corte, ultimo)
}

fun cicloDe(fecha: LocalDate, corte: Int): Ciclo {
    val corteEsteMes = corteEfectivo(fecha.year, fecha.monthValue, corte)

    val inicio = if (fecha.dayOfMonth >= corteEsteMes) {
        LocalDate.of(fecha.year, fecha.monthValue, corteEsteMes)
    } else {
        val anterior = YearMonth.from(fecha).minusMonths(1)
        LocalDate.of(
            anterior.year,
            anterior.monthValue,
            corteEfectivo(anterior.year, anterior.monthValue, corte),
        )
    }

    val siguiente = YearMonth.from(inicio).plusMonths(1)
    val inicioSiguiente = LocalDate.of(
        siguiente.year,
        siguiente.monthValue,
        corteEfectivo(siguiente.year, siguiente.monthValue, corte),
    )

    return Ciclo(inicio = inicio, fin = inicioSiguiente.minusDays(1))
}

fun cicloActual(corte: Int, hoy: LocalDate = LocalDate.now()): Ciclo =
    cicloDe(hoy, corte)

private val formatoDiaMes = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-CO"))

fun etiquetaCiclo(ciclo: Ciclo): String {
    val inicio = ciclo.inicio.format(formatoDiaMes)
    val fin = ciclo.fin.format(formatoDiaMes)

    return if (ciclo.inicio.year == ciclo.fin.year) {
        "$inicio – $fin ${ciclo.fin.year}"
    } else {
        "$inicio ${ciclo.inicio.year} – $fin ${ciclo.fin.year}"
    }
}
