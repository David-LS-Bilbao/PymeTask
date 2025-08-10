
// AlarmReceiver.kt
package com.dls.pymetask.presentation.agenda

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dls.pymetask.utils.NotificationHelper

/**
 * Recibe el disparo de la alarma y:
 *  - Reproduce el tono elegido (o el de alarma por defecto si no hay)
 *  - Muestra una notificación sin sonido (el sonido lo controlamos nosotros)
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 🔑 Usa la MISMA clave que envía AlarmUtils ("titulo")
        val title = intent.getStringExtra("titulo") ?: "Tarea pendiente"

        // URI del tono elegido (puede ser null -> se usará default TYPE_ALARM)
        val toneUriString = intent.getStringExtra("alarmToneUri")

        // 1) Reproducir sonido de alarma (una sola vez)
        NotificationHelper.playAlarmSound(context, toneUriString)
        Log.d("AlarmReceiver", "🔊 Sonido iniciado para: $title")

        // 2) Mostrar notificación (el canal va sin sonido para evitar doble tono)
        NotificationHelper.showAlarmNotification(
            context = context,
            title   = title,
            message = "Es hora de completar: $title"
        )
        Log.d("AlarmReceiver", "🔔 Notificación emitida")
    }
}







