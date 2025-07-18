package com.dls.pymetask.domain.repository

import com.dls.pymetask.domain.model.Movimiento
import kotlinx.coroutines.flow.Flow

interface MovimientoRepository {
    fun getMovimientos(): Flow<List<Movimiento>>
    suspend fun addMovimiento(movimiento: Movimiento)
    suspend fun updateMovimiento(movimiento: Movimiento)
    suspend fun getMovimientoById(id: String): Movimiento?
    suspend fun insertMovimiento(mov: Movimiento)
    suspend fun deleteMovimiento(id: String)

}
