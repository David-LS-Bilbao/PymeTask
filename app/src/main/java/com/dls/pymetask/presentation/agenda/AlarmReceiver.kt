// AlarmReceiver.kt
package com.dls.pymetask.presentation.agenda

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dls.pymetask.utils.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("taskTitle") ?: "Tarea pendiente"
        Log.d("AlarmReceiver", "📢 Alarma recibida para: $title")

        // 1) Reproducir sonido de alarma
        NotificationHelper.playAlarmSound(context)
        Log.d("AlarmReceiver", "🔊 Sonido iniciado")

        // 2) Mostrar notificación
        NotificationHelper.showAlarmNotification(
            context,
            title,
            "Es hora de completar: $title"
        )
        Log.d("AlarmReceiver", "🔔 Notificación emitida")
    }
}






