package com.dls.pymetask.data.remote


import com.dls.pymetask.domain.model.Tarea



/**
 * Clase DTO para mapear tareas desde/hacia Firestore.
 */
data class TareaDto(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val descripcionLarga: String = "",
    val fecha: String = "",
    val hora: String = "",
    val completado: Boolean = false,
    val activarAlarma: Boolean = true,
    val userId: String = ""


) {
    fun toDomain() = Tarea(id, titulo, descripcion, descripcionLarga, fecha, hora, completado)// userId)


    companion object {
        fun fromDomain(tarea: Tarea) = TareaDto(
            id = tarea.id,
            titulo = tarea.titulo,
            descripcion = tarea.descripcion,
            descripcionLarga = tarea.descripcionLarga,
            fecha = tarea.fecha,
            hora = tarea.hora,
            completado = tarea.completado,
            activarAlarma = tarea.activarAlarma,
            userId = tarea.userId
        )
    }
}
