package com.andromeda.gastos.domain.port.portout

interface ExportacionRemotaPort {
    suspend fun descargarCsv(hogarId: String, ciclos: List<String>? = null): String
}

interface ExportacionLocalPort {
    suspend fun compartirCsv(nombreArchivo: String, contenido: String)
}
