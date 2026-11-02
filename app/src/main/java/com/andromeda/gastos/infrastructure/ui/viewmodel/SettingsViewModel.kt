package com.andromeda.gastos.infrastructure.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.andromeda.gastos.AppContainer
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.enums.Rol
import com.andromeda.gastos.domain.port.portin.AutenticacionPort
import com.andromeda.gastos.domain.port.portin.ConsultarGastosPort
import com.andromeda.gastos.domain.port.portin.ExportarGastosPort
import com.andromeda.gastos.domain.port.portin.GestionarHogarPort
import com.andromeda.gastos.domain.port.portin.GestionarMiembrosPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val password: String = "",
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null,
    val esAdmin: Boolean = false,
    val cargandoRol: Boolean = true,
    val cutoffDay: Int = 1,
    val corteTexto: String = "1",
    val guardandoCorte: Boolean = false,
    val ciclos: List<ResumenCiclo> = emptyList(),
    val ciclosSeleccionados: Set<String> = emptySet(),
    val todosSeleccionados: Boolean = false,
    val cargandoCiclos: Boolean = true,
)

class SettingsViewModel(
    private val autenticacion: AutenticacionPort,
    private val gestionarHogar: GestionarHogarPort,
    private val gestionarMiembros: GestionarMiembrosPort,
    private val consultarGastos: ConsultarGastosPort,
    private val exportarGastos: ExportarGastosPort,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()

    fun cargarRol(hogarId: String) {
        viewModelScope.launch {
            val esAdmin = runCatching {
                val sesion = autenticacion.sesionActual()
                gestionarMiembros.listar(hogarId).any {
                    it.usuarioId == sesion?.usuario?.id && it.rol == Rol.ADMIN
                }
            }.getOrDefault(false)

            _state.update { it.copy(esAdmin = esAdmin, cargandoRol = false) }
        }
    }

    fun cargarCorte(hogarId: String) {
        viewModelScope.launch {
            runCatching { gestionarHogar.obtener(hogarId) }
                .onSuccess { hogar ->
                    _state.update {
                        it.copy(
                            cutoffDay = hogar.cutoffDay,
                            corteTexto = hogar.cutoffDay.toString(),
                        )
                    }
                }
        }
    }

    fun cargarCiclos(hogarId: String) {
        viewModelScope.launch {
            val ciclos = runCatching {
                consultarGastos.listarCiclos(hogarId)
            }.getOrDefault(emptyList())

            _state.update {
                it.copy(
                    ciclos = ciclos,
                    ciclosSeleccionados = emptySet(),
                    todosSeleccionados = false,
                    cargandoCiclos = false,
                )
            }
        }
    }

    fun onCorte(valor: String) = _state.update {
        it.copy(
            corteTexto = valor.filter { caracter -> caracter.isDigit() }.take(2),
            error = null,
            mensaje = null,
        )
    }

    fun guardarCorte(hogarId: String) {
        if (!_state.value.esAdmin) {
            _state.update {
                it.copy(error = "Solo el administrador puede cambiar el ciclo")
            }
            return
        }

        val dia = _state.value.corteTexto.toIntOrNull()
        if (dia == null || dia !in 1..31) {
            _state.update { it.copy(error = "El día de corte debe estar entre 1 y 31") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(guardandoCorte = true, error = null, mensaje = null) }
            runCatching { gestionarHogar.configurarCorte(hogarId, dia) }
                .onSuccess { hogar ->
                    _state.update {
                        it.copy(
                            guardandoCorte = false,
                            cutoffDay = hogar.cutoffDay,
                            corteTexto = hogar.cutoffDay.toString(),
                            mensaje = "Ciclo actualizado",
                        )
                    }
                    cargarCiclos(hogarId)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            guardandoCorte = false,
                            error = error.message ?: "No se pudo actualizar el ciclo",
                        )
                    }
                }
        }
    }

    fun toggleCiclo(inicio: String) {
        _state.update { actual ->
            val seleccion = if (inicio in actual.ciclosSeleccionados) {
                actual.ciclosSeleccionados - inicio
            } else {
                actual.ciclosSeleccionados + inicio
            }

            actual.copy(
                ciclosSeleccionados = seleccion,
                todosSeleccionados = actual.ciclos.isNotEmpty() &&
                    seleccion.size == actual.ciclos.size,
                error = null,
                mensaje = null,
            )
        }
    }

    fun seleccionarTodos(seleccionado: Boolean) {
        _state.update {
            it.copy(
                ciclosSeleccionados = if (seleccionado) {
                    it.ciclos.map { ciclo -> ciclo.inicio }.toSet()
                } else {
                    emptySet()
                },
                todosSeleccionados = seleccionado,
                error = null,
                mensaje = null,
            )
        }
    }

    fun onPassword(valor: String) = _state.update {
        it.copy(password = valor, error = null, mensaje = null)
    }

    fun guardarPassword(hogarId: String) {
        if (!_state.value.esAdmin) {
            _state.update {
                it.copy(error = "Solo el administrador puede cambiar la contraseña")
            }
            return
        }

        val password = _state.value.password
        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null, mensaje = null) }
            runCatching { gestionarHogar.configurarPassword(hogarId, password) }
                .onSuccess {
                    _state.update {
                        it.copy(
                            guardando = false,
                            password = "",
                            mensaje = "Contraseña de administrador actualizada",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            guardando = false,
                            error = error.message ?: "No se pudo actualizar la contraseña",
                        )
                    }
                }
        }
    }

    fun exportar(hogarId: String) {
        val actual = _state.value
        if (!actual.todosSeleccionados && actual.ciclosSeleccionados.isEmpty()) {
            _state.update { it.copy(error = "Selecciona al menos un ciclo") }
            return
        }

        val ciclos = if (actual.todosSeleccionados) null else actual.ciclosSeleccionados.toList()

        viewModelScope.launch {
            _state.update { it.copy(guardando = true, error = null, mensaje = null) }
            runCatching { exportarGastos.ejecutar(hogarId, ciclos) }
                .onSuccess {
                    _state.update {
                        it.copy(guardando = false, mensaje = "CSV listo para compartir")
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            guardando = false,
                            error = error.message ?: "No se pudo exportar",
                        )
                    }
                }
        }
    }

    fun limpiarMensaje() = _state.update { it.copy(mensaje = null, error = null) }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    autenticacion = container.autenticacion,
                    gestionarHogar = container.gestionarHogar,
                    gestionarMiembros = container.gestionarMiembros,
                    consultarGastos = container.consultarGastos,
                    exportarGastos = container.exportarGastos,
                )
            }
        }
    }
}
