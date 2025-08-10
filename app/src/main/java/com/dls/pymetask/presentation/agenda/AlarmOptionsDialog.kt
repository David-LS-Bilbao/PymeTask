
package com.dls.pymetask.presentation.agenda
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri

@Composable
fun AlarmOptionsDialog(
    initialToneUri: String?,
    initialLeadMinutes: Int,
    onDismiss: () -> Unit,
    onPickRingtone: () -> Unit,
    onLeadTimeChange: (Int) -> Unit,
) {
    var toneUri by rememberSaveable { mutableStateOf(initialToneUri) }
    var selectedLead by rememberSaveable { mutableIntStateOf(initialLeadMinutes) }
    val context = LocalContext.current

    val pickRingtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            uri?.let {
                PreferencesHelper.saveToneUri(context, it.toString())
                toneUri = it.toString()
            }
        }
    }

    val toneTitle by remember(toneUri) {
        derivedStateOf {
            runCatching {
                toneUri?.toUri()?.let { RingtoneManager.getRingtone(context, it)?.getTitle(context) }
                    ?: "Por defecto"
            }.getOrNull() ?: "Por defecto"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Opciones de alarma") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp)
            ) {
                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsActive,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("Tono de alarma") },
                        supportingContent = {
                            Text(
                                toneTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        trailingContent = {
                            TextButton(
                                onClick = {
                                    onPickRingtone()
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(
                                            RingtoneManager.EXTRA_RINGTONE_TYPE,
                                            RingtoneManager.TYPE_ALARM
                                        )
                                        putExtra(
                                            RingtoneManager.EXTRA_RINGTONE_TITLE,
                                            "Selecciona tono de alarma"
                                        )
                                        putExtra(
                                            RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                            toneUri?.toUri()
                                        )
                                    }
                                    pickRingtoneLauncher.launch(intent)
                                },
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text("Cambiar")
                            }
                        }
                    )
                }

                item {
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }

                item {
                    ListItem(
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text("Aviso previo") },
                        supportingContent = {
                            Text(
                                if (selectedLead == 0) "A la hora exacta"
                                else "$selectedLead minutos antes"
                            )
                        }
                    )
                }

                // Lista de opciones de minutos con RadioButtons
                val opcionesMinutos = listOf(0, 5, 15, 30, 60)

                items(opcionesMinutos.size) { index ->
                    val minutes = opcionesMinutos[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                role = Role.RadioButton,
                                onClick = {
                                    selectedLead = minutes
                                    onLeadTimeChange(minutes)
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedLead == minutes,
                            onClick = {
                                selectedLead = minutes
                                onLeadTimeChange(minutes)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (minutes == 0) "Exacto" else "$minutes min")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Aceptar") }
        }
    )
}

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
            .edit { putInt(KEY_LEAD, minutes.coerceAtLeast(0)) }
    }

    fun getLeadMinutes(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_LEAD, 5)
}
