// NotificationHelper.kt
package com.dls.pymetask.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dls.pymetask.R
import com.dls.pymetask.main.MainActivity

/**
 * NOTA IMPORTANTE SOBRE EL DOBLE SONIDO:
 * - Si alguna vez se creó un CHANNEL con sonido, Android mantiene esa config aunque cambies el código.
 * - Solución: usar un NUEVO channelId sin sonido y, opcionalmente, borrar el viejo.
 */

// ➜ NUEVO canal silencioso para evitar "doble sonido"
private const val CHANNEL_ID_SILENT = "tarea_recordatorio_silent_v2"

// ➜ Canales antiguos que pudieron crearse con sonido (ajusta si usaste otros)
private val OLD_CHANNEL_IDS = arrayOf("tarea_recordatorio")

object NotificationHelper {
    private var ringtone: Ringtone? = null

    /**
     * Borra canales antiguos (si existen) y crea el canal NUEVO sin sonido.
     * Llama a esto al inicio de la app (Application.onCreate) o antes de notificar.
     */
    fun ensureSilentChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1) Borrar canales antiguos que podrían tener sonido configurado
            OLD_CHANNEL_IDS.forEach { oldId ->
                runCatching { nm.deleteNotificationChannel(oldId) }
            }

            // 2) Crear canal silencioso
            val name = "Recordatorios de Tareas (silencioso)"
            val descriptionText = "Canal sin sonido; el tono lo reproduce la app."
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID_SILENT, name, importance).apply {
                description = descriptionText
                // 🔇 Canal SIN sonido (el audio lo manejamos nosotros con Ringtone)
                setSound(null, null)
                enableLights(true)
                enableVibration(true)
            }
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Reproduce el tono seleccionado (o el de alarma por defecto si null).
     * Usamos Ringtone (simple y fiable para alarmas) en vez de MediaPlayer.
     */
    fun playAlarmSound(context: Context, toneUriString: String?): Ringtone? {
        stopAlarmSound() // por si ya había uno sonando

        val chosen: Uri = toneUriString?.let { Uri.parse(it) }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        return try {
            ringtone = RingtoneManager.getRingtone(context, chosen)?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                play()
            }
            ringtone
        } catch (_: Exception) {
            // Fallback: tono por defecto si el elegido falla
            ringtone = RingtoneManager.getRingtone(
                context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )?.apply { play() }
            ringtone
        }
    }

    /** Detiene el sonido si está activo y libera referencia. */
    fun stopAlarmSound() {
        try { ringtone?.stop() } finally { ringtone = null }
    }

    /**
     * Notificación SILENCIOSA (sin setSound ni DEFAULT_ALL).
     * El sonido ya lo reproduce playAlarmSound().
     */
    fun showAlarmNotification(context: Context, title: String, message: String) {
        ensureSilentChannel(context) // asegurar canal silencioso creado

        val stopIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.dls.pymetask.STOP_ALARM"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID_SILENT)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)     // evita re-alertas si actualizas
            .setSilent(true)            // 🔕 extra: pide notificación silenciosa
            // NO setSound, NO defaults con sonido:
            // .setDefaults(NotificationCompat.DEFAULT_ALL) ← ¡NO!
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(1, notif)
    }

    fun cancelActiveAlarmNotification(context: Context) {
        // Si usas siempre el mismo ID (1), con esto basta.
        NotificationManagerCompat.from(context).cancel(1)
    }
}


//package com.dls.pymetask.utils
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.media.Ringtone
//import android.media.RingtoneManager
//import android.media.AudioAttributes
//import android.net.Uri
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.dls.pymetask.R
//import com.dls.pymetask.main.MainActivity
//
///**
// * - Canal SIN sonido (el tono lo reproducimos manualmente).
// * - Reproduce/detiene el tono elegido por el usuario.
// */
//object NotificationHelper {
//    private const val CHANNEL_ID = "tarea_recordatorio"
//    private var ringtone: Ringtone? = null
//
//    fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                "Recordatorios de Tareas",
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "Canal para alarmas de tareas (sin sonido; lo controla la app)"
//                setSound(null, null)            // 🔇 sin sonido en el canal
//                enableLights(true)
//                enableVibration(true)
//            }
//            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
//                .createNotificationChannel(channel)
//        }
//    }
//
//    /** Reproduce el tono seleccionado (o el de alarma por defecto si null). */
//    fun playAlarmSound(context: Context, toneUriString: String?): Ringtone? {
//        stopAlarmSound()
//        val chosen: Uri = toneUriString?.let { Uri.parse(it) }
//            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//
//        return try {
//            ringtone = RingtoneManager.getRingtone(context, chosen)?.apply {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    audioAttributes = AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_ALARM)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                        .build()
//                }
//                play()
//            }
//            ringtone
//        } catch (_: Exception) {
//            ringtone = RingtoneManager.getRingtone(
//                context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//            )?.apply { play() }
//            ringtone
//        }
//    }
//
//    fun stopAlarmSound() {
//        try { ringtone?.stop() } finally { ringtone = null }
//    }
//
//    /** Notificación silenciosa (el sonido ya está sonando con Ringtone). */
//    fun showAlarmNotification(context: Context, title: String, message: String) {
//        createNotificationChannel(context)
//
//        val stopIntent = Intent(context, MainActivity::class.java).apply {
//            action = "com.dls.pymetask.STOP_ALARM"
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pi = PendingIntent.getActivity(
//            context, 0, stopIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_alarm)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//            // ❌ NO setSound: evitamos doble audio
//            .setContentIntent(pi)
//            .build()
//
//        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
//            .notify(1, notif)
//    }
//}




//// NotificationHelper.kt
//package com.dls.pymetask.utils
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.media.Ringtone
//import android.media.RingtoneManager
//import android.media.AudioAttributes
//import android.net.Uri
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.dls.pymetask.R
//import com.dls.pymetask.main.MainActivity
//
//object NotificationHelper {
//    private const val CHANNEL_ID = "tarea_recordatorio"
//    private var ringtone: Ringtone? = null
//
//    /** Crea el canal (con sonido + vibración). Llamar UNA VEZ al arrancar la app. */
//    fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val name = "Recordatorios de Tareas"
//            val descriptionText = "Canal para alarmas de tareas con sonido"
//            val importance = NotificationManager.IMPORTANCE_HIGH
//
//            val soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//            val audioAttrs = AudioAttributes.Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                .setUsage(AudioAttributes.USAGE_ALARM)
//                .build()
//
//            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
//                description = descriptionText
//                setSound(soundUri, audioAttrs)
//                enableLights(true)
//                enableVibration(true)
//            }
//
//            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            manager.createNotificationChannel(channel)
//        }
//    }
//
//    /** Reproduce el sonido de alarma y guarda la referencia para poder detenerlo. */
//    fun playAlarmSound(context: Context, toneUriString: String?): Ringtone? {
//        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//        ringtone = RingtoneManager.getRingtone(context, alarmUri)
//        ringtone?.play()
//        return ringtone
//    }
//
//    /** Detiene el sonido de alarma si está activo. */
//    fun stopAlarmSound() {
//        ringtone?.stop()
//        ringtone = null
//    }
//
//    /**
//     * Muestra notificación de alarma.
//     * - Al tocarla lanzará STOP_ALARM en MainActivity para detener el sonido.
//     */
//    fun showAlarmNotification(context: Context, title: String, message: String) {
//        // Aseguramos canal
//        createNotificationChannel(context)
//
//        // Intent para detener sonido al abrir la app
//        val stopIntent = Intent(context, MainActivity::class.java).apply {
//            action = "com.dls.pymetask.STOP_ALARM"
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val pi = PendingIntent.getActivity(
//            context, 0, stopIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_alarm)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
//            .setContentIntent(pi)
//            .build()
//
//        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        mgr.notify(1, notif)
//    }
//}







