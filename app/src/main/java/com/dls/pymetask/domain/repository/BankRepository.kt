package com.dls.pymetask.domain.repository

/**
 * Repositorio de banco. Devuelve nº de movimientos importados/actualizados.
 */
interface BankRepository {
    suspend fun syncAccount(
        accountId: String,
        fromMillis: Long,
        toMillis: Long
    ): Result<Int>
}
