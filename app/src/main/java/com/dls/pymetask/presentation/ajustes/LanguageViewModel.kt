package com.dls.pymetask.presentation.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.preferences.LanguagePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para gestionar el idioma (leer/guardar).
 * Expone un StateFlow<String> con el código de idioma actual.
 */
@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languagePrefs: LanguagePreferences
) : ViewModel() {

    // Idioma actual como flujo de estado (por defecto "es")
    val languageCode: StateFlow<String> = languagePrefs.languageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "es")

    /**
     * Guarda un nuevo idioma (código ISO) en DataStore.
     */
    fun setLanguage(code: String) {
        viewModelScope.launch {
            languagePrefs.saveLanguage(code)
        }
    }
}
