package com.andromeda.gastos.application.usecase

import com.andromeda.gastos.domain.port.portin.ExportarGastosPort
import com.andromeda.gastos.domain.port.portout.ExportacionLocalPort
import com.andromeda.gastos.domain.port.portout.ExportacionRemotaPort

class ExportarGastosUseCase(
    private val remoto: ExportacionRemotaPort,
    private val local: ExportacionLocalPort,
) : ExportarGastosPort {

    override suspend fun ejecutar(hogarId: String, meses: List<String>?) {
        val csv = remoto.descargarCsv(hogarId, meses)
        local.compartirCsv("gastos_andromeda.csv", csv)
    }
}
