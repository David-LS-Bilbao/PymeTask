package com.dls.pymetask.domain.model

import com.google.firebase.Timestamp


data class Movimiento(
    val id: String = "",
    val titulo: String = "",
    val subtitulo: String = "",
    val cantidad: Double = 0.0,
    val ingreso: Boolean = true,
    //val fecha: Long = Timestamp.now(),
    val fecha: Long = System.currentTimeMillis(), // Agrega esta línea
    val userId: String = ""
)
