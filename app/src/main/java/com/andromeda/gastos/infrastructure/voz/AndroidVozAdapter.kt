package com.andromeda.gastos.infrastructure.voz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.andromeda.gastos.domain.port.portout.VozPort
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class AndroidVozAdapter(
    private val context: Context,
) : VozPort {

    private val ttsListo = CompletableDeferred<TextToSpeech>()
    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null

    private fun asegurarTts() {
        if (tts != null) return
        tts = TextToSpeech(context) { estado ->
            val actual = tts
            if (estado == TextToSpeech.SUCCESS && actual != null) {
                actual.language = Locale.getDefault()
                ttsListo.complete(actual)
            } else {
                ttsListo.completeExceptionally(IllegalStateException("No se pudo iniciar la voz"))
            }
        }
    }

    override suspend fun hablar(texto: String) {
        withContext(Dispatchers.Main) { asegurarTts() }
        val motor = ttsListo.await()
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                val id = UUID.randomUUID().toString()
                motor.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = Unit

                        override fun onDone(utteranceId: String?) {
                            if (cont.isActive) cont.resume(Unit)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            if (cont.isActive) cont.resume(Unit)
                        }
                    },
                )
                motor.speak(texto, TextToSpeech.QUEUE_FLUSH, null, id)
            }
        }
    }

    override suspend fun escuchar(): String = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            val sr = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer = sr

            sr.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) = Unit
                    override fun onBeginningOfSpeech() = Unit
                    override fun onRmsChanged(rmsdB: Float) = Unit
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() = Unit
                    override fun onPartialResults(partialResults: Bundle?) = Unit
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit

                    override fun onError(error: Int) {
                        sr.destroy()
                        recognizer = null
                        if (cont.isActive) cont.resume("")
                    }

                    override fun onResults(results: Bundle?) {
                        val texto = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            .orEmpty()
                        sr.destroy()
                        recognizer = null
                        if (cont.isActive) cont.resume(texto)
                    }
                },
            )

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora")
            }
            sr.startListening(intent)

            cont.invokeOnCancellation {
                sr.destroy()
                recognizer = null
            }
        }
    }

    override fun detener() {
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
    }
}
