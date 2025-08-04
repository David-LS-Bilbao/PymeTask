@file:Suppress("DEPRECATION")

package com.dls.pymetask.presentation.agenda

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaFormScreen(
    taskId: String?=null,
    navController: NavController,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.loading.collectAsState()
    val tareaActual = viewModel.tareaActual

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))


    // Datos de la tarea
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var descripcionLarga by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var completado by remember { mutableStateOf(false) }
//    var activarAlarma by remember { mutableStateOf(true) }

    val activarAlarmaState = remember { mutableStateOf(true) }

    LaunchedEffect(viewModel.tareaActual) {
        viewModel.tareaActual?.let {
            activarAlarmaState.value = it.activarAlarma
        }
    }







    // Diálogo de confirmación para eliminar
    var mostrarConfirmacionBorrado by remember { mutableStateOf(false) }


    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val nuevaFecha = dateFormatter.format(calendar.time)
            fecha = nuevaFecha                      // ⬅️ actualizar UI
            viewModel.actualizarFecha(nuevaFecha)   // ⬅️ actualizar ViewModel

        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val nuevaHora = String.format("%02d:%02d", hourOfDay, minute)
            hora = nuevaHora                        // ⬅️ actualizar UI
            viewModel.actualizarHora(nuevaHora)     // ⬅️ actualizar ViewModel
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )



    // Carga de tarea si taskId está presente
    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.seleccionarTarea(taskId)
        }else{
            viewModel.limpiarTareaActual()
        }
    }

    // copiar los datos de la tarea seleccionada al formulario
    val tareaCargada = tareaActual != null

    LaunchedEffect(tareaActual) {
        tareaActual?.let { tarea ->
            titulo = tarea.titulo
            descripcion = tarea.descripcion
            descripcionLarga = tarea.descripcionLarga
            fecha = tarea.fecha
            hora = tarea.hora
            completado = tarea.completado
            activarAlarmaState.value = tarea.activarAlarma
            Log.d("TareaForm", "✅ Datos de tarea cargados correctamente")
        }
    }


        // guardar al pulsar atrás físico
    BackHandler {
       guardarYSalirAgenda(
           context = context,
           navController = navController,
           viewModel = viewModel,
           taskId = taskId,
           titulo = titulo,
           descripcion = descripcion,
           descripcionLarga = descripcionLarga,
           fecha = fecha,
           hora = hora,
           completado = completado,
           activarAlarma = activarAlarmaState.value
       )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tarea", fontFamily = Poppins) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (titulo.isBlank()) {
                            Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        // guardamos
                        guardarYSalirAgenda(
                            context = context,
                            navController = navController,
                            viewModel = viewModel,
                            taskId = taskId,
                            titulo = titulo,
                            descripcion = descripcion,
                            descripcionLarga = descripcionLarga,
                            fecha = fecha,
                            hora = hora,
                            completado = completado,
                            activarAlarma = activarAlarmaState.value
                        )

                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón guardar
                    IconButton(onClick = {
                        if (titulo.isBlank()) {
                            Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        // guardamos
                        guardarYSalirAgenda(
                            context = context,
                            navController = navController,
                            viewModel = viewModel,
                            taskId = taskId,
                            titulo = titulo,
                            descripcion = descripcion,
                            descripcionLarga = descripcionLarga,
                            fecha = fecha,
                            hora = hora,
                            completado = completado,
                            activarAlarma = activarAlarmaState.value
                        )

                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                    // Botón borrar
                    IconButton(onClick = { mostrarConfirmacionBorrado = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar nota")
                    }
                }
            )
        },

    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CAMPO: TÍTULO
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        if (titulo != it) {
                            titulo = it
                        }
                    },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // CAMPO: DESCRIPCIÓN BREVE
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = {
                        if (descripcion != it) {
                            descripcion = it
                        }
                    },
                    label = { Text("Resumen breve") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                ) {
                    // CAMPO: FECHA con calendario (formato español)
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = {

                        },
                        readOnly = true,
                        label = { Text("Fecha") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { timePickerDialog.show() }
                ) {
                    // CAMPO: HORA con reloj
                    OutlinedTextField(
                        value = hora,
                        onValueChange = {

                        },
                        readOnly = true,
                        label = { Text("Hora") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
                // CAMPO: DESCRIPCIÓN DETALLADA
                OutlinedTextField(
                    value = descripcionLarga,
                    onValueChange = {
                        if (descripcionLarga != it) {
                            descripcionLarga = it
                        }
                        },
                    label = { Text("Descripción detallada") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    singleLine = false,
                    maxLines = 5
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // CAMPO: CHECKBOX "Completada"
                    Checkbox(
                        checked = completado,
                        onCheckedChange =  { completado = it }

                    )
                    Text("Completada")

                    Spacer(modifier = Modifier.weight(1f))

                    Text("Activar alarma  ")
                // CAMPO: SWITCH "Activar alarma"
                    Log.d("SwitchEstado", "activarAlarma = $activarAlarmaState")

                    Switch(
                        checked = activarAlarmaState.value,
                        onCheckedChange = { activarAlarmaState.value = it }
                    )
                }
            }
            // Dialogo de confirmación para eliminar
            if (mostrarConfirmacionBorrado) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmacionBorrado = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val idTarea = taskId ?: viewModel.tareaActual?.id
                            if (idTarea != null) {
                                viewModel.eliminarTareaPorId(idTarea)
                                Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error al eliminar la tarea",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            mostrarConfirmacionBorrado = false
                            navController.popBackStack()
                        }) {
                            Text("Eliminar")
                        }
                    },
                        dismissButton = {
                            TextButton(onClick = { mostrarConfirmacionBorrado = false }) {
                                Text("Cancelar")
                            }
                        },
                        title = { Text("¿Estás seguro?") },
                        text = { Text("¿Quieres eliminar esta tarea?") }
                        )
                    }

            }
        }
    }


fun guardarYSalirAgenda(
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
    activarAlarma: Boolean = true
) {
    when {
        titulo.isBlank() -> {
            Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
        }
        fecha.isBlank() -> {
            Toast.makeText(context, "La fecha es obligatoria", Toast.LENGTH_SHORT).show()
        }
        hora.isBlank() -> {
            Toast.makeText(context, "La hora es obligatoria", Toast.LENGTH_SHORT).show()
        }
        else -> {
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
    }
}


