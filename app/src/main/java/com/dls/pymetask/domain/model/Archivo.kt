package com.dls.pymetask.domain.model

data class Archivo(
    val id: String = "",
    val nombre: String = "",
    val url: String = "",
    val tipo: String = "", // "pdf", "jpg", "png"
    val fecha: Long = System.currentTimeMillis(),
    val carpetaId: String? = null
)
