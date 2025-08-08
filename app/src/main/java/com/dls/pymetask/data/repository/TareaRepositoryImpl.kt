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
    private val firestore: FirebaseFirestore
) : TareaRepository {

    private fun userCollection(userId: String) =
        firestore
            .collection("usuarios")
            .document(userId)
            .collection("tareas")

    override suspend fun getTareas(userId: String): List<Tarea> {
        return userCollection(userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(TareaDto::class.java)?.toDomain() }
            .sortedBy { it.fecha }
    }

    override suspend fun getTareaById(id: String, userId: String): Tarea? =
        userCollection(userId)
            .document(id)
            .get()
            .await()
            .toObject(TareaDto::class.java)
            ?.toDomain()

    override suspend fun addTarea(tarea: Tarea, userId: String) {
        val col = userCollection(userId)
        val id  = tarea.id.ifBlank { col.document().id }
        col.document(id)
            .set(TareaDto.fromDomain(tarea.copy(id = id, userId = userId)))
            .await()
    }

    override suspend fun updateTarea(tarea: Tarea, userId: String) {
        userCollection(userId)
            .document(tarea.id)
            .set(TareaDto.fromDomain(tarea))
            .await()
    }

    override suspend fun deleteTarea(id: String, userId: String) {
        userCollection(userId)
            .document(id)
            .delete()
            .await()
    }

        override suspend fun eliminarTarea(tarea: Tarea, userId: String) {
            userCollection(userId)
                .document(tarea.id)
                .delete()
                .await()
    }
}





//class TareaRepositoryImpl(
//    private val firestore: FirebaseFirestore,
//    private val context: Context
//) : TareaRepository {
//    private fun userCollection(userId: String) = Constants.getUserIdSeguro(context)?.let { userId ->
//        firestore.collection("usuarios").document(userId).collection("tareas")
//    } ?: run {
//        Log.e("TareaRepo", "❌ userId no disponible, abortando operación")
//        null
//    }
//    override suspend fun getTareas(userId: String): List<Tarea> {
//        return userCollection(userId)
//            ?.get()
//            ?.await()
//            ?.documents
//            ?.mapNotNull { it.toObject(TareaDto::class.java)?.toDomain() }
//            ?.sortedBy { it.fecha } ?: emptyList()
//    }
//    override suspend fun getTareaById(id: String, userId: String): Tarea? {
//        return userCollection(userId)
//            ?.document(id)
//            ?.get()
//            ?.await()
//            ?.toObject(TareaDto::class.java)?.toDomain()
//    }
//    override suspend fun addTarea(tarea: Tarea, userId: String) {
//        val id = tarea.id.ifBlank {
//            userCollection(userId)?.document()?.id ?: return
//        }
//        val tareaConId = tarea.copy(id = id)
//        userCollection(userId)
//            ?.document(id)
//            ?.set(TareaDto.fromDomain(tareaConId))
//            ?.await()
//    }
//    override suspend fun updateTarea(tarea: Tarea, userId: String) {
//        userCollection(userId)
//            ?.document(tarea.id)
//            ?.set(TareaDto.fromDomain(tarea))
//            ?.await()
//    }
//    override suspend fun deleteTarea(id: String, userId: String) {
//        userCollection(userId)
//            ?.document(id)
//            ?.delete()
//            ?.await()
//    }
//    override suspend fun eliminarTarea(tarea: Tarea, userId: String) {
//        userCollection(userId)
//            ?.document(tarea.id)
//            ?.delete()
//            ?.await()
//    }
//}
