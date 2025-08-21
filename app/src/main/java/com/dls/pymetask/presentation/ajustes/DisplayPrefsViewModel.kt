package com.dls.pymetask.presentation.ajustes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.preferences.CurrencyOption
import com.dls.pymetask.data.preferences.DateFormatOption
import com.dls.pymetask.data.preferences.DisplayPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** VM que expone fecha/moneda/recordatorio por defecto y permite actualizarlos. */
@HiltViewModel
class DisplayPrefsViewModel @Inject constructor(app: Application) : AndroidViewModel(app) {

    private val prefs = DisplayPreferences(app.applicationContext)

    val dateFormat: StateFlow<DateFormatOption> =
        prefs.dateFormat.stateIn(viewModelScope, SharingStarted.Eagerly, DateFormatOption.DMY_SLASH)

    val currency: StateFlow<CurrencyOption> =
        prefs.currency.stateIn(viewModelScope, SharingStarted.Eagerly, CurrencyOption.EUR)

    val reminderDefault: StateFlow<Boolean> =
        prefs.reminderDefault.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setDateFormat(option: DateFormatOption) = viewModelScope.launch { prefs.setDateFormat(option) }
    fun setCurrency(option: CurrencyOption) = viewModelScope.launch { prefs.setCurrency(option) }
    fun setReminderDefault(enabled: Boolean) = viewModelScope.launch { prefs.setReminderDefault(enabled) }
}
