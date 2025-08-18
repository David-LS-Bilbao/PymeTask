package com.dls.pymetask.data.remote.bank.auth

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.dls.pymetask.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import androidx.core.net.toUri
import java.util.UUID

/**
 * OAuthManager:
 * - Construye la URL de autorización y abre Custom Tabs para el consentimiento.
 * - Gestiona el redirect (URI) para canjear el 'code' por tokens y guardarlos.
 * - Expuesto como clase simple para usar desde VM/UI.
 */
class OAuthManager(
    private val context: Context,
    private val oauthApi: OAuthApi,
    private val tokenStore: TokenStore,
) {

    /** Abre el navegador (Custom Tabs) con la URL de autorización del proveedor */
    fun startAuth(activity: Activity) {
        val authUri = buildAuthorizeUri()
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(activity, authUri)
    }

    /** Construye la URI de autorización. Ajusta parámetros a tu proveedor si difieren. */

    private fun buildAuthorizeUri(): Uri {

            // 👇 Debe ser sandbox si tu client_id es sandbox-...
            val base = BuildConfig.OAUTH_BASE_URL.trimEnd('/') // "https://auth.truelayer-sandbox.com"
            val state = UUID.randomUUID().toString() // recomendado por seguridad (CSRF)

            // TrueLayer acepta el authorize en la raíz del host con query params.
            // No añadas un path, basta con el host y los parámetros.
            val builder = base.toUri().buildUpon()
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("client_id", BuildConfig.OAUTH_CLIENT_ID)
                .appendQueryParameter("redirect_uri", BuildConfig.OAUTH_REDIRECT_URI)
                .appendQueryParameter("scope", BuildConfig.OAUTH_SCOPES)
                .appendQueryParameter("response_mode", "form_post") // recomendado para móviles
                .appendQueryParameter("state", state)

            // 🔧 Para primeras pruebas, NO fuerces provider_id; deja que el flujo muestre el mock/bancos soportados.
            // builder.appendQueryParameter("providers", "uk-cs-mock")
            // builder.appendQueryParameter("provider_id", "uk-cs-mock")

            val uri = builder.build()
            android.util.Log.d("OAuth", "Auth URL: $uri")
            return uri


//        val base = BuildConfig.OAUTH_BASE_URL.trimEnd('/') // "https://auth.truelayer.com"
//        // ⚠️ NO añadimos "/authorize"
//        val builder = base.toUri().buildUpon()
//            .appendQueryParameter("response_type", "code")
//            .appendQueryParameter("client_id", BuildConfig.OAUTH_CLIENT_ID)
//            .appendQueryParameter("redirect_uri", BuildConfig.OAUTH_REDIRECT_URI)
//            .appendQueryParameter("scope", BuildConfig.OAUTH_SCOPES)
//        // .appendQueryParameter("state", YOUR_RANDOM_STATE) // recomendado
//
//        // 💡 Para probar con Mock Bank (sandbox):
//        // - muestra solo el banco simulado y salta selección
//        builder.appendQueryParameter("providers", "uk-cs-mock")
//        builder.appendQueryParameter("provider_id", "uk-cs-mock")
//
//        // 🔁 Si más adelante pasamos a producción ES:
//        // - quita las 2 líneas de arriba y usa, por ejemplo:
//        // builder.appendQueryParameter("country_id", "ES") // salta selección de país
//        // (sin 'providers' mostrará los bancos soportados para ES)
//
//        val uri = builder.build()
//        android.util.Log.d("OAuth", "Auth URL: $uri")
//        return uri
    }

    /**
     * Maneja el redirect: extrae 'code', intercambia por tokens y los guarda.
     * Llama a esta función desde la Activity receptora del deep link.
     */
    suspend fun handleRedirect(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        val code = uri.getQueryParameter("code")
            ?: return@withContext Result.failure(IllegalStateException("Falta 'code' en redirect"))

        try {
            val resp = oauthApi.token(
                grantType = "authorization_code",
                clientId = BuildConfig.OAUTH_CLIENT_ID,
                clientSecret = BuildConfig.OAUTH_CLIENT_SECRET.takeIf { it.isNotBlank() },
                redirectUri = BuildConfig.OAUTH_REDIRECT_URI,
                code = code,
                refreshToken = null,
                scope = BuildConfig.OAUTH_SCOPES
            )
            val expiresAt = System.currentTimeMillis() + max(0L, (resp.expires_in - 30L)) * 1000L
            tokenStore.save(
                OAuthTokens(
                    accessToken = resp.access_token,
                    refreshToken = resp.refresh_token,
                    tokenType = resp.token_type,
                    expiresAtMillis = expiresAt
                )
            )
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
