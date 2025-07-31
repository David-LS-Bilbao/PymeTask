package com.dls.pymetask.presentation.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.ui.theme.Poppins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    navController: NavController,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val tareas by viewModel.tareas.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

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
                                viewModel.seleccionarTarea(tarea)
                                navController.navigate("tarea_form")
                            },
                            onEliminar = { viewModel.eliminarTarea(tarea.id) }
                        )
                    }
                }
            }
        }
    }
}




