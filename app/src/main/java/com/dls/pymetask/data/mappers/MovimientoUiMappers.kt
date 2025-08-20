package com.dls.pymetask.data.mappers

import com.dls.pymetask.domain.model.Movimiento

/**
 * Mapper DTO (Firestore) -> Dominio.
 * - Si 'fecha' viene a 0L, usamos 'now' como fallback para evitar nulos en dominio.
 * - NO se formatea fecha aquí (eso es de la vista).
 */
//fun MovimientoDto.toDomain(): Movimiento = Movimiento(
//    id        = id,
//    titulo    = titulo,
//    subtitulo = subtitulo,
//    cantidad  = cantidad,
//    ingreso   = ingreso,
//    fecha     = if (fecha == 0L) System.currentTimeMillis() else fecha,
//    userId    = userId
//)

/**
 * Mapper Dominio -> DTO (para guardar en Firestore).
 * - No aplicamos lógica de UI ni formateos aquí.
 */
//fun Movimiento.toDto(): MovimientoDto = MovimientoDto(
//    id        = id,
//    titulo    = titulo,
//    subtitulo = subtitulo,
//    cantidad  = cantidad,
//    ingreso   = ingreso,
//    fecha     = fecha,
//    userId    = userId
//)

