package com.dls.pymetask.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

/**
 * Devuelve el UID del usuario autenticado de forma segura.
 * Si FirebaseAuth falla, intenta obtenerlo desde SharedPreferences.
 */

fun getUserIdSeguro(context: Context): String? {
    val auth = FirebaseAuth.getInstance()
    val firebaseUserId = auth.currentUser?.uid
    if (!firebaseUserId.isNullOrBlank()) return firebaseUserId

    // Si no está disponible en FirebaseAuth, usar la copia local
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_id", null)
}

// subelooo
