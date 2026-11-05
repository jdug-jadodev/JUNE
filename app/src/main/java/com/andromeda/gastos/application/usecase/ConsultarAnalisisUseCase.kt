package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.Analisis
import com.andromeda.gastos.domain.port.portin.ConsultarAnalisisPort
import com.andromeda.gastos.domain.port.portout.AnalisisRepositorioPort

class ConsultarAnalisisUseCase(
    private val repositorio: AnalisisRepositorioPort,
) : ConsultarAnalisisPort {

    override suspend fun analizar(hogarId: String, ciclos: List<String>?): Analisis =
        repositorio.analizar(hogarId, ciclos)
}
