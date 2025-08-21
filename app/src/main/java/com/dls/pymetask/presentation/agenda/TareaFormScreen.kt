

package com.dls.pymetask.presentation.agenda

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <-- i18n en Compose
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.ui.theme.Poppins
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("NewApi", "DefaultLocale", "LocalContextConfigurationRead", "ObsoleteSdkInt")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaFormScreen(
    taskId: String? = null,
    navController: NavController,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    // Contexto para dialogs/toasts y para acceder a recursos
    val context = LocalContext.current

    // Estado de carga del ViewModel
    val isLoading by viewModel.loading.collectAsState()
    // Tarea seleccionada (si la hay)
    val tareaActual by rememberUpdatedState(newValue = viewModel.tareaActual)

    // Formateador de fecha (guardamos en ISO "yyyy-MM-dd" para coherencia con el resto de la app)
    // Nota: usamos Locale del sistema por si en el futuro decides mostrar nombres de mes.
    val dateFormatter = remember {
        val sysLocale = context.resources.configuration.let {
            if (android.os.Build.VERSION.SDK_INT >= 24) it.locales[0] else @Suppress("DEPRECATION") it.locale
        }
        DateTimeFormatter.ofPattern("yyyy-MM-dd", sysLocale)
    }

    // ------------------ Estados locales del formulario ------------------
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var descripcionLarga by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var completado by remember { mutableStateOf(false) }
    var activarAlarma by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Carga inicial de la tarea o limpieza de formulario según el argumento
    LaunchedEffect(taskId) {
        if (taskId != null) viewModel.seleccionarTarea(taskId) else viewModel.limpiarTareaActual()
    }

    // Copia datos de la tarea en los campos cuando llegue del VM
    LaunchedEffect(tareaActual) {
        tareaActual?.let { t ->
            titulo = t.titulo
            descripcion = t.descripcion
            descripcionLarga = t.descripcionLarga
            fecha = t.fecha
            hora = t.hora
            completado = t.completado
            activarAlarma = t.activarAlarma
        }
    }

    // Manejo del botón atrás del sistema: guardar y salir
    BackHandler {
        saveAndExit(
            context, navController, viewModel,
            taskId, titulo, descripcion, descripcionLarga,
            fecha, hora, completado, activarAlarma
        )
    }

    // ========================= UI =========================
    Scaffold(
        topBar = {
            TopAppBar(
                // Título localizado de la pantalla
                title = { Text(text = stringResource(R.string.task_title_bar), fontFamily = Poppins) },
                // Botón "volver" con contentDescription localizado
                navigationIcon = {
                    IconButton(
                        onClick = {
                            saveAndExit(
                                context, navController, viewModel,
                                taskId, titulo, descripcion, descripcionLarga,
                                fecha, hora, completado, activarAlarma
                            )
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    // Guardar
                    IconButton(
                        onClick = {
                            saveAndExit(
                                context, navController, viewModel,
                                taskId, titulo, descripcion, descripcionLarga,
                                fecha, hora, completado, activarAlarma
                            )
                        }
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = stringResource(R.string.common_save)
                        )
                    }
                    // Borrar
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.common_delete)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            // Indicador de carga centrado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            // Formulario
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo: Título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text(stringResource(R.string.task_field_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Campo: Resumen breve
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text(stringResource(R.string.task_field_summary)) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Campo: Fecha (readonly) - abre DatePicker al pulsar el contenedor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val now = Calendar.getInstance()
                            // Si ya hay fecha, inicializa el picker con ella
                            val init = runCatching { LocalDate.parse(fecha, dateFormatter) }.getOrNull()
                            val year = init?.year ?: now.get(Calendar.YEAR)
                            val month = (init?.monthValue ?: (now.get(Calendar.MONTH) + 1)) - 1
                            val day = init?.dayOfMonth ?: now.get(Calendar.DAY_OF_MONTH)
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val selected = LocalDate.of(y, m + 1, d)
                                    val newDate = selected.format(dateFormatter)
                                    fecha = newDate
                                    viewModel.actualizarFecha(newDate)
                                },
                                year, month, day
                            ).show()
                        }
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.task_field_date)) },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Campo: Hora (readonly) - abre TimePicker al pulsar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val nowH = Calendar.getInstance()
                            val (initH, initM) = hora.split(":").let {
                                val h = it.getOrNull(0)?.toIntOrNull() ?: nowH.get(Calendar.HOUR_OF_DAY)
                                val m = it.getOrNull(1)?.toIntOrNull() ?: nowH.get(Calendar.MINUTE)
                                h to m
                            }
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    val newTime = String.format("%02d:%02d", h, min)
                                    hora = newTime
                                    viewModel.actualizarHora(newTime)
                                },
                                initH, initM, true
                            ).show()
                        }
                ) {
                    OutlinedTextField(
                        value = hora,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.task_field_time)) },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Campo: Descripción detallada
                OutlinedTextField(
                    value = descripcionLarga,
                    onValueChange = { descripcionLarga = it },
                    label = { Text(stringResource(R.string.task_field_long_desc)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )

                // Fila: Completada + Activar alarma
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = completado,
                            onCheckedChange = { completado = it }
                        )
                        Text(stringResource(R.string.task_completed))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.task_enable_alarm))
                        Switch(
                            checked = activarAlarma,
                            onCheckedChange = { activarAlarma = it }
                        )
                    }
                }
            }
        }
        // Diálogo de confirmación de borrado (localizado)
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.task_confirm_delete_title)) },
                text = { Text(stringResource(R.string.task_confirm_delete_text)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            tareaActual?.id?.let {
                                viewModel.eliminarTareaPorId(it)
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.task_toast_deleted),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            }
                        }
                    ) { Text(stringResource(R.string.common_delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }
    }
}
/**
 * Guarda la tarea (validando campos mínimos) y navega atrás.
 * Mantenemos esta función fuera del composable para reutilizarla desde back y botones.
 */
@SuppressLint("NewApi")
private fun saveAndExit(
    context: Context,
    navController: NavController,
    viewModel: AgendaViewModel,
    taskId: String?,
    titulo: String,
    descripcion: String,
    descripcionLarga: String,
    fecha: String,
    hora: String,
    completado: Boolean,
    activarAlarma: Boolean
) {
    // Validaciones localizadas
    if (titulo.isBlank() && fecha.isBlank() && hora.isBlank()){
        viewModel.limpiarTareaActual()
        navController.popBackStack()
        return
    }else {
        when {
            titulo.isBlank() -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.task_toast_title_required),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            fecha.isBlank() -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.task_toast_date_required),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            hora.isBlank() -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.task_toast_time_required),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
    }

    // Persistencia en el ViewModel
    viewModel.guardarTarea(
        Tarea(
            id = taskId ?: UUID.randomUUID().toString(),
            titulo = titulo,
            descripcion = descripcion,
            descripcionLarga = descripcionLarga,
            fecha = fecha,
            hora = hora,
            completado = completado,
            activarAlarma = activarAlarma
        )
    )
    // Aviso y navegación atrás
    Toast.makeText(context, context.getString(R.string.task_toast_saved), Toast.LENGTH_SHORT).show()
    navController.popBackStack()
}


