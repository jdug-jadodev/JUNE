package com.andromeda.gastos.domain.port.portout

import com.andromeda.gastos.domain.entity.Analisis

interface AnalisisRepositorioPort {
    suspend fun analizar(hogarId: String, ciclos: List<String>? = null): Analisis
}
