package com.dls.pymetask.domain.model


data class Nota(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val contactoId: String? = null // opcional, para asociar nota con contacto
)