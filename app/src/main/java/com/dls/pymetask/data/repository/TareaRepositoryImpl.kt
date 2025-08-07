package com.dls.pymetask.data.repository

import android.content.Context
import android.util.Log
import com.dls.pymetask.data.remote.TareaDto
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.repository.TareaRepository
import com.dls.pymetask.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TareaRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val context: Context
) : TareaRepository {

    private fun userCollection() = Constants.getUserIdSeguro(context)?.let { userId ->
        firestore.collection("usuarios").document(userId).collection("tareas")
    } ?: run {
        Log.e("TareaRepo", "❌ userId no disponible, abortando operación")
        null
    }

    override suspend fun getTareas(): List<Tarea> {
        return userCollection()
            ?.get()
            ?.await()
            ?.documents
            ?.mapNotNull { it.toObject(TareaDto::class.java)?.toDomain() }
            ?.sortedBy { it.fecha } ?: emptyList()
    }

    override suspend fun getTareaById(id: String): Tarea? {
        return userCollection()
            ?.document(id)
            ?.get()
            ?.await()
            ?.toObject(TareaDto::class.java)?.toDomain()
    }

    override suspend fun addTarea(tarea: Tarea) {
        val id = tarea.id.ifBlank {
            userCollection()?.document()?.id ?: return
        }
        val tareaConId = tarea.copy(id = id)
        userCollection()
            ?.document(id)
            ?.set(TareaDto.fromDomain(tareaConId))
            ?.await()
    }

    override suspend fun updateTarea(tarea: Tarea) {
        userCollection()
            ?.document(tarea.id)
            ?.set(TareaDto.fromDomain(tarea))
            ?.await()
    }

    override suspend fun deleteTarea(id: String) {
        userCollection()
            ?.document(id)
            ?.delete()
            ?.await()
    }

    override suspend fun eliminarTarea(tarea: Tarea) {
        userCollection()
            ?.document(tarea.id)
            ?.delete()
            ?.await()
    }
}











//package com.dls.pymetask.data.repository
//
//
//import com.dls.pymetask.data.remote.TareaDto
//import com.dls.pymetask.domain.model.Tarea
//import com.dls.pymetask.domain.repository.TareaRepository
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//
//class TareaRepositoryImpl(firestore: FirebaseFirestore) : TareaRepository {
//
//    private val collection = firestore.collection("tareas")
//
//    override suspend fun getTareas(): List<Tarea> {
//        return collection
//            .get()
//            .await()
//            .documents
//            .mapNotNull { it.toObject(TareaDto::class.java)?.toDomain() }
//            .sortedBy { it.fecha }
//    }
//
//    override suspend fun getTareaById(id: String): Tarea? {
//        return collection.document(id).get().await().toObject(TareaDto::class.java)?.toDomain()
//    }
//
//    override suspend fun addTarea(tarea: Tarea) {
//        val id = tarea.id.ifBlank {
//            collection.document().id // genera nuevo ID si está en blanco
//        }
//        val tareaConId = tarea.copy(id = id)
//        collection.document(id).set(TareaDto.fromDomain(tareaConId)).await()
//    }
//
//
//    override suspend fun updateTarea(tarea: Tarea) {
//        collection.document(tarea.id).set(TareaDto.fromDomain(tarea)).await()
//    }
//
//    override suspend fun deleteTarea(id: String) {
//        collection.document(id).delete().await()
//    }
//
//    override suspend fun eliminarTarea(tarea: Tarea) {
//        collection.document(tarea.id).delete().await()
//    }
//
////
////    override suspend fun guardarTarea(tarea: Tarea) {
////        if (tarea.id.isNotEmpty()) {
////            tareasRef.document(tarea.id).set(TareaDto.fromDomain(tarea)).await()
////        } else {
////            tareasRef.add(TareaDto.fromDomain(tarea)).await()
////        }
////    }
////
////    override suspend fun eliminarTarea(tareaId: String) {
////        tareasRef.document(tareaId).delete().await()
////    }
////
////    override fun obtenerTareas(): Flow<List<Tarea>> = callbackFlow {
////        val listener = tareasRef
////            .addSnapshotListener { snapshot, _ ->
////                val tareas = snapshot?.documents?.map { doc ->
////                    TareaDto.fromSnapshot(doc)
////                } ?: emptyList()
////                trySend(tareas)
////            }
////        awaitClose { listener.remove() }
////    }
////
////    override suspend fun obtenerTareaPorId(tareaId: String): Tarea? {
////        val doc = tareasRef.document(tareaId).get().await()
////        return if (doc.exists()) TareaDto.fromSnapshot(doc) else null
////    }
//}
