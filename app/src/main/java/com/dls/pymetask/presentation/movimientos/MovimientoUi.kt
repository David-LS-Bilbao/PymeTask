package com.dls.pymetask.presentation.movimientos

data class MovimientoUi(
    val id: String,
    val titulo: String,
    val fechaTexto: String,   // fecha formateada para UI
    val importe: Double,      // positivo si ingreso, negativo si gasto
    val fechaMillis: Long     // para filtros por mes
)
