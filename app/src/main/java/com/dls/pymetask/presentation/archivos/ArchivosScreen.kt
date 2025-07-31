package com.dls.pymetask.presentation.archivos

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.presentation.components.CarpetaItemCard
import com.dls.pymetask.ui.theme.Poppins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivosScreen(
    navController: NavController,
    viewModel: ArchivosViewModel = hiltViewModel()
) {
    val archivos by viewModel.archivos.collectAsState()
    val context = LocalContext.current
    var mostrarDialogo by remember { mutableStateOf(false) }
    var nombreCarpeta by remember { mutableStateOf("") }
    val estaCargando by viewModel.cargando.collectAsState()





    //desactivar modo landscape

    val activity = context as? Activity
    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
    // reactivar modo landscape
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }//---------------------------------------------------






    // Mostrar mensajes (Toast) desde ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarArchivos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Archivos",
                        fontSize = 22.sp,
                        fontFamily = Poppins
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                mostrarDialogo = true
            }) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "Nueva carpeta")
            }
        },

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (estaCargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (archivos.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📁 No hay carpetas aún",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pulsa el botón ➕ para crear una carpeta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {


                        items(archivos) { archivo ->
                            CarpetaItemCard(
                                archivo = archivo,
                                onClick = {
                                    navController.navigate("contenido_carpeta/${archivo.id}")
                                }
                            )
                        }
                    }
                }
            }

        }
    }
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Crear nueva carpeta") },
            text = {
                OutlinedTextField(
                    value = nombreCarpeta,
                    onValueChange = { nombreCarpeta = it },
                    label = { Text("Nombre de la carpeta") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nombreCarpeta.isNotBlank()) {
                        viewModel.crearCarpeta(nombreCarpeta.trim())
                        mostrarDialogo = false
                        nombreCarpeta = ""
                    }
                }) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    nombreCarpeta = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

}



