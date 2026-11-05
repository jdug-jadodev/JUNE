package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.port.portin.ClasificarTerminoPort
import com.andromeda.gastos.domain.port.portout.AnalisisRepositorioPort

class ClasificarTerminoUseCase(
    private val repositorio: AnalisisRepositorioPort,
) : ClasificarTerminoPort {

    override suspend fun clasificar(
        hogarId: String,
        termino: String,
        conceptoId: String,
        nota: String?,
    ) = repositorio.clasificar(hogarId, termino, conceptoId, nota)
}
