package com.dls.pymetask.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.dls.pymetask.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.dls.pymetask.utils.AlarmUtils
import jakarta.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() { @Inject lateinit var alarmUtils: AlarmUtils   // para cancelar PendingIntent

    private var taskIdInicial: String? = null

    private var taskIdParaDesactivar: String? = null

    // Lanzador para pedir permiso de notificaciones (Android 13+)
    private val notifPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "Permiso POST_NOTIFICATIONS = $granted")
        }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.ensureSilentChannel(this)
        handleIntent(intent)

//        // 0) Si Android 13+, pedir permiso de notificaciones
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(
//                    this, Manifest.permission.POST_NOTIFICATIONS
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }
//
//        // 1) Crear canal de notificaciones (con sonido + vibración)
//        NotificationHelper.ensureSilentChannel(this)
//
//        // 2) Si venimos de la notificación de alarma, detenemos el sonido
//        if (intent?.action == "com.dls.pymetask.STOP_ALARM") {
//            Log.d("MainActivity", "🛑 Deteniendo sonido de alarma")
//            NotificationHelper.stopAlarmSound()
//            NotificationManagerCompat.from(this).cancel(1)
//        } else {
//            handleIntent(intent)
//        }
//        // 3) Recuperar taskId (si abrimos desde la alarma)
//        val taskIdFromAlarm = intent.getStringExtra("taskId")

        setContent {
            // Pasamos el taskId a la raíz para que Agenda lo consuma

            PymeTaskAppRoot(taskIdInicial = taskIdParaDesactivar)
        }
    }

    /**
     * Maneja acciones que puedan llegar a esta Activity mientras está viva.
     * Si ya estaba abierta y vuelve a lanzarse con la misma instancia (singleTop),
     * la acción puede llegar por onNewIntent.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            "com.dls.pymetask.OPEN_TASK" -> {
                // 1) Detener audio y cerrar notificación visibles ahora
                NotificationHelper.stopAlarmSound()               // ✅ parar sonido
                NotificationHelper.cancelActiveAlarmNotification(this)
                //taskIdInicial = intent.getStringExtra("taskId")   // guardar para navegar

                // 2) Cancelar la ALARMA programada (desactivar técnicamente)
                val tid = intent.getStringExtra("taskId")
                if (!tid.isNullOrBlank()) {
                    alarmUtils.cancelarAlarma(tid)
                    taskIdParaDesactivar = tid   // lo usará Agenda para poner activarAlarma=false en BD
                }
            }
           // "com.dls.pymetask.STOP_ALARM" -> { NotificationHelper.stopAlarmSound() }
        }
    }

    /**
     * Detiene el sonido de la alarma si la acción recibida es STOP_ALARM.
     * (La notificación se crea sin sonido; el tono lo reproducimos nosotros.)
     */



}


