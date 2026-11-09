package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.AnalisisIA

interface AnalizarConIAPort {
    suspend fun analizarConIA(
        hogarId: String,
        ciclos: List<String>?,
        personas: Int? = null,
    ): AnalisisIA
}
