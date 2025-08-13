package com.dls.pymetask.data.remote.bank.auth



import com.dls.pymetask.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

/**
 * Authenticator de OkHttp que intenta refrescar el accessToken cuando el servidor devuelve 401.
 * - Usa el refresh_token guardado para obtener un nuevo access_token.
 * - Actualiza el TokenStore y reintenta la request original con el token nuevo.
 * - Bloquea (runBlocking) porque Authenticator es síncrono por diseño de OkHttp.
 */
class OAuthAuthenticator(
    private val tokenStore: TokenStore,
    private val oauthApi: OAuthApi
) : Authenticator {

    // Bandera para evitar tormenta de refresh simultáneos
    private val refreshing = AtomicBoolean(false)

    override fun authenticate(route: Route?, response: Response): Request? {
        // Si ya hemos intentado varias veces, no seguir
        if (responseCount(response) >= 2) return null

        // Evitar múltiples refresh en paralelo
        if (!refreshing.compareAndSet(false, true)) {
            // Otro hilo ya refresca; no creamos más oleadas.
            return null
        }

        try {
            val current = tokenStore.load() ?: return null
            val refresh = current.refreshToken ?: return null

            // Llamada síncrona (bloqueante) al token endpoint para refrescar
            val newTokens = runBlocking {
                val resp = oauthApi.token(
                    grantType = "refresh_token",
                    clientId = BuildConfig.OAUTH_CLIENT_ID,
                    clientSecret = BuildConfig.OAUTH_CLIENT_SECRET.takeIf { it.isNotBlank() },
                    redirectUri = null,
                    code = null,
                    refreshToken = refresh,
                    scope = null
                )
                // Margen de seguridad de 30s antes del expire
                val expiresAt = System.currentTimeMillis() + max(0L, (resp.expires_in - 30L)) * 1000L
                OAuthTokens(
                    accessToken = resp.access_token,
                    refreshToken = resp.refresh_token ?: current.refreshToken,
                    tokenType = resp.token_type,
                    expiresAtMillis = expiresAt
                )
            }

            // Guardar nuevos tokens
            tokenStore.save(newTokens)

            // Reintentar la request original con el nuevo token
            return response.request.newBuilder()
                .header("Authorization", "${newTokens.tokenType} ${newTokens.accessToken}")
                .build()

        } catch (t: Throwable) {
            return null // refresh falló
        } finally {
            refreshing.set(false)
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
