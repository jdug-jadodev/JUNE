package com.andromeda.gastos.domain.rule

object GastoReglas {
    private val formatoFecha = Regex("""\d{4}-\d{2}-\d{2}""")

    fun validar(producto: String, montoCents: Long, fecha: String) {
        require(producto.isNotBlank()) { "El producto es obligatorio" }
        require(montoCents > 0) { "El monto debe ser mayor que cero" }
        require(formatoFecha.matches(fecha)) { "La fecha debe tener formato YYYY-MM-DD" }
    }
}
