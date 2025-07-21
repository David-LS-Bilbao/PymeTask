package com.dls.pymetask.domain.repository

import com.google.firebase.firestore.auth.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(email: String, password: String): Result<Unit>
    fun logout()
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun isUserLoggedIn(): Boolean


}