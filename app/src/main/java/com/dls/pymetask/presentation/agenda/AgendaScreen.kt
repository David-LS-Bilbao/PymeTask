package com.dls.pymetask.presentation.agenda

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.utils.MyFab

@SuppressLint("NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    navController: NavController,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tareas by viewModel.tareas.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current


    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) AlarmOptionsDialog(
        initialToneUri = PreferencesHelper.getToneUri(context),
        initialLeadMinutes = PreferencesHelper.getLeadMinutes(context),
        onDismiss = { showDialog = false },
        onPickRingtone = { /* abre selector de tonos */ },
        onLeadTimeChange = { minutes ->
            PreferencesHelper.saveLeadMinutes(context, minutes)
        }
    )


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargarTareas()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // DISEÑO DE PANTALLA========================================================================
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda", fontFamily = Poppins, fontWeight = FontWeight.SemiBold) },
                   navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
                // BOTON OPCIONES DE ALARMA
                , actions = {
                    IconButton(onClick = {
                        // MENU DE OPCIONES DE ALARMA
                        showDialog = true

                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Opciones Alarma")
                    }
                }
            )
        },

        // MyFAB
        floatingActionButton = {
            MyFab.Default(
                onClick = { navController.navigate("tarea_form") }
            )
        },
                containerColor = MaterialTheme.colorScheme.background,)

        { padding ->

             Column(modifier = Modifier.padding(padding)) {

                 when{
                     isLoading -> {
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             CircularProgressIndicator()
                         }
                     }
                     tareas.isEmpty() -> {
                         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                             Text("No hay tareas aún.")
                             Log.d("AgendaScreen", "No hay tareas aún.")
                         }
                     }
                     else -> {
                         LazyColumn(
                           verticalArrangement = Arrangement.spacedBy(8.dp),
                             modifier = Modifier.padding(8.dp)
                         ) {

                             // Ordenar las tareas por fecha y hora para mostrarlas en el orden correcto
                             val tareasOrdenadas = tareas.sortedWith(compareBy({ it.fecha }, { it.hora }))
                             items(tareasOrdenadas, key = { it.id }) { tarea ->
                                 TareaCard(tarea = tarea) {
                                     viewModel.seleccionarTarea(tarea.id)
                                     navController.navigate("tarea_form?taskId=${tarea.id}")
                                     Log.d("AgendaScreen", "Tarea seleccionada: ${tarea.id}")
                                 }
                             }

                         }
                     }
                 }
             }
    }
}




