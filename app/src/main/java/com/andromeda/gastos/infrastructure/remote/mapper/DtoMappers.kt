package com.andromeda.gastos.infrastructure.remote.mapper

import com.andromeda.gastos.domain.entity.Gasto
import com.andromeda.gastos.domain.entity.Hogar
import com.andromeda.gastos.domain.entity.Miembro
import com.andromeda.gastos.domain.entity.ResumenCiclo
import com.andromeda.gastos.domain.entity.ResumenMes
import com.andromeda.gastos.domain.entity.Sesion
import com.andromeda.gastos.domain.entity.Tokens
import com.andromeda.gastos.domain.entity.Usuario
import com.andromeda.gastos.domain.entity.Analisis
import com.andromeda.gastos.domain.entity.CarreraCiclo
import com.andromeda.gastos.domain.entity.CarreraDia
import com.andromeda.gastos.domain.entity.CategoriaAnalisis
import com.andromeda.gastos.domain.entity.Concepto
import com.andromeda.gastos.domain.entity.EjemploAnalisis
import com.andromeda.gastos.domain.entity.GustoAnalisis
import com.andromeda.gastos.domain.entity.ProyeccionAnalisis
import com.andromeda.gastos.domain.entity.ResumenAnalisis
import com.andromeda.gastos.domain.entity.SinClasificarAnalisis
import com.andromeda.gastos.domain.enums.Acceso
import com.andromeda.gastos.domain.enums.Rol
import com.andromeda.gastos.infrastructure.remote.dto.AnalisisDto
import com.andromeda.gastos.infrastructure.remote.dto.AuthResponseDto
import com.andromeda.gastos.infrastructure.remote.dto.ConceptoDto
import com.andromeda.gastos.infrastructure.remote.dto.EjemploAnalisisDto
import com.andromeda.gastos.infrastructure.remote.dto.GastoDto
import com.andromeda.gastos.infrastructure.remote.dto.HogarDto
import com.andromeda.gastos.infrastructure.remote.dto.MiembroDto
import com.andromeda.gastos.infrastructure.remote.dto.ResumenCicloDto
import com.andromeda.gastos.infrastructure.remote.dto.ResumenMesDto

fun AuthResponseDto.aDominio(): Sesion = Sesion(
    usuario = Usuario(id = usuario.id, email = usuario.email, nombre = usuario.nombre),
    tokens = Tokens(accessToken = accessToken, refreshToken = refreshToken),
)

fun HogarDto.aDominio(): Hogar = Hogar(
    id = id,
    nombre = nombre,
    ownerId = ownerId,
    inviteCode = inviteCode,
    cutoffDay = cutoffDay,
    createdAt = createdAt,
)

fun MiembroDto.aDominio(): Miembro = Miembro(
    id = id,
    hogarId = hogarId,
    usuarioId = usuarioId,
    nombre = nombre.orEmpty(),
    email = email.orEmpty(),
    rol = runCatching { Rol.valueOf(rol) }.getOrDefault(Rol.MIEMBRO),
    acceso = runCatching { Acceso.valueOf(acceso) }.getOrDefault(Acceso.ESCRITURA),
    joinedAt = joinedAt,
)

fun GastoDto.aDominio(): Gasto = Gasto(
    id = id,
    hogarId = hogarId,
    producto = producto,
    montoCents = montoCents,
    fecha = fecha,
    creadoPor = creadoPor,
    creadoPorNombre = creadoPorNombre,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ResumenMesDto.aDominio(): ResumenMes = ResumenMes(
    mes = mes,
    cantidad = cantidad,
    totalCents = totalCents,
)

fun ResumenCicloDto.aDominio(): ResumenCiclo = ResumenCiclo(
    inicio = inicio,
    fin = fin,
    cantidad = cantidad,
    totalCents = totalCents,
)

fun ConceptoDto.aDominio(): Concepto = Concepto(
    id = id,
    nombre = nombre,
    categoria = categoria,
    categoriaNombre = categoriaNombre,
    esGusto = esGusto,
)

fun EjemploAnalisisDto.aDominio(): EjemploAnalisis = EjemploAnalisis(
    nombre = nombre,
    termino = termino,
)

fun AnalisisDto.aDominio(): Analisis = Analisis(
    ciclos = ciclos,
    resumen = ResumenAnalisis(
        totalCents = resumen.totalCents,
        cantidad = resumen.cantidad,
        diasConRegistro = resumen.diasConRegistro,
        fijosCents = resumen.fijosCents,
        variablesCents = resumen.variablesCents,
    ),
    categorias = categorias.map {
        CategoriaAnalisis(
            categoria = it.categoria,
            nombre = it.nombre,
            totalCents = it.totalCents,
            cantidad = it.cantidad,
            ejemplos = it.ejemplos.map { ejemplo -> ejemplo.aDominio() },
        )
    },
    gustos = gustos.map {
        GustoAnalisis(
            conceptoId = it.conceptoId,
            nombre = it.nombre,
            cantidad = it.cantidad,
            totalCents = it.totalCents,
            ejemplos = it.ejemplos.map { ejemplo -> ejemplo.aDominio() },
        )
    },
    sinClasificar = sinClasificar.map {
        SinClasificarAnalisis(
            termino = it.termino,
            ejemplos = it.ejemplos.map { ejemplo -> ejemplo.aDominio() },
            cantidad = it.cantidad,
            totalCents = it.totalCents,
        )
    },
    carrera = carrera.map {
        CarreraCiclo(
            etiqueta = it.etiqueta,
            inicio = it.inicio,
            fin = it.fin,
            dias = it.dias.map { d ->
                CarreraDia(fecha = d.fecha, acumuladoCents = d.acumuladoCents)
            },
        )
    },
    proyeccion = ProyeccionAnalisis(
        diasTranscurridos = proyeccion.diasTranscurridos,
        diasTotales = proyeccion.diasTotales,
        ritmoDiarioCents = proyeccion.ritmoDiarioCents,
        ritmoAnteriorCents = proyeccion.ritmoAnteriorCents,
        variableCents = proyeccion.variableCents,
        fijoCents = proyeccion.fijoCents,
    ),
)
