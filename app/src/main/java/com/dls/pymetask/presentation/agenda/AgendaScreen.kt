package com.dls.pymetask.presentation.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.ui.theme.Poppins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    navController: NavController,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val tareas by viewModel.tareas.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarTareas()
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.seleccionarTarea(null) // nueva tarea
                navController.navigate("tarea_form")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir tarea")
            }
        },
        topBar = {
            TopAppBar(title = {
                Text("Agenda", fontFamily = Poppins, fontWeight = FontWeight.SemiBold)
            })
        }
    ) { padding ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (tareas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay tareas aún.")
                }
            } else {
                LazyColumn(
                    contentPadding = padding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(tareas) { tarea ->
                        TareaCard(
                            tarea = tarea,
                            onEditar = {
                              //  viewModel.seleccionarTarea(tarea)
                                navController.navigate("tarea_form?taskId=${tarea.id}")
                            },
                            onEliminar = { viewModel.eliminarTarea(tarea.id) }
                        )
                    }
                }
            }
        }
    }
}




