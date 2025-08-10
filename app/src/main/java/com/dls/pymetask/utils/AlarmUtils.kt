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
import com.dls.pymetask.presentation.agenda.PreferencesHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.max

@Suppress("DEPRECATION")
class AlarmUtils @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Programa una alarma exacta para la tarea:
     * - Parsea fecha/hora ("yyyy-MM-dd", "HH:mm")
     * - Resta los minutos de aviso configurados en PreferencesHelper (0,5,10,15,30,60)
     * - Añade al Intent el título, id y el alarmToneUri seleccionado por el usuario
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarma(tarea: Tarea) {
        if (tarea.fecha.isBlank() || tarea.hora.isBlank()) return

        try {
            // 1) Parseo seguro de fecha/hora
            val formatterFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formatterHora  = DateTimeFormatter.ofPattern("HH:mm")

            val localDate = LocalDate.parse(tarea.fecha, formatterFecha)
            val localTime = LocalTime.parse(tarea.hora,  formatterHora)

            // 2) Restar minutos de aviso configurados por el usuario
            val leadMinutes = PreferencesHelper.getLeadMinutes(context).coerceAtLeast(0)
            val zonedDateTime = ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault())
                .minusMinutes(leadMinutes.toLong())

            // Evitar programar en pasado si el usuario elige un lead grande
            val nowMillis = System.currentTimeMillis()
            val alarmTimeMillis = max(zonedDateTime.toInstant().toEpochMilli(), nowMillis + 1_000L)

            // 3) Construir Intent -> Receiver con extras útiles
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("titulo", tarea.titulo)          // título para notificación
                putExtra("taskId", tarea.id)              // id por si quieres cancelarla
                // Pasamos el tono elegido (puede ser null = por defecto)
                putExtra("alarmToneUri", PreferencesHelper.getToneUri(context))
            }

            // 4) PendingIntent único por tarea (hash del id)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                tarea.id.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // 5) Programación exacta (y permiso en Android 12+)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmUtils", "⚠️ Sin permiso para alarmas exactas. Uso set() y solicito permiso.")
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent)
                // Redirigir a ajustes para permitir alarmas exactas
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return
            }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent)
            Log.d("AlarmUtils", "✅ Alarma programada para $zonedDateTime (lead=$leadMinutes min)")
        } catch (e: Exception) {
            Log.e("AlarmUtils", "❌ Error al programar la alarma: ${e.message}")
        }
    }
}











//package com.dls.pymetask.utils
//
//import android.annotation.SuppressLint
//import android.app.AlarmManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.provider.Settings
//import android.util.Log
//import androidx.annotation.RequiresApi
//import androidx.core.net.toUri
//import com.dls.pymetask.domain.model.Tarea
//import com.dls.pymetask.presentation.agenda.AlarmReceiver
//import com.dls.pymetask.presentation.agenda.PreferencesHelper
//import dagger.hilt.android.qualifiers.ApplicationContext
//import java.time.LocalDate
//import java.time.LocalTime
//import java.time.ZoneId
//import java.time.ZonedDateTime
//import java.time.format.DateTimeFormatter
//import javax.inject.Inject
//
//@Suppress("DEPRECATION")
//class AlarmUtils @Inject constructor(
//    @ApplicationContext private val context: Context,
//) {
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    @SuppressLint("ScheduleExactAlarm")
//    fun programarAlarma(tarea: Tarea) {
//        if (tarea.fecha.isBlank() || tarea.hora.isBlank()) return
//
//        try {
//            val formatterFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//            val formatterHora = DateTimeFormatter.ofPattern("HH:mm")
//
//            val localDate = LocalDate.parse(tarea.fecha, formatterFecha)
//            val localTime = LocalTime.parse(tarea.hora, formatterHora)
//
//            val zonedDateTime = ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault())
//            val alarmTimeMillis = zonedDateTime.toInstant().toEpochMilli()
//
//
//            val intent = Intent(context, AlarmReceiver::class.java).apply {
//                putExtra("titulo", tarea.titulo)
//                putExtra("taskId", tarea.id)
//
//                // 📌 Leer el tono guardado en preferencias y pasarlo al receiver
//                val toneUri = PreferencesHelper.getToneUri(context)
//                putExtra("alarmToneUri", toneUri) // puede ser null si no se ha configurado
//            }
//
//
//
//
//
//            val pendingIntent = PendingIntent.getBroadcast(
//                context,
//                tarea.id.hashCode(),
//                intent,
//                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//            )
//
//            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (!alarmManager.canScheduleExactAlarms()) {
//                    Log.w("AlarmUtils", "⚠️ No tiene permiso para alarmas exactas. Usando set() como alternativa.")
//                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent)
//
//                    // Redirige al usuario para habilitar alarmas exactas
//                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
//                        data = "package:${context.packageName}".toUri()
//                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    }
//                    context.startActivity(settingsIntent)
//
//                    return
//                }
//            }
//
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent)
//            Log.d("AlarmUtils", "✅ Alarma programada para $zonedDateTime")
//        } catch (e: Exception) {
//            Log.e("AlarmUtils", "❌ Error al programar la alarma: ${e.message}")
//        }
//    }
//}