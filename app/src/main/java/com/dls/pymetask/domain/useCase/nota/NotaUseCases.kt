package com.dls.pymetask.domain.useCase.nota

import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.domain.repository.NotaRepository

// domain.usecase.nota.UseCasesNota.kt
data class NotaUseCases(
    val getNotas: GetNotas,
    val getNota: GetNota,
    val addNota: AddNota,
    val updateNota: UpdateNota,
    val deleteNota: DeleteNota,
    val eliminarNota: EliminarNota
)

class GetNotas(private val repo: NotaRepository) {
    suspend operator fun invoke() = repo.getNotas()
}
class GetNota(private val repo: NotaRepository) {
    suspend operator fun invoke(id: String) = repo.getNotaById(id)
}
class AddNota(private val repo: NotaRepository) {
    suspend operator fun invoke(nota: Nota) = repo.addNota(nota)
}
class UpdateNota(private val repo: NotaRepository) {
    suspend operator fun invoke(nota: Nota) = repo.updateNota(nota)
}
class DeleteNota(private val repo: NotaRepository) {
    suspend operator fun invoke(id: String) = repo.deleteNota(id)
}
class EliminarNota(private val repo: NotaRepository) {
    suspend operator fun invoke(nota: Nota) = repo.eliminarNota(nota)
}

