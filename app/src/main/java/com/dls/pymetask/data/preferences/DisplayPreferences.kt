package com.dls.pymetask.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore para preferencias de visualización y recordatorios
private val Context.displayDataStore by preferencesDataStore(name = "display_prefs")

/** Formato de fecha que usará la app para pintar fechas. */
enum class DateFormatOption { DMY_SLASH, DMY_TEXT, RELATIVE }

/** Moneda por defecto para cantidades. */
enum class CurrencyOption(val iso: String) { EUR("EUR"), USD("USD"), GBP("GBP") }

class DisplayPreferences(private val context: Context) {

    companion object {
        private val KEY_DATE_FORMAT = stringPreferencesKey("date_format")
        private val KEY_CURRENCY = stringPreferencesKey("currency")
        private val KEY_REMINDER_DEFAULT = booleanPreferencesKey("reminder_default")
        private const val DF_DEFAULT = "DMY_SLASH"
        private const val CUR_DEFAULT = "EUR"
        private const val REM_DEFAULT = false
    }

    /** Flujo del formato de fecha preferido. */
    val dateFormat: Flow<DateFormatOption> =
        context.displayDataStore.data.map { prefs: Preferences ->
            runCatching { DateFormatOption.valueOf(prefs[KEY_DATE_FORMAT] ?: DF_DEFAULT) }
                .getOrElse { DateFormatOption.DMY_SLASH }
        }

    /** Flujo de la moneda preferida. */
    val currency: Flow<CurrencyOption> =
        context.displayDataStore.data.map { prefs: Preferences ->
            runCatching { CurrencyOption.valueOf(prefs[KEY_CURRENCY] ?: CUR_DEFAULT) }
                .getOrElse { CurrencyOption.EUR }
        }

    /** Flujo: ¿activar alarma por defecto al crear tareas? */
    val reminderDefault: Flow<Boolean> =
        context.displayDataStore.data.map { it[KEY_REMINDER_DEFAULT] ?: REM_DEFAULT }

    /** Setters */
    suspend fun setDateFormat(option: DateFormatOption) {
        context.displayDataStore.edit { it[KEY_DATE_FORMAT] = option.name }
    }

    suspend fun setCurrency(option: CurrencyOption) {
        context.displayDataStore.edit { it[KEY_CURRENCY] = option.name }
    }

    suspend fun setReminderDefault(enabled: Boolean) {
        context.displayDataStore.edit { it[KEY_REMINDER_DEFAULT] = enabled }
    }
}
