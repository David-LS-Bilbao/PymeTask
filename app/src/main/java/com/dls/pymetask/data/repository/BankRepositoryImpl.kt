package com.dls.pymetask.data.repository

import android.content.Context
import com.dls.pymetask.data.remote.bank.BankRemoteDataSource
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.repository.BankRepository
import com.dls.pymetask.domain.repository.MovimientoRepository
import com.dls.pymetask.utils.getUserIdSeguro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * Implementación del repositorio de banco.
 * - Pide transacciones al data source.
 * - Las convierte a 'Movimiento' de dominio.
 * - Las guarda vía MovimientoRepository (Firestore bajo el userId).
 */
class BankRepositoryImpl(
    private val remote: BankRemoteDataSource,
    private val movimientoRepo: MovimientoRepository,
    private val appContext: Context
) : BankRepository {

    override suspend fun syncAccount(
        accountId: String,
        fromMillis: Long,
        toMillis: Long
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val userId = getUserIdSeguro(appContext)
                ?: return@withContext Result.failure(IllegalStateException("Usuario no autenticado"))

            val dtos = remote.fetchTransactions(accountId, fromMillis, toMillis)

            // Parser para ISO "yyyy-MM-dd" si bookingMillis es nulo
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            var inserted = 0
            for (dto in dtos) {
                // Fecha: preferimos bookingMillis; si no, parseamos bookingDate
                val fechaMillis = dto.bookingMillis
                    ?: dto.bookingDate?.let { sdf.parse(it)?.time }
                    ?: continue // si no hay fecha, descartamos

                // Determinar ingreso/gasto por signo; almacenamos 'cantidad' como valor ABS en dominio
                val ingreso = dto.amount >= 0.0
                val cantidadAbs = abs(dto.amount)

                // ID estable para evitar duplicados: "bank_{providerId}"
                val movId = "bank_${dto.id}"

                val mov = Movimiento(
                    id = movId,
                    titulo = dto.description ?: "Movimiento bancario",
                    subtitulo = dto.currency,  // puedes usar merchant si lo tienes
                    cantidad = cantidadAbs,
                    ingreso = ingreso,
                    fecha = fechaMillis,
                    userId = userId
                )

                // upsert en Firestore (usa tu 'set' con id)
                movimientoRepo.updateMovimiento(mov)
                inserted++
            }
            Result.success(inserted)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
