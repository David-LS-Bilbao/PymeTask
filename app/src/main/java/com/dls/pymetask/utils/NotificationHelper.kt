
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
import com.dls.pymetask.R
import com.dls.pymetask.main.MainActivity

/**
 * Helper para:
 *  - Crear canal de notificación SIN sonido (lo reproducimos manualmente)
 *  - Reproducir/detener el tono elegido por el usuario
 *  - Mostrar la notificación de la alarma
 */
object NotificationHelper {
    private const val CHANNEL_ID = "tarea_recordatorio"
    private var ringtone: Ringtone? = null

    /** Crear canal sin sonido (evita "doble audio" canal + Ringtone). */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Tareas"
            val descriptionText = "Canal para alarmas de tareas (sin sonido; el tono lo controla la app)"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // 🔇 Sin sonido en el canal: el audio lo reproduce playAlarmSound()
                setSound(null, null)
                enableLights(true)
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Reproduce el tono seleccionado por el usuario. Si toneUriString == null,
     * utiliza el tono de alarma por defecto del sistema.
     */
    fun playAlarmSound(context: Context, toneUriString: String?): Ringtone? {
        // Elegimos el tono: preferimos el URI guardado (si llega), si no, el por defecto
        val chosenUri: Uri = toneUriString?.let { Uri.parse(it) }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Detenemos el posible tono anterior
        stopAlarmSound()

        // Creamos y arrancamos el Ringtone
        return try {
            ringtone = RingtoneManager.getRingtone(context, chosenUri)?.apply {
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
            // Fallback: intenta con el tono por defecto si el elegido falla
            ringtone = RingtoneManager.getRingtone(
                context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )?.apply { play() }
            ringtone
        }
    }

    /** Detiene el sonido si está activo y libera recursos. */
    fun stopAlarmSound() {
        try {
            ringtone?.stop()
        } finally {
            ringtone = null
        }
    }

    /**
     * Muestra una notificación "silenciosa" (sin sonido de canal).
     * El sonido ya lo estamos reproduciendo con playAlarmSound().
     */
    fun showAlarmNotification(context: Context, title: String, message: String) {
        // Aseguramos canal
        createNotificationChannel(context)

        // Intent para abrir la app y, si quieres, detener la alarma (maneja la action en MainActivity)
        val stopIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.dls.pymetask.STOP_ALARM"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            // 🔇 No llamamos a setSound(): canal sin sonido + sonido manual con Ringtone
            .setContentIntent(pi)
            .build()

        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(1, notif)
    }
}





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







