package com.dls.pymetask.data.remote


import com.dls.pymetask.domain.model.Tarea
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Clase DTO para mapear tareas desde/hacia Firestore.
 */
data class TareaDto(
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val completado: Boolean = false,
    val userId: String = ""
) {
    fun toDomain(id: String) = Tarea(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        hora = hora,
        completado = completado,
        userId = userId
    )

    companion object {
        fun fromDomain(tarea: Tarea) = TareaDto(
            titulo = tarea.titulo,
            descripcion = tarea.descripcion,
            fecha = tarea.fecha,
            hora = tarea.hora,
            completado = tarea.completado,
            userId = tarea.userId
        )

        fun fromSnapshot(snapshot: DocumentSnapshot): Tarea {
            val data = snapshot.toObject(TareaDto::class.java)!!
            return data.toDomain(snapshot.id)
        }
    }
}
