package com.andromeda.gastos.domain.entity

data class ResumenAnalisis(
    val totalCents: Long,
    val cantidad: Int,
    val diasConRegistro: Int,
    val fijosCents: Long,
    val variablesCents: Long,
)

data class EjemploAnalisis(
    val nombre: String,
    val termino: String,
)

data class CategoriaAnalisis(
    val categoria: String,
    val nombre: String,
    val totalCents: Long,
    val cantidad: Int,
    val ejemplos: List<EjemploAnalisis> = emptyList(),
)

data class GustoAnalisis(
    val conceptoId: String,
    val nombre: String,
    val cantidad: Int,
    val totalCents: Long,
    val ejemplos: List<EjemploAnalisis> = emptyList(),
)

data class SinClasificarAnalisis(
    val termino: String,
    val ejemplos: List<EjemploAnalisis>,
    val cantidad: Int,
    val totalCents: Long,
)

data class CarreraDia(
    val fecha: String,
    val acumuladoCents: Long,
)

data class CarreraCiclo(
    val etiqueta: String,
    val inicio: String,
    val fin: String,
    val dias: List<CarreraDia>,
)

data class ProyeccionAnalisis(
    val diasTranscurridos: Int,
    val diasTotales: Int,
    val ritmoDiarioCents: Long,
    val ritmoAnteriorCents: Long,
    val variableCents: Long,
    val fijoCents: Long,
)

data class Analisis(
    val ciclos: List<String>,
    val resumen: ResumenAnalisis,
    val categorias: List<CategoriaAnalisis>,
    val gustos: List<GustoAnalisis>,
    val sinClasificar: List<SinClasificarAnalisis>,
    val carrera: List<CarreraCiclo>,
    val proyeccion: ProyeccionAnalisis,
)
