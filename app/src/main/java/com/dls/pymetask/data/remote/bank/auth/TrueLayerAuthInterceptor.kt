package com.dls.pymetask.data.remote.bank.auth

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de autenticación OAuth2 para el proveedor bancario (TrueLayer).
 * - Inyecta el header Authorization: Bearer <access_token> en cada request.
 * - Si el servidor devuelve 401, dispara onUnauthorized() para que tu app refresque el token.
 *
 * NOTA: tokenProvider y onUnauthorized se pasan por constructor para no acoplar a la capa de datos.
 */
class TrueLayerAuthInterceptor(
    private val tokenProvider: () -> String?,   // devuelve el access_token actual o null si no hay
    private val onUnauthorized: () -> Unit      // callback para gestionar refresh al recibir 401
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Si hay token, añadimos el header Authorization
        val token = tokenProvider()
        val authedRequest = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(authedRequest)

        // Si el token ha caducado, avisamos para refrescar (p. ej., en un ViewModel/Repo)
        if (response.code == 401) {
            response.close()
            onUnauthorized()
            // No reintentamos aquí para mantener la lógica de refresh fuera del interceptor.
            // Tras refrescar, la siguiente llamada tendrá el token nuevo.
        }

        return response
    }
}
