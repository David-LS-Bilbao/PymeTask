package com.dls.pymetask.data.repository

import android.content.Context
import android.util.Log
import com.dls.pymetask.data.remote.NotaDto
import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.domain.repository.NotaRepository
import com.dls.pymetask.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotaRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val context: Context
) : NotaRepository {

    private fun userCollection() = Constants.getUserIdSeguro(context)?.let { userId ->
        firestore.collection("usuarios").document(userId).collection("notas")
    } ?: run {
        Log.e("NotaRepo", "❌ userId no disponible, abortando operación")
        null
    }

    override suspend fun getNotas(): List<Nota> {
        return userCollection()
            ?.get()
            ?.await()
            ?.documents
            ?.mapNotNull { it.toObject(NotaDto::class.java)?.toDomain() }
            ?.sortedBy { it.posicion } ?: emptyList()
    }

    override suspend fun getNotaById(id: String): Nota? {
        return userCollection()
            ?.document(id)
            ?.get()
            ?.await()
            ?.toObject(NotaDto::class.java)?.toDomain()
    }

    override suspend fun addNota(nota: Nota) {
        userCollection()
            ?.document(nota.id)
            ?.set(NotaDto.fromDomain(nota))
            ?.await()
    }

    override suspend fun updateNota(nota: Nota) {
        userCollection()
            ?.document(nota.id)
            ?.set(NotaDto.fromDomain(nota))
            ?.await()
    }

    override suspend fun deleteNota(id: String) {
        userCollection()
            ?.document(id)
            ?.delete()
            ?.await()
    }

    override suspend fun eliminarNota(nota: Nota) {
        userCollection()
            ?.document(nota.id)
            ?.delete()
            ?.await()
    }
}












//package com.dls.pymetask.data.repository
//
//import com.dls.pymetask.data.remote.NotaDto
//import com.dls.pymetask.domain.model.Nota
//import com.dls.pymetask.domain.repository.NotaRepository
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//
//
//class NotaRepositoryImpl(firestore: FirebaseFirestore) : NotaRepository {
//    private val collection = firestore.collection("notas")
//
//
//    override suspend fun getNotas(): List<Nota> {
//        return collection
//            .get()
//            .await()
//            .documents
//            .mapNotNull { it.toObject(NotaDto::class.java)?.toDomain() }
//            .sortedBy { it.posicion }
//    }
//
//    override suspend fun getNotaById(id: String): Nota? {
//        return collection.document(id).get().await().toObject(NotaDto::class.java)?.toDomain()
//    }
//
//    override suspend fun addNota(nota: Nota) {
//        collection.document(nota.id).set(NotaDto.fromDomain(nota)).await()
//    }
//
//    override suspend fun updateNota(nota: Nota) {
//        collection.document(nota.id).set(NotaDto.fromDomain(nota)).await()
//    }
//
//    override suspend fun deleteNota(id: String) {
//        collection.document(id).delete().await()
//    }
//
//    override suspend fun eliminarNota(nota: Nota) {
//        collection.document(nota.id).delete().await()
//
//    }
//}