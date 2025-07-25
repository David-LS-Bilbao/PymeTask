package com.dls.pymetask.domain.repository

import com.dls.pymetask.domain.model.Nota


interface NotaRepository {
    suspend fun getNotas(): List<Nota>
    suspend fun getNotaById(id: String): Nota?
    suspend fun addNota(nota: Nota)
    suspend fun updateNota(nota: Nota)
    suspend fun deleteNota(id: String)
    suspend fun eliminarNota(nota: Nota)


}
