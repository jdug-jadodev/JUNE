package com.andromeda.gastos.domain

import com.andromeda.gastos.domain.rule.GastoReglas
import org.junit.Test

class GastoReglasTest {

    @Test
    fun `acepta un gasto valido`() {
        GastoReglas.validar("Arroz", 3_500_000L, "2026-10-30")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rechaza producto vacio`() {
        GastoReglas.validar("   ", 3_500_000L, "2026-10-30")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rechaza monto cero`() {
        GastoReglas.validar("Arroz", 0L, "2026-10-30")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rechaza fecha invalida`() {
        GastoReglas.validar("Arroz", 3_500_000L, "30/10/2026")
    }
}
