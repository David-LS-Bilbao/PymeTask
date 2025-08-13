package com.dls.pymetask.data.remote.bank.auth


/**
 * Abstracción para guardar/cargar/borrar tokens.
 */
interface TokenStore {
    fun save(tokens: OAuthTokens)
    fun load(): OAuthTokens?
    fun clear()
}
