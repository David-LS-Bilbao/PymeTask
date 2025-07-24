package com.dls.pymetask.presentation.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.preferences.ThemeMode
import com.dls.pymetask.data.preferences.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePrefs: ThemePreferences
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePrefs.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            themePrefs.saveThemeMode(mode)
        }
    }
}

