package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Analisis
import com.andromeda.gastos.domain.entity.Concepto

interface AnalisisRepositorioPort {
    suspend fun analizar(hogarId: String, ciclos: List<String>? = null): Analisis

    suspend fun conceptos(): List<Concepto>

    suspend fun clasificar(
        hogarId: String,
        termino: String,
        conceptoId: String,
        nota: String?,
    )
}
