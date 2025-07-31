package com.dls.pymetask.presentation.agenda

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "Tarea pendiente"
        Toast.makeText(context, "⏰ Recordatorio: $titulo", Toast.LENGTH_LONG).show()
        // Aquí puedes lanzar una notificación si lo prefieres
    }
}
