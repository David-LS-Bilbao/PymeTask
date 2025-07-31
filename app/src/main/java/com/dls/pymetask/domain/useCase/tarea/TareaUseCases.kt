package com.dls.pymetask.domain.usecase.tarea

import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.repository.TareaRepository

data class TareaUseCases(
    val guardarTarea: GuardarTarea,
    val eliminarTarea: EliminarTarea,
    val obtenerTareas: ObtenerTareas,
    val obtenerTareaPorId: ObtenerTareaPorId
)
class GuardarTarea(private val repository: TareaRepository) {
    suspend operator fun invoke(tarea: Tarea) = repository.guardarTarea(tarea)
}

class EliminarTarea(private val repository: com.dls.pymetask.domain.repository.TareaRepository) {
    suspend operator fun invoke(tareaId: String) = repository.eliminarTarea(tareaId)
}
class ObtenerTareas(private val repository: TareaRepository) {
    operator fun invoke(userId: String) = repository.obtenerTareas(userId)
}

class ObtenerTareaPorId(private val repository: TareaRepository) {
    suspend operator fun invoke(tareaId: String) = repository.obtenerTareaPorId(tareaId)
}