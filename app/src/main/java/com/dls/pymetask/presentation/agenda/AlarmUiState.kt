package com.dls.pymetask.presentation.agenda


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Estado global muy simple para comunicar "qué tarea está sonando" a la UI.
 * Cuando AlarmReceiver dispare la alarma, pondrá aquí el taskId.
 * AgendaScreen lo observará para activar el parpadeo.
 */
object AlarmUiState {
    private val _currentRingingTaskId = MutableStateFlow<String?>(null)
    val currentRingingTaskId: StateFlow<String?> = _currentRingingTaskId

    /** Llamar cuando empieza a sonar una alarma */
    fun startBlink(taskId: String?) {
        _currentRingingTaskId.value = taskId
    }

    /** Llamar cuando se para el sonido / se abre la tarjeta */
    fun stopBlink() {
        _currentRingingTaskId.value = null
    }
}
