package com.dls.pymetask.domain.useCase.tarea

import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.repository.TareaRepository

data class TareaUseCases(
    val getTareas: GetTareas,
    val getTarea: GetTarea,
    val addTarea: AddTarea,
    val updateTarea: UpdateTarea,
    val deleteTarea: DeleteTarea,
    val eliminarTarea: EliminarTarea
)
class GetTareas(private val repo: TareaRepository) {
    suspend operator fun invoke(userId: String) = repo.getTareas(userId)
}
class GetTarea(private val repo: TareaRepository) {
    /** Ahora recibe userId para filtrar en el repositorio */

    suspend operator fun invoke(id: String, userId: String) = repo.getTareaById(id,userId)
}
class AddTarea(private val repo: TareaRepository) {
    /** Ahora recibe la tarea y el userId */
    suspend operator fun invoke(tarea: Tarea, userId: String) =
        repo.addTarea(tarea, userId)
}
class UpdateTarea(private val repo: TareaRepository) {
    suspend operator fun invoke(tarea: Tarea, userId: String) = repo.updateTarea(tarea, userId)
}
class DeleteTarea(private val repo: TareaRepository) {
    suspend operator fun invoke(id: String, userId: String) = repo.deleteTarea(id, userId)
}
class EliminarTarea(private val repo: TareaRepository) {
    suspend operator fun invoke(tarea: Tarea, userId: String) = repo.eliminarTarea(tarea, userId)
}
