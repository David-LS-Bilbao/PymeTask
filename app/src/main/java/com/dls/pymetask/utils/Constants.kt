package com.dls.pymetask.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth


object Constants{

val coloresDisponibles = listOf(
"Amarillo" to "#FFF9C4",
"Rojo" to "#FFCDD2",
"Verde" to "#C8E6C9",
"Azul" to "#BBDEFB",
"Lila" to "#D1C4E9",
"Blanco" to "#FFFFFF"
)

    fun getUserIdSeguro(context: Context): String? {
        val auth = FirebaseAuth.getInstance()
        val firebaseUserId = auth.currentUser?.uid
        if (!firebaseUserId.isNullOrBlank()) return firebaseUserId

        // Si no está disponible en FirebaseAuth, usar la copia local
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_id", null)
    }



}
