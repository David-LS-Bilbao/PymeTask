package com.dls.pymetask.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore de Preferences: reutiliza tu extensión si ya la tienes.
// Si tienes un DataStore central, ajusta el import para usar ese.
private val Context.dataStore by androidx.datastore.preferences.preferencesDataStore(
    name = "app_language_prefs"
)

/**
 * Clase de preferencias para guardar y leer el idioma de la app.
 * Guarda el código de idioma (ej. "es", "en", "fr").
 */
class LanguagePreferences(private val context: Context) {

    // Clave para el idioma seleccionado
    private val KEY_LANGUAGE = stringPreferencesKey("app_language_code")

    /**
     * Flujo reactivo del idioma almacenado. Por defecto "es" (Español).
     */
    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE] ?: "es"
    }

    /**
     * Guarda el idioma (código ISO, p. ej. "es", "en", "fr").
     */
    suspend fun saveLanguage(code: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = code
        }
    }
}
