// Paquete sugerido: com.dls.pymetask.data.remote
package com.dls.pymetask.data.remote

import com.google.firebase.firestore.PropertyName

/**
 * DTO de persistencia para Firestore.
 * - Campos 'var' + constructor sin args -> necesario para des/serialización.
 * - Mantiene los mismos nombres de campo que el documento en Firestore.
 * - NO lo uses en UI ni en dominio.
 */
data class MovimientoDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("titulo") @set:PropertyName("titulo")
    var titulo: String = "",

    @get:PropertyName("subtitulo") @set:PropertyName("subtitulo")
    var subtitulo: String = "",

    @get:PropertyName("cantidad") @set:PropertyName("cantidad")
    var cantidad: Double = 0.0,

    @get:PropertyName("ingreso") @set:PropertyName("ingreso")
    var ingreso: Boolean = true,

    @get:PropertyName("fecha") @set:PropertyName("fecha")
    var fecha: Long = 0L,

    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = ""
)
