package com.dls.pymetask.data.repository


import com.dls.pymetask.data.remote.TareaDto
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.repository.TareaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TareaRepositoryImpl(
    private val firestore: FirebaseFirestore
) : TareaRepository {

    private val tareasRef = firestore.collection("tareas")

    override suspend fun guardarTarea(tarea: Tarea) {
        if (tarea.id.isNotEmpty()) {
            tareasRef.document(tarea.id).set(TareaDto.fromDomain(tarea)).await()
        } else {
            tareasRef.add(TareaDto.fromDomain(tarea)).await()
        }
    }

    override suspend fun eliminarTarea(tareaId: String) {
        tareasRef.document(tareaId).delete().await()
    }

    override fun obtenerTareas(userId: String): Flow<List<Tarea>> = callbackFlow {
        val listener = tareasRef
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val tareas = snapshot?.documents?.map { doc ->
                    TareaDto.fromSnapshot(doc)
                } ?: emptyList()
                trySend(tareas)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun obtenerTareaPorId(tareaId: String): Tarea? {
        val doc = tareasRef.document(tareaId).get().await()
        return if (doc.exists()) TareaDto.fromSnapshot(doc) else null
    }
}
