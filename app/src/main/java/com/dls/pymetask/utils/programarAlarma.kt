package com.dls.pymetask.utils

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.dls.pymetask.presentation.agenda.AlarmReceiver
import com.dls.pymetask.domain.model.Tarea
import java.text.SimpleDateFormat
import java.util.*

//object AlarmUtils {
//
//    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
//    fun programarAlarma(context: Context, tarea: Tarea) {
//        if (tarea.fecha.isBlank() || tarea.hora.isBlank()) return
//
//        val format = SimpleDateFormat("dd 'de' MMMM 'de' yyyy HH:mm", Locale("es", "ES"))
//        val fechaHora = "${tarea.fecha} ${tarea.hora}"
//        val date = format.parse(fechaHora) ?: return
//
//        val intent = Intent(context, AlarmReceiver::class.java).apply {
//            putExtra("titulo", tarea.titulo)
//        }
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            tarea.id.hashCode(), // id única por tarea
//            intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
////        alarmManager.setExactAndAllowWhileIdle(
////            AlarmManager.RTC_WAKEUP,
////            date.time,
////            pendingIntent
////        )
//    }
//}
