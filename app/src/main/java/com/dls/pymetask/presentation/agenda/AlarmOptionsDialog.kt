package com.dls.pymetask.presentation.agenda

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import com.dls.pymetask.presentation.agenda.PreferencesHelper.saveToneUri

/**
 * Diálogo para configurar opciones de alarma:
 * - Selección de tono (dispara listener en host)
 * - Selección de adelanto (5, 10, 30 min antes)
 */
@Suppress("DEPRECATION")
@Composable
fun AlarmOptionsDialog(
    initialToneUri: String?,
    initialLeadMinutes: Int,
    onDismiss: () -> Unit,
    onPickRingtone: () -> Unit,
    onLeadTimeChange: (Int) -> Unit,
) {
    var selectedLead by remember { mutableIntStateOf(initialLeadMinutes) }
    var toneUri by remember { mutableStateOf(initialToneUri) }
    val context = LocalContext.current


    // Launcher para el selector de tonos
    val pickRingtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: Uri? = result.data
                ?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                saveToneUri(context, it.toString())
                toneUri = it.toString()
            }
        }
    }



    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Opciones de Alarma") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tono actual: ${toneUri ?: "por defecto"}")
                ClickableText(
                    text = AnnotatedString("Cambiar tono de alarma"),
                    onClick = {
                        // Abrir selector de tonos del sistema
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Selecciona tono de alarma")
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                toneUri?.toUri()
                            )
                        }
                        pickRingtoneLauncher.launch(intent)
                    }
                )
                Text("Adelanto de aviso:")
                listOf(5, 10, 30).forEach { minutes ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLead = minutes; onLeadTimeChange(minutes) }
                        .padding(4.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        RadioButton(
                            selected = (selectedLead == minutes),
                            onClick = { selectedLead = minutes; onLeadTimeChange(minutes) }
                        )
                        Text("$minutes minutos antes", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Aceptar")
            }
        }
    )
}

/**
 * Helper para guardar/retriever preferencias de tono y adelanto.
 */
object PreferencesHelper {
    private const val PREFS = "alarm_prefs"
    private const val KEY_TONE = "alarm_tone_uri"
    private const val KEY_LEAD = "alarm_lead_minutes"

    fun saveToneUri(context: Context, uriString: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putString(KEY_TONE, uriString) }
    }

    fun getToneUri(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TONE, null)

    fun saveLeadMinutes(context: Context, minutes: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putInt(KEY_LEAD, minutes) }
    }

    fun getLeadMinutes(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_LEAD, 5)
}

/**
 * Integración en AgendaScreen (simplificada):
 *
 * var showDialog by remember { mutableStateOf(false) }
 * if (showDialog) AlarmOptionsDialog(...)
 * TopAppBar(actions = {
 *   IconButton(onClick = { showDialog = true }) { Icon(Icons.Default.Settings, null) }
 * })
 */
