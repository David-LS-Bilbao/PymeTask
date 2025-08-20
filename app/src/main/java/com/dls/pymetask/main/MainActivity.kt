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
class MainActivity : ComponentActivity() {

    // Inyectamos utilidades de alarmas con Hilt
    @Inject lateinit var alarmUtils: AlarmUtils

    // Guardamos el taskId que llegó desde la notificación para desactivar la alarma en la capa UI si hace falta
    private var taskIdParaDesactivar: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activa dibujado edge-to-edge
        enableEdgeToEdge()

        // Asegura el canal de notificaciones en silencio para alarmas
        NotificationHelper.ensureSilentChannel(this)

        // Procesa el intent que abrió la Activity (por ejemplo desde notificación)
        handleIntent(intent)

        // Carga el árbol de Compose. El NavController se crea DENTRO de Compose.
        setContent {
            PymeTaskAppRoot()
        }
    }

    /**
     * Se invoca cuando la Activity ya abierta recibe un nuevo Intent (singleTop).
     * Aquí gestionamos acciones disparadas desde notificaciones.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Maneja la acción de abrir una tarea desde notificación:
     * - Detiene sonido y cierra notificación
     * - Cancela la alarma programada (desactiva técnicamente)
     */
    private fun handleIntent(intent: Intent?) {
        val action = intent?.action ?: return
        if (action == "com.dls.pymetask.OPEN_TASK") {
            // 1) Parar sonido y notificación activa
            NotificationHelper.stopAlarmSound()
            NotificationHelper.cancelActiveAlarmNotification(this)

            // 2) Cancelar la alarma con el mismo requestCode (taskId)
            val tid = intent.getStringExtra("taskId")
            if (!tid.isNullOrBlank()) {
                alarmUtils.cancelarAlarma(tid)
                taskIdParaDesactivar = tid
            }
        }
    }
}




















//import android.content.Intent
//import android.os.Build
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.annotation.RequiresApi
//import androidx.navigation.NavController
//import com.dls.pymetask.utils.AlarmUtils
//import com.dls.pymetask.utils.NotificationHelper
//import dagger.hilt.android.AndroidEntryPoint
//import jakarta.inject.Inject
//
//
//
//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//    @Inject lateinit var alarmUtils: AlarmUtils
////    @Inject lateinit var oauthManager: OAuthManager   // <-- inyección Hilt
//    private val navController: NavController = NavController(this)
//
//    // para cancelar PendingIntent
//    private var taskIdParaDesactivar: String? = null
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        enableEdgeToEdge()
//        NotificationHelper.ensureSilentChannel(this)
//        handleIntent(intent)
//        setContent {
//            PymeTaskAppRoot(navController = navController)
//        }
//    }
//    /**
//     * Maneja acciones que puedan llegar a esta Activity mientras está viva.
//     * Si ya estaba abierta y vuelve a lanzarse con la misma instancia (singleTop),
//     * la acción puede llegar por onNewIntent.
//     */
//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        handleIntent(intent)
//    }
//
//    private fun handleIntent(intent: Intent?) {
//        when (intent?.action) {
//            "com.dls.pymetask.OPEN_TASK" -> {
//                // 1) Detener audio y cerrar notificación visibles ahora
//                NotificationHelper.stopAlarmSound()               // ✅ parar sonido
//                NotificationHelper.cancelActiveAlarmNotification(this)
//
//                // 2) Cancelar la ALARMA programada (desactivar técnicamente)
//                val tid = intent.getStringExtra("taskId")
//                if (!tid.isNullOrBlank()) {
//                    alarmUtils.cancelarAlarma(tid)
//                    taskIdParaDesactivar = tid   // lo usará Agenda para poner activarAlarma=false en BD
//                }
//            }
//        }
//    }
//}


