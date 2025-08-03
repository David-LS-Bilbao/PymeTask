package com.dls.pymetask.utils

import android.app.PendingIntent
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.getSystemService

import android.Manifest
import android.Manifest.permission.SCHEDULE_EXACT_ALARM
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.presentation.agenda.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
// ... otras importaciones
import javax.inject.Inject // Para inyección de constructor si lo necesitas



@Suppress("DEPRECATION")
class AlarmUtils @Inject constructor(@ApplicationContext private val context: Context) {



    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarma(tarea: Tarea) { // El contexto ya está disponible como propiedad de la clase
        if (tarea.fecha.isBlank() || tarea.hora.isBlank()) return

        val format = SimpleDateFormat(
            "dd 'de' MMMM 'de' yyyy HH:mm",
            java.util.Locale("es", "ES")
        )
        val fechaHora = "${tarea.fecha} ${tarea.hora}"
        val date = format.parse(fechaHora) ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("titulo", tarea.titulo)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            tarea.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            date.time,
            pendingIntent
        )
    }
}