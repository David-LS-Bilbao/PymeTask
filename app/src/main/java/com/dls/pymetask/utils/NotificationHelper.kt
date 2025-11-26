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
import com.dls.pymetask.presentation.agenda.DismissAlarmReceiver

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
    fun showAlarmNotification(
        context: Context,
        title: String,
        taskId: String?,
        message: String) {
        ensureSilentChannel(context) // asegurar canal silencioso creado

        // Al tocar la notificación, abrimos la tarea específica
        val openIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.dls.pymetask.OPEN_TASK"
            putExtra("taskId", taskId)
            putExtra("openAgenda", true) // Para navegar a Agenda
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPI = PendingIntent.getActivity(
            context, taskId?.hashCode() ?: 1001, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Botón para desactivar alarma sin abrir la app
        val dismissIntent = Intent(context, DismissAlarmReceiver::class.java).apply {
            action = "com.dls.pymetask.DISMISS_ALARM"
            putExtra("taskId", taskId)
        }
        val dismissPI = PendingIntent.getBroadcast(
            context, (taskId?.hashCode() ?: 0) + 5000, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID_SILENT)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOnlyAlertOnce(true)
            .setSilent(true)      // sin sonido del canal
            .setContentIntent(contentPI) // <- abre la tarea
            .addAction(
                R.drawable.ic_alarm,
                "Desactivar alarma",
                dismissPI
            )
            .setAutoCancel(true)
            .build()

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(taskId?.hashCode() ?: 1, notif)
    }
    fun cancelActiveAlarmNotification(context: Context, taskId: String? = null) {
        val notificationId = taskId?.hashCode() ?: 1
        NotificationManagerCompat.from(context).cancel(notificationId)
        stopAlarmSound() // También detener el sonido si está activo
    }

}







