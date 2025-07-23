package com.dls.pymetask.domain.model


data class Nota(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val colorHex: String = "#FFF9C4",
    val contactoId: String? = null, // opcional, para asociar nota con contacto
    val posicion:Int =0 //marca la posicion de la nota
)