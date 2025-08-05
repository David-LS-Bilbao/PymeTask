package com.dls.pymetask.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.presentation.agenda.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Suppress("DEPRECATION")
class AlarmUtils @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarma(tarea: Tarea) {
        if (tarea.fecha.isBlank() || tarea.hora.isBlank()) return

        try {
            val formatterFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formatterHora = DateTimeFormatter.ofPattern("HH:mm")

            val localDate = LocalDate.parse(tarea.fecha, formatterFecha)
            val localTime = LocalTime.parse(tarea.hora, formatterHora)

            val zonedDateTime = ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault())
            val alarmTimeMillis = zonedDateTime.toInstant().toEpochMilli()


            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("titulo", tarea.titulo)
                putExtra("taskId", tarea.id) // ⬅️ añadimos el ID
            }




            val pendingIntent = PendingIntent.getBroadcast(
                context,
                tarea.id.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w("AlarmUtils", "⚠️ No tiene permiso para alarmas exactas. Usando set() como alternativa.")
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent)

                    // Redirige al usuario para habilitar alarmas exactas
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = "package:${context.packageName}".toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)

                    return
                }
            }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent)
            Log.d("AlarmUtils", "✅ Alarma programada para $zonedDateTime")
        } catch (e: Exception) {
            Log.e("AlarmUtils", "❌ Error al programar la alarma: ${e.message}")
        }
    }
}