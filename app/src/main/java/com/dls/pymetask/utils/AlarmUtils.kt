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
    /** PendingIntent único por tarea (requestCode = hash del id) → evita duplicados. */
    private fun buildPendingIntent(taskId: String, title: String, toneUri: String?): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("titulo", title)               // usar SIEMPRE "titulo"
            putExtra("taskId", taskId)
            putExtra("alarmToneUri", toneUri)       // puede ser null → por defecto
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    /** ¿Existe una alarma activa para esta tarea? (NO crea si no existe). */
    fun existeAlarma(taskId: String): Boolean {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, taskId.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        return pi != null
    }
    /** Cancela la alarma de una tarea si existe. */
    fun cancelarAlarma(taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, taskId.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
            Log.d("AlarmUtils", "🗑️ Alarma cancelada para $taskId")
        }
    }
    /** Reprograma de forma idempotente (cancela + programa). */
    @RequiresApi(Build.VERSION_CODES.O)
    fun reprogramarAlarma(tarea: Tarea) {
        cancelarAlarma(tarea.id)
        programarAlarma(tarea)
    }
    /**
     * Programa una alarma exacta:
     * - Parsea fecha/hora ("yyyy-MM-dd", "HH:mm")
     * - Resta minutos de aviso guardados (PreferencesHelper)
     * - Cancela la previa (evita duplicados)
     * - Pasa el alarmToneUri al Receiver
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ScheduleExactAlarm")
    fun programarAlarma(tarea: Tarea) {
        if (tarea.fecha.isBlank() || tarea.hora.isBlank()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("taskId", tarea.id)
            putExtra("taskTitle", tarea.titulo)
        }


        try {
            // 1) Parseo de fecha/hora
            val fFecha = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val fHora  = DateTimeFormatter.ofPattern("HH:mm")
            val date = LocalDate.parse(tarea.fecha, fFecha)
            val time = LocalTime.parse(tarea.hora,  fHora)

            // 2) Restar lead time (minutos antes)
            val lead = PreferencesHelper.getLeadMinutes(context).coerceAtLeast(0)
            val zdt  = ZonedDateTime.of(date, time, ZoneId.systemDefault())
                .minusMinutes(lead.toLong())

            // 3) Evitar programar en pasado por ediciones/lead grande
            val now = System.currentTimeMillis()
            val whenMillis = max(zdt.toInstant().toEpochMilli(), now + 1_000L)

            // 4) Cancelar posible previa de esta misma tarea
            cancelarAlarma(tarea.id)

            // 5) PendingIntent consistente + tono elegido
            val pi = buildPendingIntent(
                taskId = tarea.id,
                title  = tarea.titulo,
                toneUri = PreferencesHelper.getToneUri(context) // null → por defecto
            )

            // 6) setExact… (y pedir permiso en 12+)
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.set(AlarmManager.RTC_WAKEUP, whenMillis, pi)
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
                return
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMillis, pi)
            Log.d("AlarmUtils", "✅ Alarma ${tarea.id} @ $zdt (lead=$lead)")
        } catch (e: Exception) {
            Log.e("AlarmUtils", "❌ Error al programar: ${e.message}")
        }
    }
}
