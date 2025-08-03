package com.dls.pymetask.domain.model

/**
 * Entidad de dominio que representa una tarea de la agenda.
 */
data class Tarea(
    val id: String = "",               // ID único de Firestore
    val titulo: String = "",           // Título de la tarea (obligatorio)
    val descripcion: String = "",      // Descripción opcional
    val descripcionLarga: String = "", // Descripción larga opcional
    val fecha: String = "",            // Fecha en formato "yyyy-MM-dd"
    val hora: String = "",             // Hora opcional, formato "HH:mm"
    val completado: Boolean = false,   // Estado de la tarea
    val activarAlarma: String? = null, // ID del usuario propietario (FirebaseAuth.uid), // Estado de la tarea
    val userId: String = ""            // Estado de la tarea
)

