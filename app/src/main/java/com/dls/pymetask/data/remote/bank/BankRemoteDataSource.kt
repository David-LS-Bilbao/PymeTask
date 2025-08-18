package com.dls.pymetask.data.remote.bank

/**
 * Abstracción del origen remoto de banco (para facilitar tests y cambiar proveedor).
 */
interface BankRemoteDataSource {
    suspend fun fetchTransactions(
        accountId: String,
        fromMillis: Long,
        toMillis: Long
    ): List<BankTransactionDto>

    suspend fun getAccounts(): List<AccountDto>
}

class BankRemoteDataSourceImpl(
    private val api: BankApi
) : BankRemoteDataSource {

    override suspend fun fetchTransactions(
        accountId: String,
        fromMillis: Long,
        toMillis: Long
    ): List<BankTransactionDto> {
        // Llama a la API y devuelve la lista (maneja errores arriba en el repo)
        return api.getTransactions(accountId, fromMillis, toMillis).transactions
    }

    override suspend fun getAccounts(): List<AccountDto> = api.getAccounts().results

}
