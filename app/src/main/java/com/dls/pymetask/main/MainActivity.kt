package com.dls.pymetask.main

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.dls.pymetask.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Lanzador para pedir permiso de notificaciones (Android 13+)
    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("MainActivity", "Permiso POST_NOTIFICATIONS = $granted")
        }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 0) Si Android 13+, pedir permiso de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 1) Crear canal de notificaciones (con sonido + vibración)
        NotificationHelper.createNotificationChannel(this)

        // 2) Si venimos de la notificación de alarma, detenemos el sonido
        if (intent?.action == "com.dls.pymetask.STOP_ALARM") {
            Log.d("MainActivity", "🛑 Deteniendo sonido de alarma")
            NotificationHelper.stopAlarmSound()
        }

        // 3) Recuperar taskId (si abrimos desde la alarma)
        val taskIdFromAlarm = intent.getStringExtra("taskId")


        setContent {
            PymeTaskAppRoot(taskIdInicial = taskIdFromAlarm)
        }
    }
}


