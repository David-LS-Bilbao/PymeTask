package com.dls.pymetask.data.remote.bank

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz Retrofit para la API del banco (genérica).
 * Ajusta las rutas/params cuando definas proveedor real.
 */
interface BankApi {

    /**
     * Devuelve las transacciones de una cuenta entre fechas (epoch millis o ISO, según proveedor).
     * Aquí usamos epoch millis para simplificar; cambia tipos si el proveedor usa ISO.
     */
    @GET("accounts/{accountId}/transactions")
    suspend fun getTransactions(
        @Path("accountId") accountId: String,
        @Query("from") fromMillis: Long,
        @Query("to") toMillis: Long
    ): TransactionsResponse

    // TrueLayer Data API – listado de cuentas del usuario
    @GET("data/v1/accounts")
    suspend fun getAccounts(): AccountsResponse

}

/** Respuesta JSON simple */
data class TransactionsResponse(
    val transactions: List<BankTransactionDto>
)

/** DTO de transacción del banco (ajústalo a tu proveedor real) */
data class BankTransactionDto(
    val id: String,            // id del proveedor
    val bookingDate: String?,  // fecha contable en ISO "yyyy-MM-dd" (si tu proveedor lo da así)
    val bookingMillis: Long?,  // o epoch millis si viene ya en long
    val amount: Double,        // positivo = ingreso, negativo = gasto
    val currency: String,
    val description: String?   // concepto / merchant
)



// Respuesta mínima para la UI
data class AccountsResponse(val results: List<AccountDto>)
data class AccountDto(
    @SerializedName("account_id") val accountId: String,
    @SerializedName("display_name") val displayName: String?,
    val currency: String?
)
