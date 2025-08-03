package com.dls.pymetask.domain.repository

import com.dls.pymetask.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

interface TareaRepository {
    suspend fun getTareas(): List<Tarea>
    suspend fun getTareaById(id: String): Tarea?
    suspend fun addTarea(tarea: Tarea)
    suspend fun updateTarea(tarea: Tarea)
    suspend fun deleteTarea(id: String)
    suspend fun eliminarTarea(tarea: Tarea)

}
