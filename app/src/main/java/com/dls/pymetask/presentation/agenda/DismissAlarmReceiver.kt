package com.dls.pymetask.presentation.agenda

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.dls.pymetask.utils.AlarmUtils
import com.dls.pymetask.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receiver que maneja la acción de desactivar alarma desde la notificación.
 * Cancela la alarma, detiene el sonido y cierra la notificación.
 */
@AndroidEntryPoint
class DismissAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmUtils: AlarmUtils

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.dls.pymetask.DISMISS_ALARM") {
            val taskId = intent.getStringExtra("taskId")

            if (taskId != null) {
                try {
                    // 1. Cancelar la alarma
                    alarmUtils.cancelarAlarma(taskId)

                    // 2. Detener el sonido y cancelar la notificación
                    NotificationHelper.stopAlarmSound()
                    NotificationHelper.cancelActiveAlarmNotification(context, taskId)

                    // 3. Detener el parpadeo de la UI
                    AlarmUiState.stopBlink()

                    Log.d("DismissAlarmReceiver", "✅ Alarma desactivada para taskId: $taskId")

                    // Feedback al usuario
                    Toast.makeText(context, "Alarma desactivada", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("DismissAlarmReceiver", "❌ Error al desactivar alarma: ${e.message}")
                }
            }
        }
    }
}

