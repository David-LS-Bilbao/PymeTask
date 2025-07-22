package com.dls.pymetask.domain.model

data class Contacto(
    val id: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val tipo: String = "",
    val direccion: String = "",
    val fotoUrl: String? = null,
    val email: String = ""
)
