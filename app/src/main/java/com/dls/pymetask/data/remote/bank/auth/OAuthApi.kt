package com.dls.pymetask.data.remote.bank.auth

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Interfaz Retrofit para el endpoint de token OAuth2 del proveedor.
 * TrueLayer/Tink/GoCardless suelen usar 'application/x-www-form-urlencoded'.
 * Ajusta la ruta "connect/token" si tu proveedor usa otra (p. ej. "/oauth/token").
 */
interface OAuthApi {

    @FormUrlEncoded
    @POST("connect/token") // <- cambia si el proveedor usa otra ruta
    suspend fun token(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String?,
        @Field("redirect_uri") redirectUri: String?,
        @Field("code") code: String?,
        @Field("refresh_token") refreshToken: String?,
        @Field("scope") scope: String? // algunos proveedores requieren scope en el token exchange
    ): TokenResponse
}

/** Respuesta estándar de token OAuth2 */
data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Long,
    val refresh_token: String?
)
