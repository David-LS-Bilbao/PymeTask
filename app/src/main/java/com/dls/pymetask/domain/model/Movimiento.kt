package com.dls.pymetask.domain.model

import java.util.Date

data class Movimiento(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val cantidad: Double,
    val ingreso: Boolean,
    val fecha: Date
)
