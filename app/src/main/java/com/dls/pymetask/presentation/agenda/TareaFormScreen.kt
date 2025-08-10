

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.ui.theme.Poppins
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("NewApi", "DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaFormScreen(
    taskId: String? = null,
    navController: NavController,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    // Contexto para dialogs y toasts
    val context = LocalContext.current
    // Estados expuestos por el ViewModel
    val isLoading by viewModel.loading.collectAsState()
    val tareaActual by rememberUpdatedState(newValue = viewModel.tareaActual)
    // Formateador y calendario base
    val calendar = Calendar.getInstance()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale("es", "ES"))

    // Estados locales del formulario
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var descripcionLarga by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var completado by remember { mutableStateOf(false) }
    var activarAlarma by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Carga inicial de la tarea o limpieza
    LaunchedEffect(taskId) {
        if (taskId != null) viewModel.seleccionarTarea(taskId) else viewModel.limpiarTareaActual()
    }

    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            val selected = LocalDate.of(y, m + 1, d)
            fecha = selected.format(dateFormatter)
            viewModel.actualizarFecha(fecha)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    val timePicker = TimePickerDialog(
        context,
        { _, h, min ->
            hora = String.format("%02d:%02d", h, min)
            viewModel.actualizarHora(hora)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    // Copiar datos de la tarea seleccionada al formulario
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

    // Acción común de guardar y salir
    BackHandler {
        saveAndExit(
            context, navController, viewModel,
            taskId, titulo, descripcion, descripcionLarga,
            fecha, hora, completado, activarAlarma
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Tarea", fontFamily = Poppins) },
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            saveAndExit(
                                context, navController, viewModel,
                                taskId, titulo, descripcion, descripcionLarga,
                                fecha, hora, completado, activarAlarma
                            )
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Titulo
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Resumen breve") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Fecha y hora


                // Fecha: usamos un contenedor clickable y deshabilitamos el TextField
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val now = Calendar.getInstance()
                            // Si ya hay fecha, inicializamos el picker con ella
                            val init = runCatching { LocalDate.parse(fecha, dateFormatter) }.getOrNull()
                            val year = init?.year ?: now.get(Calendar.YEAR)
                            val month = (init?.monthValue ?: (now.get(Calendar.MONTH) + 1)) - 1
                            val day = init?.dayOfMonth ?: now.get(Calendar.DAY_OF_MONTH)
                            DatePickerDialog(context, { _, y, m, d ->
                                val selected = LocalDate.of(y, m + 1, d)
                                val newDate = selected.format(dateFormatter)
                                fecha = newDate
                                viewModel.actualizarFecha(newDate)
                            }, year, month, day).show()
                        }
                ) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = {},
                        label = { Text("Fecha") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Hora: mismo patrón
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val nowH = calendar
                            val (initH, initM) = hora.split(":").let {
                                val h = it.getOrNull(0)?.toIntOrNull() ?: nowH.get(Calendar.HOUR_OF_DAY)
                                val m = it.getOrNull(1)?.toIntOrNull() ?: nowH.get(Calendar.MINUTE)
                                h to m
                            }
                            TimePickerDialog(context, { _, h, min ->
                                val newTime = String.format("%02d:%02d", h, min)
                                hora = newTime
                                viewModel.actualizarHora(newTime)
                            }, initH, initM, true).show()
                        }
                ) {
                    OutlinedTextField(
                        value = hora,
                        onValueChange = {},
                        label = { Text("Hora") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Descripción larga
                OutlinedTextField(
                    value = descripcionLarga,
                    onValueChange = { descripcionLarga = it },
                    label = { Text("Descripción detallada") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5
                )
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
                        Text("Completada")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Activar alarma")
                        Switch(
                            checked = activarAlarma,
                            onCheckedChange = { activarAlarma = it }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("¿Estás seguro?") },
                text = { Text("Se eliminará la tarea permanentemente.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            tareaActual?.id?.let {
                                viewModel.eliminarTareaPorId(it)
                                Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        }
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

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
    if (titulo.isBlank()) {
        Toast.makeText(context, "Título obligatorio", Toast.LENGTH_SHORT).show()
        return
    }
    if (fecha.isBlank()) {
        Toast.makeText(context, "Fecha obligatoria", Toast.LENGTH_SHORT).show()
        return
    }
    if (hora.isBlank()) {
        Toast.makeText(context, "Hora obligatoria", Toast.LENGTH_SHORT).show()
        return
    }
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
    Toast.makeText(context, "Tarea guardada", Toast.LENGTH_SHORT).show()
    navController.popBackStack()
}
























//@file:Suppress("DEPRECATION")
//
//package com.dls.pymetask.presentation.agenda
//
//import android.annotation.SuppressLint
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.content.Context
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.compose.BackHandler
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Save
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import com.dls.pymetask.domain.model.Tarea
//import com.dls.pymetask.ui.theme.Poppins
//import java.text.SimpleDateFormat
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//import java.util.*

//@SuppressLint("DefaultLocale", "NewApi")
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TareaFormScreen(
//    taskId: String?=null,
//    navController: NavController,
//    viewModel: AgendaViewModel = hiltViewModel()
//) {
//    val context = LocalContext.current
//    val isLoading by viewModel.loading.collectAsState()
//    val tareaActual = viewModel.tareaActual
//
//    val calendar = Calendar.getInstance()
//    val dateFormatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
//
//    // Datos de la tarea
//    var titulo by remember { mutableStateOf("") }
//    var descripcion by remember { mutableStateOf("") }
//    var descripcionLarga by remember { mutableStateOf("") }
//    var fecha by remember { mutableStateOf("") }
//    var hora by remember { mutableStateOf("") }
//    var completado by remember { mutableStateOf(false) }
//    val activarAlarmaState = remember { mutableStateOf(true) }
//
//    LaunchedEffect(viewModel.tareaActual) {
//        viewModel.tareaActual?.let {
//            activarAlarmaState.value = it.activarAlarma
//        }
//    }
//
//    // Diálogo de confirmación para eliminar
//    var mostrarConfirmacionBorrado by remember { mutableStateOf(false) }
//
//
//    val datePickerDialog = DatePickerDialog(
//        context,
//        { _, year, month, dayOfMonth ->
//            calendar.set(year, month, dayOfMonth)
////            val nuevaFecha = dateFormatter.format(calendar.time)
////            fecha = nuevaFecha                      // ⬅️ actualizar UI
////            viewModel.actualizarFecha(nuevaFecha)   // ⬅️ actualizar ViewModel
//            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
//            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
//            val nuevaFecha = selectedDate.format(formatter)
//
//            fecha = nuevaFecha                      // ⬅️ para el campo
//            viewModel.actualizarFecha(nuevaFecha)  // ⬅️ para el ViewModel
//
//
//        },
//        calendar.get(Calendar.YEAR),
//        calendar.get(Calendar.MONTH),
//        calendar.get(Calendar.DAY_OF_MONTH)
//    )
//
//    val timePickerDialog = TimePickerDialog(
//        context,
//        { _, hourOfDay, minute ->
//            val nuevaHora = String.format("%02d:%02d", hourOfDay, minute)
//            hora = nuevaHora                        // ⬅️ actualizar UI
//            viewModel.actualizarHora(nuevaHora)     // ⬅️ actualizar ViewModel
//        },
//        calendar.get(Calendar.HOUR_OF_DAY),
//        calendar.get(Calendar.MINUTE),
//        true
//    )
//
//
//
//    // Carga de tarea si taskId está presente
//    LaunchedEffect(taskId) {
//        if (taskId != null) {
//            viewModel.seleccionarTarea(taskId)
//        }else{
//            viewModel.limpiarTareaActual()
//        }
//    }
//
//    // copiar los datos de la tarea seleccionada al formulario
//    val tareaCargada = tareaActual != null
//
//    LaunchedEffect(tareaActual) {
//        tareaActual?.let { tarea ->
//            titulo = tarea.titulo
//            descripcion = tarea.descripcion
//            descripcionLarga = tarea.descripcionLarga
//            fecha = tarea.fecha
//            hora = tarea.hora
//            completado = tarea.completado
//            activarAlarmaState.value = tarea.activarAlarma
//            Log.d("TareaForm", "✅ Datos de tarea cargados correctamente")
//        }
//    }
//
//
//        // guardar al pulsar atrás físico
//    BackHandler {
//       guardarYSalirAgenda(
//           context = context,
//           navController = navController,
//           viewModel = viewModel,
//           taskId = taskId,
//           titulo = titulo,
//           descripcion = descripcion,
//           descripcionLarga = descripcionLarga,
//           fecha = fecha,
//           hora = hora,
//           completado = completado,
//           activarAlarma = activarAlarmaState.value
//       )
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Tarea", fontFamily = Poppins) },
//                navigationIcon = {
//                    IconButton(onClick = {
//                        if (titulo.isBlank()) {
//                            Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
//                            return@IconButton
//                        }
//                        // guardamos
//                        guardarYSalirAgenda(
//                            context = context,
//                            navController = navController,
//                            viewModel = viewModel,
//                            taskId = taskId,
//                            titulo = titulo,
//                            descripcion = descripcion,
//                            descripcionLarga = descripcionLarga,
//                            fecha = fecha,
//                            hora = hora,
//                            completado = completado,
//                            activarAlarma = activarAlarmaState.value
//                        )
//
//                    }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
//                    }
//                },
//                actions = {
//                    // Botón guardar
//                    IconButton(onClick = {
//                        if (titulo.isBlank()) {
//                            Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
//                            return@IconButton
//                        }
//                        // guardamos
//                        guardarYSalirAgenda(
//                            context = context,
//                            navController = navController,
//                            viewModel = viewModel,
//                            taskId = taskId,
//                            titulo = titulo,
//                            descripcion = descripcion,
//                            descripcionLarga = descripcionLarga,
//                            fecha = fecha,
//                            hora = hora,
//                            completado = completado,
//                            activarAlarma = activarAlarmaState.value
//                        )
//
//                    }) {
//                        Icon(Icons.Default.Save, contentDescription = "Guardar")
//                    }
//                    // Botón borrar
//                    IconButton(onClick = { mostrarConfirmacionBorrado = true }) {
//                        Icon(Icons.Default.Delete, contentDescription = "Borrar nota")
//                    }
//                }
//            )
//        },
//
//    ) { padding ->
//        if (isLoading) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        } else {
//            Column(
//                modifier = Modifier
//                    .padding(padding)
//                    .padding(16.dp)
//                    .fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                // CAMPO: TÍTULO
//                OutlinedTextField(
//                    value = titulo,
//                    onValueChange = {
//                        if (titulo != it) {
//                            titulo = it
//                        }
//                    },
//                    label = { Text("Título") },
//                    singleLine = true,
//                    modifier = Modifier.fillMaxWidth()
//                )
//                // CAMPO: DESCRIPCIÓN BREVE
//                OutlinedTextField(
//                    value = descripcion,
//                    onValueChange = {
//                        if (descripcion != it) {
//                            descripcion = it
//                        }
//                    },
//                    label = { Text("Resumen breve") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { datePickerDialog.show() }
//                ) {
//                    // CAMPO: FECHA con calendario (formato español)
//                    OutlinedTextField(
//                        value = fecha,
//                        onValueChange = {
//
//                        },
//                        readOnly = true,
//                        label = { Text("Fecha") },
//                        modifier = Modifier.fillMaxWidth(),
//                        enabled = false
//                    )
//                }
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable { timePickerDialog.show() }
//                ) {
//                    // CAMPO: HORA con reloj
//                    OutlinedTextField(
//                        value = hora,
//                        onValueChange = {
//
//                        },
//                        readOnly = true,
//                        label = { Text("Hora") },
//                        modifier = Modifier.fillMaxWidth(),
//                        enabled = false
//                    )
//                }
//                // CAMPO: DESCRIPCIÓN DETALLADA
//                OutlinedTextField(
//                    value = descripcionLarga,
//                    onValueChange = {
//                        if (descripcionLarga != it) {
//                            descripcionLarga = it
//                        }
//                        },
//                    label = { Text("Descripción detallada") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(150.dp),
//                    singleLine = false,
//                    maxLines = 5
//                )
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    // CAMPO: CHECKBOX "Completada"
//                    Checkbox(
//                        checked = completado,
//                        onCheckedChange =  { completado = it }
//
//                    )
//                    Text("Completada")
//
//                    Spacer(modifier = Modifier.weight(1f))
//
//                    Text("Activar alarma  ")
//                // CAMPO: SWITCH "Activar alarma"
//                    Log.d("SwitchEstado", "activarAlarma = $activarAlarmaState")
//
//                    Switch(
//                        checked = activarAlarmaState.value,
//                        onCheckedChange = { activarAlarmaState.value = it }
//                    )
//                }
//            }
//            // Dialogo de confirmación para eliminar
//            if (mostrarConfirmacionBorrado) {
//                AlertDialog(
//                    onDismissRequest = { mostrarConfirmacionBorrado = false },
//                    confirmButton = {
//                        TextButton(onClick = {
//                            val idTarea = taskId ?: viewModel.tareaActual?.id
//                            if (idTarea != null) {
//                                viewModel.eliminarTareaPorId( idTarea)
//                                Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT)
//                                    .show()
//                            } else {
//                                Toast.makeText(
//                                    context,
//                                    "Error al eliminar la tarea",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                            mostrarConfirmacionBorrado = false
//                            navController.popBackStack()
//                        }) {
//                            Text("Eliminar")
//                        }
//                    },
//                        dismissButton = {
//                            TextButton(onClick = { mostrarConfirmacionBorrado = false }) {
//                                Text("Cancelar")
//                            }
//                        },
//                        title = { Text("¿Estás seguro?") },
//                        text = { Text("¿Quieres eliminar esta tarea?") }
//                        )
//                    }
//
//            }
//        }
//    }
//
//
//@SuppressLint("NewApi")
//fun guardarYSalirAgenda(
//    context: Context,
//    navController: NavController,
//    viewModel: AgendaViewModel,
//    taskId: String?,
//    titulo: String,
//    descripcion: String,
//    descripcionLarga: String,
//    fecha: String,
//    hora: String,
//    completado: Boolean,
//    activarAlarma: Boolean = true
//) {
//    when {
//        titulo.isBlank() -> {
//            Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
//        }
//        fecha.isBlank() -> {
//            Toast.makeText(context, "La fecha es obligatoria", Toast.LENGTH_SHORT).show()
//        }
//        hora.isBlank() -> {
//            Toast.makeText(context, "La hora es obligatoria", Toast.LENGTH_SHORT).show()
//        }
//        else -> {
//            viewModel.guardarTarea(
//                Tarea(
//                    id = taskId ?: UUID.randomUUID().toString(),
//                    titulo = titulo,
//                    descripcion = descripcion,
//                    descripcionLarga = descripcionLarga,
//                    fecha = fecha,
//                    hora = hora,
//                    completado = completado,
//                    activarAlarma = activarAlarma
//                )
//            )
//            Toast.makeText(context, "Tarea guardada", Toast.LENGTH_SHORT).show()
//            navController.popBackStack()
//        }
//    }
//}


