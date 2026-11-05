package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Analisis
import com.andromeda.gastos.domain.entity.Concepto

interface ConsultarAnalisisPort {
    suspend fun analizar(hogarId: String, ciclos: List<String>? = null): Analisis

    suspend fun conceptos(): List<Concepto>
}
