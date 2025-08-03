package com.dls.pymetask.data.remote


import com.dls.pymetask.domain.model.Tarea



/**
 * Clase DTO para mapear tareas desde/hacia Firestore.
 */
data class TareaDto(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val completado: Boolean = false
    //val userId: String? = null
) {
    fun toDomain() = Tarea(id, titulo, descripcion, fecha, hora, completado)// userId)


    companion object {
        fun fromDomain(tarea: Tarea) = TareaDto(
            id = tarea.id,
            titulo = tarea.titulo,
            descripcion = tarea.descripcion,
            fecha = tarea.fecha,
            hora = tarea.hora,
            completado = tarea.completado,
            //userId = tarea.userId
        )
    }
}
