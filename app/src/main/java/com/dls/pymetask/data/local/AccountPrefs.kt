
package com.dls.pymetask.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión DataStore ligada a Context (una única instancia por proceso)
private val Context.accountDataStore by preferencesDataStore("account_prefs")
private val KEY_SELECTED_ACCOUNT = stringPreferencesKey("selected_account_id")

/**
 * Almacén de la cuenta bancaria seleccionada por el usuario.
 * - Se inyecta Context de aplicación con @ApplicationContext
 * - @Singleton: una única instancia durante el ciclo de vida de la app
 */
@Singleton
class AccountPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Guarda la cuenta seleccionada */
    suspend fun setSelectedAccountId(id: String) {
        context.accountDataStore.edit { it[KEY_SELECTED_ACCOUNT] = id }
    }

    /** Flujo con la cuenta seleccionada (null si no hay) */
    fun selectedAccountId(): Flow<String?> =
        context.accountDataStore.data.map { it[KEY_SELECTED_ACCOUNT] }
}
