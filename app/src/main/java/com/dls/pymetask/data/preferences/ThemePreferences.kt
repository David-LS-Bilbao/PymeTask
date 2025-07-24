package com.dls.pymetask.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.core.DataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = stringPreferencesKey("theme_mode")

    val themeFlow: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            when (preferences[themeKey]) {
                ThemeMode.LIGHT.name -> ThemeMode.LIGHT
                ThemeMode.DARK.name -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    suspend fun saveThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = mode.name
        }
    }
}

