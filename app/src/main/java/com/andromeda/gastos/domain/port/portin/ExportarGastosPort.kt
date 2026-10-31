package com.andromeda.gastos.domain.port.portin

interface ExportarGastosPort {
    suspend fun ejecutar(hogarId: String, meses: List<String>? = null)
}
