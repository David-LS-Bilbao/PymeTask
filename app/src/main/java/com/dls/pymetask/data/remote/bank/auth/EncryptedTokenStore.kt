@file:Suppress("DEPRECATION")

package com.dls.pymetask.data.remote.bank.auth


import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

/**
 * Implementación segura de TokenStore usando EncryptedSharedPreferences.
 * Guarda los tokens cifrados en el dispositivo.
 */
class EncryptedTokenStore(context: Context) : TokenStore {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM) // Clave maestra
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_tokens_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun save(tokens: OAuthTokens) {
        prefs.edit {
            putString("accessToken", tokens.accessToken)
                .putString("refreshToken", tokens.refreshToken)
                .putString("tokenType", tokens.tokenType)
                .putLong("expiresAtMillis", tokens.expiresAtMillis)
        }
    }

    override fun load(): OAuthTokens? {
        val access = prefs.getString("accessToken", null) ?: return null
        val type = prefs.getString("tokenType", "Bearer") ?: "Bearer"
        val refresh = prefs.getString("refreshToken", null)
        val exp = prefs.getLong("expiresAtMillis", 0L)
        return OAuthTokens(
            accessToken = access,
            refreshToken = refresh,
            tokenType = type,
            expiresAtMillis = exp
        )
    }

    override fun clear() {
        prefs.edit { clear() }
    }
}
