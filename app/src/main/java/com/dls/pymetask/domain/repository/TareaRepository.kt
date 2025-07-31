package com.dls.pymetask.domain.repository

import com.dls.pymetask.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

interface TareaRepository {
    suspend fun guardarTarea(tarea: Tarea)
    suspend fun eliminarTarea(tareaId: String)
    fun obtenerTareas(userId: String): Flow<List<Tarea>>
    suspend fun obtenerTareaPorId(tareaId: String): Tarea?
}
