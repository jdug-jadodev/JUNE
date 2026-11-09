package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.AnalisisIA

interface AnalisisIARepositorioPort {
    suspend fun analizarConIA(
        hogarId: String,
        ciclos: List<String>?,
        personas: Int? = null,
    ): AnalisisIA
}
