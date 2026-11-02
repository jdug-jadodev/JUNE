package com.andromeda.gastos.domain

import com.andromeda.gastos.domain.util.Ciclo
import com.andromeda.gastos.domain.util.cicloDe
import com.andromeda.gastos.domain.util.etiquetaCiclo
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CiclosTest {

    @Test
    fun `corte 1 equivale al mes calendario`() {
        assertEquals(
            Ciclo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)),
            cicloDe(LocalDate.of(2026, 10, 31), 1),
        )
    }

    @Test
    fun `corte 29 agrupa el 31 de octubre y el 2 de noviembre`() {
        val esperado = Ciclo(LocalDate.of(2026, 10, 29), LocalDate.of(2026, 11, 28))

        assertEquals(esperado, cicloDe(LocalDate.of(2026, 10, 31), 29))
        assertEquals(esperado, cicloDe(LocalDate.of(2026, 11, 2), 29))
    }

    @Test
    fun `el dia antes del corte pertenece al ciclo anterior`() {
        assertEquals(
            Ciclo(LocalDate.of(2026, 9, 29), LocalDate.of(2026, 10, 28)),
            cicloDe(LocalDate.of(2026, 10, 28), 29),
        )
    }

    @Test
    fun `corte 31 se ajusta al ultimo dia de febrero`() {
        val esperado = Ciclo(LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 30))

        assertEquals(esperado, cicloDe(LocalDate.of(2026, 2, 28), 31))
        assertEquals(esperado, cicloDe(LocalDate.of(2026, 3, 1), 31))
    }

    @Test
    fun `corte 15 maneja la segunda quincena`() {
        assertEquals(
            Ciclo(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 12, 14)),
            cicloDe(LocalDate.of(2026, 11, 15), 15),
        )
        assertEquals(
            Ciclo(LocalDate.of(2026, 10, 15), LocalDate.of(2026, 11, 14)),
            cicloDe(LocalDate.of(2026, 11, 14), 15),
        )
    }

    @Test
    fun `etiqueta muestra el rango del ciclo`() {
        val ciclo = Ciclo(LocalDate.of(2026, 10, 29), LocalDate.of(2026, 11, 28))
        assertEquals("29 oct – 28 nov 2026", etiquetaCiclo(ciclo))
    }
}
