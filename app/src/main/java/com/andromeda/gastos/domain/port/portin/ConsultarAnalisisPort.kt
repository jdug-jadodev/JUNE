package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Analisis

interface ConsultarAnalisisPort {
    suspend fun analizar(hogarId: String, ciclos: List<String>? = null): Analisis
}
