package com.dls.pymetask.data.preferences

import android.content.Context
import androidx.core.content.edit

@Suppress("UNCHECKED_CAST")
class DefaultAppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("preferencias_archivos", Context.MODE_PRIVATE)

    fun guardarApp(mime: String, packageName: String) {
        prefs.edit { putString(mime, packageName) }
    }

    fun obtenerApp(mime: String): String? {
        return prefs.getString(mime, null)
    }

    fun eliminarApp(mime: String) {
        prefs.edit { remove(mime) }
    }

    fun obtenerTodas(): Map<String, String> {
        return prefs.all.filterValues { it is String } as Map<String, String>
    }

    fun limpiarTodas() {
        prefs.edit { clear() }
    }
}
