package com.dls.pymetask.data.repository

import com.dls.pymetask.data.remote.NotaDto
import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.domain.repository.NotaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class NotaRepositoryImpl(firestore: FirebaseFirestore) : NotaRepository {
    private val collection = firestore.collection("notas")

//    override suspend fun inicializarPosicionesSiFaltan() {
//        val snapshot = collection.get().await()
//        val documentosSinPosicion = snapshot.documents.filter { it.getLong("posicion") == null }
//
//        documentosSinPosicion.forEachIndexed { index, doc ->
//            doc.reference.update("posicion", index)
//        }
//    }


    override suspend fun getNotas(): List<Nota> {
        return collection
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(NotaDto::class.java)?.toDomain() }
            .sortedBy { it.posicion }
    }

    override suspend fun getNotaById(id: String): Nota? {
        return collection.document(id).get().await().toObject(NotaDto::class.java)?.toDomain()
    }

    override suspend fun addNota(nota: Nota) {
        collection.document(nota.id).set(NotaDto.fromDomain(nota)).await()
    }

    override suspend fun updateNota(nota: Nota) {
        collection.document(nota.id).set(NotaDto.fromDomain(nota)).await()
    }

    override suspend fun deleteNota(id: String) {
        collection.document(id).delete().await()
    }
}