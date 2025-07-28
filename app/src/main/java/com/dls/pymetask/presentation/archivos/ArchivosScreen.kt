package com.dls.pymetask.presentation.archivos

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.ui.theme.Poppins
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivosScreen(
    navController: NavController,
    viewModel: ArchivosViewModel = hiltViewModel()
) {
    val archivos by viewModel.archivos.collectAsState()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    var mostrarDialogo by remember { mutableStateOf(false) }
    var nombreCarpeta by remember { mutableStateOf("") }


    // Mostrar mensajes (Toast) desde ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.cargarArchivos()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val nombre = uri.lastPathSegment?.substringAfterLast("/")
                    ?: "archivo_${System.currentTimeMillis()}"
                viewModel.subirArchivo(uri, nombre)
            }
        }
    )

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
                },
                actions = {
                    IconButton(onClick = { launcher.launch("*/*") }) {
                        Icon(Icons.Default.Upload, contentDescription = "Subir archivo")
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
        containerColor = if (isDark) Color.Black else Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(archivos) { archivo ->
                    ArchivoItemCard(
                        archivo = archivo,
                        onClick = { viewModel.onArchivoClick(archivo) }
                    )
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



