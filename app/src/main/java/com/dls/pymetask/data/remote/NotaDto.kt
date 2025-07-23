package com.dls.pymetask.data.remote

import com.dls.pymetask.domain.model.Nota

data class NotaDto(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val contactoId: String? = null
) {
    fun toDomain() = Nota(id, titulo, contenido, fecha, contactoId)
    companion object {
        fun fromDomain(nota: Nota) = NotaDto(
            id = nota.id,
            titulo = nota.titulo,
            contenido = nota.contenido,
            fecha = nota.fecha,
            contactoId = nota.contactoId
        )
    }
}