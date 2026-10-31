package com.andromeda.gastos.infrastructure.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.andromeda.gastos.domain.port.portout.ExportacionLocalPort
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CsvShareAdapter(
    private val context: Context,
) : ExportacionLocalPort {

    override suspend fun compartirCsv(nombreArchivo: String, contenido: String) {
        val archivo = withContext(Dispatchers.IO) {
            val carpeta = File(context.cacheDir, "exports").apply { mkdirs() }
            File(carpeta, nombreArchivo).apply { writeText(contenido) }
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo,
        )

        val enviar = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val selector = Intent.createChooser(enviar, "Compartir CSV").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(selector)
    }
}
