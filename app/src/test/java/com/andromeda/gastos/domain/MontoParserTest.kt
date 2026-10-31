package com.andromeda.gastos.domain

import com.andromeda.gastos.domain.util.MontoParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MontoParserTest {

    @Test
    fun `interpreta digitos con separador de miles`() {
        assertEquals(3_500_000L, MontoParser.aCentavos("35.000"))
    }

    @Test
    fun `interpreta digitos sin separador`() {
        assertEquals(3_500_000L, MontoParser.aCentavos("35000"))
    }

    @Test
    fun `interpreta mil`() {
        assertEquals(3_500_000L, MontoParser.aCentavos("35 mil"))
    }

    @Test
    fun `interpreta palabras`() {
        assertEquals(3_500_000L, MontoParser.aCentavos("treinta y cinco mil"))
    }

    @Test
    fun `interpreta un millon`() {
        assertEquals(100_000_000L, MontoParser.aCentavos("un millón"))
    }

    @Test
    fun `rechaza texto sin numero`() {
        assertNull(MontoParser.aCentavos("hola"))
    }
}
