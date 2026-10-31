package com.andromeda.gastos.infrastructure.widget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.andromeda.gastos.AndromedaApp
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.infrastructure.ui.theme.AndromedaTheme
import kotlinx.coroutines.delay

class VoiceCaptureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(false)

        val container = (application as AndromedaApp).container
        setContent {
            AndromedaTheme {
                VoiceCaptureScreen(container, onFinish = { finish() })
            }
        }
    }

    override fun onDestroy() {
        (application as AndromedaApp).container.detenerVoz()
        super.onDestroy()
    }
}

@Composable
private fun VoiceCaptureScreen(
    container: AppContainer,
    onFinish: () -> Unit,
) {
    val contexto = LocalContext.current
    var mensaje by remember { mutableStateOf("Preparando micrófono…") }
    var activo by remember { mutableStateOf(false) }
    var permiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                contexto,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val lanzador = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { concedido ->
        permiso = concedido
        if (!concedido) mensaje = "Permiso de micrófono requerido"
    }

    LaunchedEffect(Unit) {
        if (!permiso) lanzador.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(permiso) {
        if (!permiso) return@LaunchedEffect

        activo = true
        val sesion = container.autenticacion.sesionActual()
        if (sesion == null) {
            mensaje = "Inicia sesión en la app"
            delay(2_000)
            onFinish()
            return@LaunchedEffect
        }

        val hogar = runCatching { container.gestionarHogar.listar().firstOrNull() }.getOrNull()
        if (hogar == null) {
            mensaje = "Configura tu hogar en la app"
            delay(2_000)
            onFinish()
            return@LaunchedEffect
        }

        val gasto = runCatching {
            container.registrarGastoPorVoz.ejecutar(hogar.id) { nuevo -> mensaje = nuevo }
        }.onFailure { error ->
            mensaje = error.message ?: "No se pudo registrar"
        }.getOrNull()

        activo = false
        if (gasto != null) {
            mensaje = "Gasto registrado"
            delay(1_500)
        } else {
            if (mensaje == "Preparando micrófono…") mensaje = "Cancelado"
            delay(1_200)
        }
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "June · Registrar por voz",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(mensaje, style = MaterialTheme.typography.bodyMedium)
                if (activo) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
                OutlinedButton(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
