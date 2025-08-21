package com.dls.pymetask.presentation.ajustes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.preferences.TextScalePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Gestiona el tamaño de fuente global (Small/Medium/Large) y lo persiste en DataStore.
 */
@HiltViewModel
class TextScaleViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    private val prefs = TextScalePreferences(app.applicationContext)

    val textScale: StateFlow<TextScale> =
        prefs.textScale
            .map { stored ->
                runCatching { TextScale.valueOf(stored) }.getOrElse { TextScale.MEDIUM }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, TextScale.MEDIUM)

    fun setTextScale(scale: TextScale) {
        viewModelScope.launch {
            prefs.setTextScale(scale.name)
        }
    }
}
