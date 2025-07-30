package com.dls.pymetask.domain.repository

import android.content.Context
import com.google.firebase.firestore.auth.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String, nombre: String, fotoUrl: String? = null): Result<Unit>
    fun logout(context: Context)
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun isUserLoggedIn(): Boolean

    // solo para samsung s24++ y similares
    fun marcarSesionActiva(context: Context)
    fun sesionMarcada(context: Context): Boolean



}