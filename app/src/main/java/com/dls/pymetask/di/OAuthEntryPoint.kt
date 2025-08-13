package com.dls.pymetask.di

import com.dls.pymetask.data.remote.bank.auth.OAuthManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * EntryPoint para obtener OAuthManager desde cualquier contexto (Compose, etc.)
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface OAuthEntryPoint {
    fun oauthManager(): OAuthManager
}
