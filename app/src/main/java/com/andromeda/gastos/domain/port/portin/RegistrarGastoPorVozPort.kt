package com.andromeda.gastos.domain.port.portin

import com.andromeda.gastos.domain.entity.Gasto

interface RegistrarGastoPorVozPort {
    suspend fun ejecutar(hogarId: String, onMensaje: (String) -> Unit): Gasto?
}
