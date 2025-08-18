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
        val base = BuildConfig.OAUTH_BASE_URL.trimEnd('/')
        val uri = "$base/authorize".toUri().buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", BuildConfig.OAUTH_CLIENT_ID)
            .appendQueryParameter("redirect_uri", BuildConfig.OAUTH_REDIRECT_URI)
            .appendQueryParameter("scope", BuildConfig.OAUTH_SCOPES)
            .build()
        android.util.Log.d("OAuth", "Auth URL: $uri") // 👈 log de depuración
        return uri
    }




//    private fun buildAuthorizeUri(): Uri {
//        val base = BuildConfig.OAUTH_BASE_URL.trimEnd('/')
//        return "$base/authorize".toUri().buildUpon()
//            .appendQueryParameter("response_type", "code")
//            .appendQueryParameter("client_id", BuildConfig.OAUTH_CLIENT_ID)
//            .appendQueryParameter("redirect_uri", BuildConfig.OAUTH_REDIRECT_URI)
//            .appendQueryParameter("scope", BuildConfig.OAUTH_SCOPES)
//            // .appendQueryParameter("state", "anti_csrf_token") // opcional, recomendado
//            .build()
//    }

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
