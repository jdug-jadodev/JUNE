package com.andromeda.gastos.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: String,
    val email: String,
    val nombre: String,
    val createdAt: String = "",
)

@Serializable
data class AuthResponseDto(
    val usuario: UsuarioDto,
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
data class HogarDto(
    val id: String,
    val nombre: String,
    val ownerId: String,
    val inviteCode: String,
    val cutoffDay: Int = 1,
    val createdAt: String = "",
    val plan: String = "FREE",
)

@Serializable
data class MiembroDto(
    val id: String,
    val hogarId: String,
    val usuarioId: String,
    val nombre: String? = null,
    val email: String? = null,
    val rol: String,
    val acceso: String = "ESCRITURA",
    val joinedAt: String = "",
)

@Serializable
data class GastoDto(
    val id: String,
    val hogarId: String,
    val producto: String,
    val montoCents: Long,
    val fecha: String,
    val creadoPor: String,
    val creadoPorNombre: String,
    val createdAt: String = "",
    val updatedAt: String = "",
)

@Serializable
data class ResumenMesDto(
    val mes: String,
    val cantidad: Int,
    val totalCents: Long,
)

@Serializable
data class ResumenCicloDto(
    val inicio: String,
    val fin: String,
    val cantidad: Int,
    val totalCents: Long,
)

@Serializable
data class RegistroRequest(
    val email: String,
    val password: String,
    val nombre: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
)

@Serializable
data class CrearHogarRequest(
    val nombre: String,
)

@Serializable
data class UnirseRequest(
    val inviteCode: String,
)

@Serializable
data class ConfigurarPasswordRequest(
    val password: String,
)

@Serializable
data class ConfigurarCorteRequest(
    val cutoffDay: Int,
)

@Serializable
data class CambiarAccesoRequest(
    val acceso: String,
)

@Serializable
data class CambiarRolRequest(
    val rol: String,
)

@Serializable
data class ResetPasswordAdminResponse(
    val passwordTemporal: String,
)

@Serializable
data class SeguridadRequest(
    val pregunta: String,
    val respuesta: String,
)

@Serializable
data class EstadoSeguridadDto(
    val pregunta: String? = null,
    val tieneCodigo: Boolean = false,
)

@Serializable
data class PreguntaRequest(
    val email: String,
)

@Serializable
data class PreguntaResponse(
    val pregunta: String? = null,
)

@Serializable
data class ResetPreguntaRequest(
    val email: String,
    val respuesta: String,
    val passwordNueva: String,
)

@Serializable
data class ResetCodigoRequest(
    val email: String,
    val codigo: String,
    val passwordNueva: String,
)

@Serializable
data class CambiarPasswordRequest(
    val passwordActual: String,
    val passwordNueva: String,
)

@Serializable
data class CodigoRecuperacionResponse(
    val codigo: String,
)

@Serializable
data class GastoRequest(
    val producto: String,
    val montoCents: Long,
    val fecha: String,
)

@Serializable
data class ErrorDto(
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class ResumenAnalisisDto(
    val totalCents: Long,
    val cantidad: Int,
    val diasConRegistro: Int,
    val fijosCents: Long,
    val variablesCents: Long,
)

@Serializable
data class EjemploAnalisisDto(
    val nombre: String,
    val termino: String,
    val cantidad: Int = 0,
    val totalCents: Long = 0,
)

@Serializable
data class CategoriaAnalisisDto(
    val categoria: String,
    val nombre: String,
    val totalCents: Long,
    val cantidad: Int,
    val ejemplos: List<EjemploAnalisisDto> = emptyList(),
)

@Serializable
data class GustoAnalisisDto(
    val conceptoId: String,
    val nombre: String,
    val cantidad: Int,
    val totalCents: Long,
    val ejemplos: List<EjemploAnalisisDto> = emptyList(),
)

@Serializable
data class SinClasificarAnalisisDto(
    val termino: String,
    val ejemplos: List<EjemploAnalisisDto> = emptyList(),
    val cantidad: Int,
    val totalCents: Long,
)

@Serializable
data class ConceptoDto(
    val id: String,
    val nombre: String,
    val categoria: String,
    val categoriaNombre: String,
    val esGusto: Boolean,
)

@Serializable
data class ClasificacionRequest(
    val termino: String,
    val conceptoId: String,
    val nota: String? = null,
)

@Serializable
data class CrearCategoriaRequest(
    val nombre: String,
)

@Serializable
data class IngresoDto(
    val id: String,
    val hogarId: String,
    val nombre: String? = null,
    val montoCents: Long,
    val periodicidad: String,
    val createdAt: String = "",
    val updatedAt: String = "",
)

@Serializable
data class IngresoRequest(
    val nombre: String? = null,
    val montoCents: Long,
    val periodicidad: String,
)

@Serializable
data class AnalisisIADto(
    val ciclos: List<String> = emptyList(),
    val progreso: ProgresoCicloIADto? = null,
    val personasEstimadas: Int = 0,
    val nivelIngreso: String = "MEDIO",
    val salariosMinimos: Double = 0.0,
    val margenLibreCents: Long = 0,
    val estado: String = "",
    val sugerencias: List<SugerenciaIADto> = emptyList(),
    val recomendacion: String = "",
    val cacheado: Boolean = false,
)

@Serializable
data class ProgresoCicloIADto(
    val diasTranscurridos: Int = 0,
    val diasTotales: Int = 0,
    val diasRestantes: Int = 0,
    val totalCents: Long = 0,
    val ingresoMensualCents: Long = 0,
    val porcentajeIngreso: Int = 0,
)

@Serializable
data class SugerenciaIADto(
    val categoria: String = "",
    val montoCents: Long = 0,
    val periodicidad: String = "SEMANAL",
    val nota: String = "",
)

@Serializable
data class AiInsightsRequest(
    val ciclos: List<String>? = null,
    /** Personas en casa ajustadas en este teléfono. No se guarda en la base. */
    val personas: Int? = null,
)

@Serializable
data class CarreraDiaDto(
    val fecha: String,
    val acumuladoCents: Long,
)

@Serializable
data class CarreraCicloDto(
    val etiqueta: String,
    val inicio: String,
    val fin: String,
    val dias: List<CarreraDiaDto>,
)

@Serializable
data class ProyeccionAnalisisDto(
    val diasTranscurridos: Int,
    val diasTotales: Int,
    val ritmoDiarioCents: Long,
    val ritmoAnteriorCents: Long,
    val variableCents: Long,
    val fijoCents: Long,
)

@Serializable
data class AnalisisDto(
    val ciclos: List<String>,
    val resumen: ResumenAnalisisDto,
    val categorias: List<CategoriaAnalisisDto>,
    val gustos: List<GustoAnalisisDto>,
    val sinClasificar: List<SinClasificarAnalisisDto> = emptyList(),
    val carrera: List<CarreraCicloDto>,
    val proyeccion: ProyeccionAnalisisDto,
)
