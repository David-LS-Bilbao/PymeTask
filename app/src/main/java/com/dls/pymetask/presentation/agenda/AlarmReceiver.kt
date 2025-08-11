package com.dls.pymetask.presentation.agenda

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dls.pymetask.utils.NotificationHelper

/**
 * Recibe la alarma y:
 *  - Reproduce el tono elegido (o por defecto si null)
 *  - Muestra notificación (canal sin sonido para evitar doble audio)
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("titulo") ?: "Tarea pendiente" // clave correcta
        val toneUriString = intent.getStringExtra("alarmToneUri")        // puede ser null

        val taskId = intent.getStringExtra("taskId")


        NotificationHelper.playAlarmSound(context, toneUriString)        // 🔊 una sola vez

        // ⇣ Pasamos taskId a la notificación para abrir Agenda y desactivar esa alarma
        NotificationHelper.showAlarmNotification(
            context = context,
            title   = title,
            message = "Es hora de completar: $title",
            taskId  = taskId
        )
    }
}








