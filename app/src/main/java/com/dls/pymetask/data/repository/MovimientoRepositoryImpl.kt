package com.dls.pymetask.data.repository

import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.repository.MovimientoRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MovimientoRepositoryImpl(
    private val firestore: FirebaseFirestore
) : MovimientoRepository {

    private val collection = firestore.collection("movimientos")

    override fun getMovimientos(): Flow<List<Movimiento>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { it.toObject<Movimiento>() }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addMovimiento(movimiento: Movimiento) {
        collection.document(movimiento.id).set(movimiento).await()
    }

    override suspend fun updateMovimiento(movimiento: Movimiento) {
        collection.document(movimiento.id).set(movimiento).await()
    }

    override suspend fun getMovimientoById(id: String): Movimiento? {
        val doc = collection.document(id).get().await()
        return doc.toObject()
    }

    override suspend fun insertMovimiento(mov: Movimiento) {
        firestore.collection("movimientos")
            .document(mov.id)
            .set(mov)
    }

}