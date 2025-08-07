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
    suspend operator fun invoke() = repo.getTareas()
}
class GetTarea(private val repo: TareaRepository) {
    suspend operator fun invoke(id: String) = repo.getTareaById(id)
}
class AddTarea(private val repo: TareaRepository) {
    suspend operator fun invoke(tarea: Tarea) = repo.addTarea(tarea)
}
class UpdateTarea(private val repo: TareaRepository) {
    suspend operator fun invoke(tarea: Tarea) = repo.updateTarea(tarea)
}
class DeleteTarea(private val repo: TareaRepository) {
    suspend operator fun invoke(id: String) = repo.deleteTarea(id)
}
class EliminarTarea(private val repo: TareaRepository) {
    suspend operator fun invoke(tarea: Tarea) = repo.eliminarTarea(tarea)
}
