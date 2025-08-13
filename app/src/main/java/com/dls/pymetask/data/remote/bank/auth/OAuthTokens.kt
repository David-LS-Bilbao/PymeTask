package com.dls.pymetask.data.remote.bank.auth

/**
 * Modelo de tokens OAuth2 guardados en el dispositivo.
 * - accessToken: token de acceso actual (Bearer)
 * - refreshToken: para renovar el accessToken cuando caduque (puede ser null si el proveedor no lo da)
 * - tokenType: "Bearer"
 * - expiresAtMillis: momento en el que expira el accessToken (epoch ms, con margen de seguridad aplicado)
 */
data class OAuthTokens(
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val expiresAtMillis: Long
) {
    /** Indica si el token ha caducado comparado con 'now' */
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean =
        nowMillis >= expiresAtMillis
}

