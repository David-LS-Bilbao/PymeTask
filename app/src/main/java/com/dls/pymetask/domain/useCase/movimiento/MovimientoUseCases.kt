package com.dls.pymetask.domain.useCase.movimiento

import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.repository.MovimientoRepository
import kotlinx.coroutines.flow.Flow

data class MovimientoUseCases(
    val getMovimientos: GetMovimientos,
    val getMovimientosBetween: GetMovimientosBetween,
    val getEarliestMovimientoMillis: GetEarliestMovimientoMillis,
    val addMovimiento: AddMovimiento,
    val updateMovimiento: UpdateMovimiento,
    val deleteMovimiento: DeleteMovimiento
)
class GetMovimientos(private val repo: MovimientoRepository) {
    operator fun invoke(): Flow<List<Movimiento>> = repo.getMovimientos()
}

class GetMovimientosBetween(private val repo: MovimientoRepository) {
    suspend operator fun invoke(userId: String, fromMillis: Long, toMillis: Long): List<Movimiento> =
        repo.getMovimientosBetween(userId, fromMillis, toMillis)
}

class GetEarliestMovimientoMillis(private val repo: MovimientoRepository) {
    suspend operator fun invoke(userId: String): Long? = repo.getEarliestMovimientoMillis(userId)
}

class AddMovimiento(private val repo: MovimientoRepository) {
    suspend operator fun invoke(movimiento: Movimiento) = repo.insertMovimiento(movimiento)
}

class UpdateMovimiento(private val repo: MovimientoRepository) {
    suspend operator fun invoke(movimiento: Movimiento) = repo.updateMovimiento(movimiento)
}

class DeleteMovimiento(private val repo: MovimientoRepository) {
    suspend operator fun invoke(id: String) = repo.deleteMovimiento(id)
}