package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.AnalisisIA
import com.andromeda.gastos.domain.port.portin.AnalizarConIAPort
import com.andromeda.gastos.domain.port.portout.AjustesHogarLocalPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalisisIAUiState(
    val cargando: Boolean = false,
    val analisis: AnalisisIA? = null,
    val error: String? = null,
    /** Personas en casa ajustadas en este teléfono; null = deducción automática. */
    val personasLocales: Int? = null,
    val dialogoPersonas: Boolean = false,
)

class AnalisisIAViewModel(
    private val analizarConIA: AnalizarConIAPort,
    private val ajustesHogar: AjustesHogarLocalPort,
) : ViewModel() {

    private val _state = MutableStateFlow(AnalisisIAUiState())
    val state = _state.asStateFlow()

    private var iniciado = false

    fun iniciar(hogarId: String, inicio: String?) {
        if (iniciado) return
        iniciado = true
        viewModelScope.launch {
            val personas = runCatching { ajustesHogar.obtenerPersonas(hogarId) }.getOrNull()
            _state.update { it.copy(personasLocales = personas) }
            analizar(hogarId, inicio)
        }
    }

    fun analizar(hogarId: String, inicio: String?) {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            runCatching {
                analizarConIA.analizarConIA(
                    hogarId = hogarId,
                    ciclos = inicio?.let { listOf(it) },
                    personas = _state.value.personasLocales,
                )
            }
                .onSuccess { resultado ->
                    _state.update { it.copy(cargando = false, analisis = resultado) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(cargando = false, error = error.message ?: "No se pudo analizar")
                    }
                }
        }
    }

    fun abrirDialogoPersonas() {
        _state.update { it.copy(dialogoPersonas = true) }
    }

    fun cerrarDialogoPersonas() {
        _state.update { it.copy(dialogoPersonas = false) }
    }

    /** Guarda solo en el teléfono. null vuelve a la deducción automática. */
    fun guardarPersonas(hogarId: String, inicio: String?, personas: Int?) {
        viewModelScope.launch {
            val anterior = _state.value.personasLocales
            runCatching { ajustesHogar.guardarPersonas(hogarId, personas) }
            _state.update { it.copy(personasLocales = personas, dialogoPersonas = false) }
            if (anterior != personas) {
                analizar(hogarId, inicio)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AnalisisIAViewModel(
                    analizarConIA = container.analizarConIA,
                    ajustesHogar = container.ajustesHogarLocal,
                )
            }
        }
    }
}
