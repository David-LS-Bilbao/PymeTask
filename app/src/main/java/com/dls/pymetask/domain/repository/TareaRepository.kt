package com.dls.pymetask.domain.repository

import com.dls.pymetask.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

interface TareaRepository {
    suspend fun getTareas(userId: String): List<Tarea>
    suspend fun getTareaById(id: String, userId: String): Tarea?
    suspend fun addTarea(tarea: Tarea, userId: String)
    suspend fun updateTarea(tarea: Tarea, userId: String)
    suspend fun deleteTarea(id: String, userId: String)
    suspend fun eliminarTarea(tarea: Tarea, userId: String)

}
