package com.dls.pymetask.main


import android.content.Context
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    // Inyectamos utilidades de alarmas con Hilt
    @Inject lateinit var alarmUtils: AlarmUtils

    // Guardamos el taskId que llegó desde la notificación para desactivar la alarma en la capa UI si hace falta
    private var taskIdParaDesactivar: String? = null

    override fun attachBaseContext(newBase: Context) {
        // ✅ Crea LanguagePreferences con el contexto, sin Hilt
        val localPrefs = com.dls.pymetask.data.preferences.LanguagePreferences(newBase.applicationContext)

        // Lee el idioma de DataStore de forma bloqueante SOLO aquí (arranque)
        val lang = runBlocking {
            localPrefs.languageFlow.firstOrNull() ?: "es"
        }

        // Aplica el locale
        val localized = com.dls.pymetask.utils.LocaleManager.setLocale(newBase, lang)
        super.attachBaseContext(localized)
    }

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

