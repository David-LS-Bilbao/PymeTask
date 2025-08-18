package com.dls.pymetask.domain.repository

import com.dls.pymetask.data.remote.bank.AccountDto

/**
 * Repositorio de banco. Devuelve nº de movimientos importados/actualizados.
 */
interface BankRepository {
    suspend fun syncAccount(
        accountId: String,
        fromMillis: Long,
        toMillis: Long
    ): Result<Int>

    suspend fun fetchAccounts(): List<AccountDto>

}
