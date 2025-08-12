package com.dls.pymetask.main
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.dls.pymetask.utils.AlarmUtils
import com.dls.pymetask.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() { @Inject lateinit var alarmUtils: AlarmUtils   // para cancelar PendingIntent
    private var taskIdParaDesactivar: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        NotificationHelper.ensureSilentChannel(this)
        handleIntent(intent)
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

                // 2) Cancelar la ALARMA programada (desactivar técnicamente)
                val tid = intent.getStringExtra("taskId")
                if (!tid.isNullOrBlank()) {
                    alarmUtils.cancelarAlarma(tid)
                    taskIdParaDesactivar = tid   // lo usará Agenda para poner activarAlarma=false en BD
                }
            }
        }
    }
}


