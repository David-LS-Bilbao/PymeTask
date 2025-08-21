package com.dls.pymetask.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.textScaleDataStore by preferencesDataStore(name = "text_scale_prefs")

class TextScalePreferences(private val context: Context) {
    companion object {
        private val KEY_TEXT_SCALE = stringPreferencesKey("text_scale")
        private const val DEFAULT = "MEDIUM"
    }

    val textScale: Flow<String> = context.textScaleDataStore.data.map { prefs: Preferences ->
        prefs[KEY_TEXT_SCALE] ?: DEFAULT
    }

    suspend fun setTextScale(value: String) {
        context.textScaleDataStore.edit { it[KEY_TEXT_SCALE] = value }
    }
}
