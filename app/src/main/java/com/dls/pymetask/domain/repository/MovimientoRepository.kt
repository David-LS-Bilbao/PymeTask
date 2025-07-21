package com.dls.pymetask.domain.repository

import com.dls.pymetask.domain.model.Movimiento
import kotlinx.coroutines.flow.Flow

// interface que define el contrato para el repositorio de movimientos en Firebase
interface MovimientoRepository {
    fun getMovimientos(): Flow<List<Movimiento>>// devuelve un flujo de listas de movimientos
    suspend fun addMovimiento(movimiento: Movimiento)// agrega un movimiento a la base de datos
    suspend fun updateMovimiento(movimiento: Movimiento){}// actualiza un movimiento en la base de datos
    suspend fun getMovimientoById(id: String): Movimiento?// devuelve un movimiento por su id
    suspend fun insertMovimiento(mov: Movimiento)// inserta un movimiento en la base de datos
    suspend fun deleteMovimiento(id: String)// elimina un movimiento de la base de datos

}
