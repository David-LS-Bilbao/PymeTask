package com.dls.pymetask.data.remote.bank.auth

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que añade el header Authorization: Bearer <accessToken> si existe.
 * No hace refresh; eso lo gestiona el Authenticator cuando se recibe un 401.
 */
class BearerInterceptor(
    private val tokenStore: TokenStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokens = tokenStore.load()
        val req = if (tokens?.accessToken != null) {
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer ${tokens.accessToken}")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(req)
    }
}
