package com.dls.pymetask.data.remote.bank


import android.os.Build
import androidx.annotation.RequiresApi
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.math.abs

/**
 * Interceptor que simula respuestas de la API del banco.
 * Útil en desarrollo cuando la baseUrl es "mock".
 */
class MockBankInterceptor : Interceptor {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val url = req.url.toString()

        if (req.url.host != "api.mockbank.local") {
            return chain.proceed(req)
        }

        // Solo interceptamos el endpoint de transacciones
        val match = Regex(".*/accounts/([^/]+)/transactions.*").find(url) ?: return passThrough(chain)
        val accountId = match.groupValues[1]

        // Extraer parámetros "from" y "to"
        val query = req.url.query ?: ""
        val params = query.split("&").mapNotNull {
            val kv = it.split("=")
            if (kv.size == 2) kv[0] to URLDecoder.decode(kv[1], StandardCharsets.UTF_8) else null
        }.toMap()

        val from = params["from"]?.toLongOrNull() ?: 0L
        val to   = params["to"]?.toLongOrNull() ?: System.currentTimeMillis()

        // Generamos 6-10 movimientos mock entre 'from' y 'to'
        val txs = buildString {
            append("""{"transactions":[""")
            val count = 8
            val step = ((to - from).coerceAtLeast(1L)) / (count + 1)
            var first = true
            for (i in 1..count) {
                val t = from + step * i
                // Alterna ingreso/gasto
                val amount = if (i % 2 == 0) 120.50 + i else -(80.30 + i)
                val id = "tx_${accountId}_$i"
                val obj = """
                  {
                    "id":"$id",
                    "bookingMillis":$t,
                    "bookingDate":null,
                    "amount":$amount,
                    "currency":"EUR",
                    "description":"Operación #$i"
                  }
                """.trimIndent()
                if (!first) append(",")
                append(obj)
                first = false
            }
            append("""]}""")
        }

        val body = txs.toResponseBody("application/json".toMediaType())
        return Response.Builder()
            .request(req)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body)
            .build()
    }

    private fun passThrough(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
