package com.dls.pymetask.presentation.archivos

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.presentation.components.ArchivoCardExtendido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoCarpetaScreen(
    carpetaId: String,
    navController: NavController,
    viewModel: ContenidoCarpetaViewModel = hiltViewModel(),
) {
    val archivos by viewModel.archivos.collectAsState()
    val context = LocalContext.current
    var mostrarMenu by remember { mutableStateOf(false) }
    var selectedArchivo by remember { mutableStateOf<ArchivoUiModel?>(null) }
    var nombreCarpeta by remember { mutableStateOf("Carpeta") }
    var mostrarMenuTipoArchivo by remember { mutableStateOf(false) }
    var mimeToLoad by remember { mutableStateOf("*/*") }
    var expanded by remember { mutableStateOf(false) }
    var mostrarDialogoRenombrar by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }

    val estaCargando by viewModel.cargando.collectAsState()





    LaunchedEffect(Unit) {
        viewModel.cargarArchivosDeCarpeta(carpetaId)
        viewModel.obtenerNombreCarpeta(carpetaId) { nombre ->
            nombreCarpeta = nombre
        }

    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { mensaje ->
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.cargarArchivosDeCarpeta(carpetaId)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val nombre = uri.lastPathSegment?.substringAfterLast("/")
                    ?: "archivo_${System.currentTimeMillis()}"
                viewModel.subirArchivo(context, uri, nombre, carpetaId) {
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreCarpeta) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Renombrar carpeta") },
                                onClick = {
                                    nuevoNombre = nombreCarpeta // si lo tienes disponible
                                    mostrarDialogoRenombrar = true
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar carpeta") },
                                onClick = {
                                    mostrarDialogoEliminar = true
                                    expanded = false
                                }
                            )
                        }
                    }
                    if (estaCargando) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                }
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                FloatingActionButton(onClick = { mostrarMenuTipoArchivo = true }) {
                    Icon(Icons.Default.Upload, contentDescription = "Subir archivo")
                }

                DropdownMenu(
                    expanded = mostrarMenuTipoArchivo,
                    onDismissRequest = { mostrarMenuTipoArchivo = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            mimeToLoad = "image/*"
                            mostrarMenuTipoArchivo = false
                            launcher.launch(mimeToLoad)
                        },
                        text = { Text("Imagen 📷") }
                    )
                    DropdownMenuItem(
                        onClick = {
                            mimeToLoad = "audio/*"
                            mostrarMenuTipoArchivo = false
                            launcher.launch(mimeToLoad)
                        },
                        text = { Text("Audio 🎧") }
                    )
                    DropdownMenuItem(
                        onClick = {
                            mimeToLoad = "video/*"
                            mostrarMenuTipoArchivo = false
                            launcher.launch(mimeToLoad)
                        },
                        text = { Text("Vídeo 🎥") }
                    )
                    DropdownMenuItem(
                        onClick = {
                            mimeToLoad = "*/*"
                            mostrarMenuTipoArchivo = false
                            launcher.launch(mimeToLoad)
                        },
                        text = { Text("Otro archivo 📄") }
                    )
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                estaCargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                archivos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📂 Esta carpeta está vacía",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pulsa el botón + para añadir archivos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1), // o 2 si prefieres
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),

                    ) {
                        items(archivos) { archivo ->
                            ArchivoCardExtendido(
                                archivo = archivo,

                                onRenombrar = {
                                    selectedArchivo = archivo
                                    nuevoNombre = archivo.nombre
                                    mostrarDialogoRenombrar = true  // modificado
                                },
                                onEliminar = {
                                    viewModel.eliminarArchivo(archivo.id, carpetaId)
                                }
                            )
                        }
                    }
                }
            }



            if (mostrarMenu && selectedArchivo != null) {
                AlertDialog(
                    onDismissRequest = { mostrarMenu = false },
                    title = { Text("¿Qué deseas hacer con '${selectedArchivo?.nombre}'?") },
                    confirmButton = {
                        Column {

                            TextButton(onClick = {

                                nuevoNombre = selectedArchivo?.nombre ?: ""
                                mostrarDialogoRenombrar = true
                                mostrarMenu = false
                            }) {
                                Text("Renombrar archivo")
                            }



                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "*/*"
                                    putExtra(Intent.EXTRA_TEXT, selectedArchivo?.url)
                                    setPackage("com.whatsapp")
                                }
                                context.startActivity(intent)
                                mostrarMenu = false
                            }) {
                                Text("Enviar por WhatsApp")
                            }

                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, "Archivo desde PymeTask")
                                    putExtra(Intent.EXTRA_TEXT, selectedArchivo?.url)
                                }
                                context.startActivity(intent)
                                mostrarMenu = false
                            }) {
                                Text("Enviar por Email")
                            }

                            TextButton(onClick = {
                                viewModel.eliminarArchivo(selectedArchivo!!.id, carpetaId)
                                mostrarMenu = false
                                mostrarDialogoEliminar = true
                            }) {
                                Text("Eliminar archivo", color = Color.Red)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarMenu = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (mostrarDialogoRenombrar && selectedArchivo != null) {
                val nombreCompleto = selectedArchivo!!.nombre
                val nombreBase = nombreCompleto.substringBeforeLast(".")
                val extension = nombreCompleto.substringAfterLast(".", "")

                // Estado separado para el nombre editable
                var nombreSinExtension by remember { mutableStateOf(nombreBase) }

                AlertDialog(
                    onDismissRequest = { mostrarDialogoRenombrar = false },
                    title = { Text("Renombrar archivo") },
                    text = {
                        Row {
                            OutlinedTextField(
                                value = nombreSinExtension,
                                onValueChange = { nombreSinExtension = it },
                                label = { Text("Nombre") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ".$extension",
                                modifier = Modifier.alignByBaseline(),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (nombreSinExtension.isNotBlank()) {
                                val nombreFinal = "${nombreSinExtension.trim()}.$extension"
                                viewModel.renombrarArchivo(
                                    selectedArchivo!!.id,
                                    nombreFinal,
                                    carpetaId
                                )
                                mostrarDialogoRenombrar = false
                            }
                        }) {
                            Text("Renombrar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            mostrarDialogoRenombrar = false
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (mostrarDialogoEliminar) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoEliminar = false },
                    title = { Text("Eliminar carpeta") },
                    text = { Text("¿Seguro que deseas eliminar esta carpeta? Esto no borrará los archivos dentro.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.eliminarCarpeta(carpetaId)
                            mostrarDialogoEliminar = false
                            navController.popBackStack() // volver atrás tras borrar
                        }) {
                            Text("Eliminar", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            mostrarDialogoEliminar = false
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

        }
    }
}
