package com.andromeda.gastos.exception

class AppException(
    message: String,
    /** Código HTTP cuando el error vino del backend. */
    val codigo: Int? = null,
) : Exception(message)
