package com.dls.pymetask.fakes

import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.repository.MovimientoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake en memoria para pruebas unitarias.
 * Simula Firestore con una lista mutable + StateFlow.
 */
class FakeMovimientoRepository : MovimientoRepository {

    private val state = MutableStateFlow<List<Movimiento>>(emptyList())

    override fun getMovimientos(): Flow<List<Movimiento>> = state.asStateFlow()
    override suspend fun addMovimiento(movimiento: Movimiento) {
        TODO("Not yet implemented")
    }

    override suspend fun getMovimientosBetween(userId: String, fromMillis: Long, toMillis: Long): List<Movimiento> {
        return state.value.filter { it.userId == userId && it.fecha in fromMillis until toMillis }
            .sortedByDescending { it.fecha }
    }

    override suspend fun getEarliestMovimientoMillis(userId: String): Long? {
        return state.value.filter { it.userId == userId }.minByOrNull { it.fecha }?.fecha
    }

    override suspend fun insertMovimiento(movimiento: Movimiento) {
        state.value = state.value + movimiento
    }

    override suspend fun updateMovimiento(movimiento: Movimiento) {
        state.value = state.value.map { if (it.id == movimiento.id) movimiento else it }
    }

    override suspend fun getMovimientoById(id: String): Movimiento? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMovimiento(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}
