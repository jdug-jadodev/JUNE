package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.entity.AnalisisIA
import com.andromeda.gastos.domain.port.portin.AnalizarConIAPort
import com.andromeda.gastos.domain.port.portout.AnalisisIARepositorioPort

class AnalizarConIAUseCase(
    private val repositorio: AnalisisIARepositorioPort,
) : AnalizarConIAPort {

    override suspend fun analizarConIA(
        hogarId: String,
        ciclos: List<String>?,
        personas: Int?,
    ): AnalisisIA = repositorio.analizarConIA(hogarId, ciclos, personas)
}
