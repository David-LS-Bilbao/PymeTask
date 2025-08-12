@file:Suppress("DEPRECATION")

package com.dls.pymetask.data.mappers

import com.dls.pymetask.data.remote.MovimientoDto
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.presentation.movimientos.MovimientoUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Convierte un DTO de Firestore a modelo de dominio (puro).
 * Úsalo en el RepositoryImpl cuando leas de Firestore.
 */
fun MovimientoDto.toDomain(): Movimiento = Movimiento(
    id        = id,
    titulo    = titulo,
    subtitulo = subtitulo,
    cantidad  = cantidad,
    ingreso   = ingreso,
    fecha     = if (fecha == 0L) System.currentTimeMillis() else fecha,
    userId    = userId
)

/**
 * Convierte el modelo de dominio a DTO.
 * Úsalo en el RepositoryImpl antes de guardar en Firestore.
 */
fun Movimiento.toDto(): MovimientoDto = MovimientoDto(
    id        = id,
    titulo    = titulo,
    subtitulo = subtitulo,
    cantidad  = cantidad,
    ingreso   = ingreso,
    fecha     = fecha,
    userId    = userId
)

/**
 * Extensión que convierte el modelo de dominio a el modelo de UI usado en la lista.
 * - fecha (Long epoch millis) -> "dd/MM/yyyy"
 * - importe: positivo si ingreso, negativo si gasto
 */
fun Movimiento.toUi(): MovimientoUi {
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    val fechaTexto = formato.format(Date(this.fecha))
    val importeUi = if (this.ingreso) this.cantidad else -this.cantidad
    return MovimientoUi(
        id = this.id,
        titulo = this.titulo,
        fecha = fechaTexto,
        importe = importeUi
    )
}


