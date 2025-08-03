package com.dls.pymetask.data.repository


import com.dls.pymetask.data.remote.TareaDto
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.repository.TareaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TareaRepositoryImpl(firestore: FirebaseFirestore) : TareaRepository {

    private val collection = firestore.collection("tareas")

    override suspend fun getTareas(): List<Tarea> {
        return collection
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(TareaDto::class.java)?.toDomain() }
            .sortedBy { it.fecha }
    }

    override suspend fun getTareaById(id: String): Tarea? {
        return collection.document(id).get().await().toObject(TareaDto::class.java)?.toDomain()
    }

    override suspend fun addTarea(tarea: Tarea) {
        val id = tarea.id.ifBlank {
            collection.document().id // genera nuevo ID si está en blanco
        }
        val tareaConId = tarea.copy(id = id)
        collection.document(id).set(TareaDto.fromDomain(tareaConId)).await()
    }


    override suspend fun updateTarea(tarea: Tarea) {
        collection.document(tarea.id).set(TareaDto.fromDomain(tarea)).await()
    }

    override suspend fun deleteTarea(id: String) {
        collection.document(id).delete().await()
    }

    override suspend fun eliminarTarea(tarea: Tarea) {
        collection.document(tarea.id).delete().await()
    }

//
//    override suspend fun guardarTarea(tarea: Tarea) {
//        if (tarea.id.isNotEmpty()) {
//            tareasRef.document(tarea.id).set(TareaDto.fromDomain(tarea)).await()
//        } else {
//            tareasRef.add(TareaDto.fromDomain(tarea)).await()
//        }
//    }
//
//    override suspend fun eliminarTarea(tareaId: String) {
//        tareasRef.document(tareaId).delete().await()
//    }
//
//    override fun obtenerTareas(): Flow<List<Tarea>> = callbackFlow {
//        val listener = tareasRef
//            .addSnapshotListener { snapshot, _ ->
//                val tareas = snapshot?.documents?.map { doc ->
//                    TareaDto.fromSnapshot(doc)
//                } ?: emptyList()
//                trySend(tareas)
//            }
//        awaitClose { listener.remove() }
//    }
//
//    override suspend fun obtenerTareaPorId(tareaId: String): Tarea? {
//        val doc = tareasRef.document(tareaId).get().await()
//        return if (doc.exists()) TareaDto.fromSnapshot(doc) else null
//    }
}
