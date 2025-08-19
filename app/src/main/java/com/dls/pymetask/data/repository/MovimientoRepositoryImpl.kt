
package com.dls.pymetask.data.repository

import android.content.Context
import android.util.Log
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.repository.MovimientoRepository
import com.dls.pymetask.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MovimientoRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val context: Context
) : MovimientoRepository {
    private fun userCollection() = Constants.getUserIdSeguro(context)?.let { userId ->
        firestore.collection("usuarios").document(userId).collection("movimientos")
    } ?: run {
        Log.e("MovimientoRepo", "❌ userId no disponible, abortando operación")
        null
    }
    override fun getMovimientos(): Flow<List<Movimiento>> = callbackFlow {
        val collection = userCollection()
            ?.orderBy("fecha", Query.Direction.DESCENDING)

        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = collection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { it.toObject<Movimiento>() }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }
    override suspend fun addMovimiento(movimiento: Movimiento) {
        userCollection()?.document(movimiento.id)?.set(movimiento)?.await()
    }
    override suspend fun updateMovimiento(movimiento: Movimiento) {
        userCollection()?.document(movimiento.id)?.set(movimiento)?.await()
    }
    override suspend fun getMovimientoById(id: String): Movimiento? {
        val doc = userCollection()?.document(id)?.get()?.await()
        return doc?.toObject()
    }
    override suspend fun insertMovimiento(mov: Movimiento) {
        userCollection()?.document(mov.id)?.set(mov)?.await()
    }
    override suspend fun deleteMovimiento(id: String) {
        userCollection()?.document(id)?.delete()?.await()
    }
    // Devuelve la referencia a /usuarios/{uid}/movimientos
    private fun userMovsRef(uid: String) =
        firestore.collection("usuarios").document(uid).collection("movimientos")


    override suspend fun getMovimientosBetween(
        userId: String,
        fromMillis: Long,
        toMillis: Long
    ): List<Movimiento> {
        return try {
            userMovsRef(userId) // 👈 ruta correcta
                .whereGreaterThanOrEqualTo("fecha", fromMillis)
                .whereLessThan("fecha", toMillis)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get().await()
                .documents.mapNotNull { it.toObject(Movimiento::class.java) }
        } catch (e: FirebaseFirestoreException) {
            // Protección para que no crashee si por algo pide índice
            if (e.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                throw IllegalStateException("Falta índice para esta consulta. Prueba de nuevo o revisa Firestore.")
            } else throw e
        }
    }


    /** Devuelve la fecha (millis) del movimiento más antiguo del usuario, o null si no hay */
    override suspend fun getEarliestMovimientoMillis(userId: String): Long? {
        val snap = userMovsRef(userId)
            .orderBy("fecha", Query.Direction.ASCENDING)
            .limit(1)
            .get().await()

        return snap.documents.firstOrNull()?.toObject(Movimiento::class.java)?.fecha
    }


}
