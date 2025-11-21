
package com.dls.pymetask.presentation.archivos

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <-- i18n Compose
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.presentation.components.ArchivoCardExtendido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoCarpetaScreen(
    carpetaId: String,
    navController: NavController,
    nombreInicial: String? = null,
    viewModel: ContenidoCarpetaViewModel = hiltViewModel()
) {
    val archivos by viewModel.archivos.collectAsState()
    val context = LocalContext.current

    // Estados UI
    var mostrarMenu by remember { mutableStateOf(false) }
    var selectedArchivo by remember { mutableStateOf<ArchivoUiModel?>(null) }

    val nombreCarpeta by viewModel.nombreCarpeta.collectAsState()

    var mostrarMenuTipoArchivo by remember { mutableStateOf(false) }
    var mimeToLoad by remember { mutableStateOf("*/*") }
    var expanded by remember { mutableStateOf(false) }
    var mostrarDialogoRenombrar by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }

    val estaCargando by viewModel.cargando.collectAsState()

    // Carga datos iniciales
    LaunchedEffect(carpetaId) {
        viewModel.cargarArchivosDeCarpeta(carpetaId)
        viewModel.obtenerNombreCarpeta(carpetaId) // ahora sin callback
    }
    // Muestra mensajes desde el VM (si vienen como literales, podemos migrarlos a recursos más adelante)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiText ->
            Toast.makeText(context, uiText.asString(context), Toast.LENGTH_SHORT).show()
        }
    }
    // 1) Pintar el nombre inmediatamente con el valor de navegación
    LaunchedEffect(nombreInicial) {
        viewModel.setNombreCarpetaInicial(nombreInicial)
    }

    // 2) Cargar datos reales y nombre desde dominio una sola vez
    LaunchedEffect(carpetaId) {
        viewModel.cargarArchivosDeCarpeta(carpetaId)
        viewModel.obtenerNombreCarpeta(carpetaId)
    }

    // Selector de contenido (sistema)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val nombre = uri.lastPathSegment?.substringAfterLast("/") ?: "archivo_${System.currentTimeMillis()}"
                viewModel.subirArchivo(context, uri, nombre, carpetaId) { /* callback subido */ }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                // Título: nombre de la carpeta (dinámico)
                title = { Text(nombreCarpeta.ifBlank { stringResource(R.string.files_folder) }) },
                navigationIcon = {
                    // Botón volver con contentDescription localizado
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    // Menú "Opciones" (tres puntos)

                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.common_options)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.files_menu_rename_folder)) },
                                onClick = {
                                    nuevoNombre = nombreCarpeta
                                    mostrarDialogoRenombrar = true
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.files_menu_delete_folder)) },
                                onClick = {
                                    mostrarDialogoEliminar = true
                                    expanded = false
                                }
                            )
                        }
                    }

                    // Indicador de carga en la app bar (si procede)
                    if (estaCargando) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(18.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB para subir archivo + menú de tipo de archivo
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                FloatingActionButton(onClick = { mostrarMenuTipoArchivo = true }) {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = stringResource(R.string.files_upload_file)
                    )
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
                        text = { Text(stringResource(R.string.files_pick_image)) }
                    )
                    DropdownMenuItem(
                        onClick = {
                            mimeToLoad = "audio/*"
                            mostrarMenuTipoArchivo = false
                            launcher.launch(mimeToLoad)
                        },
                        text = { Text(stringResource(R.string.files_pick_audio)) }
                    )
                    DropdownMenuItem(
                        onClick = {
                            mimeToLoad = "video/*"
                            mostrarMenuTipoArchivo = false
                            launcher.launch(mimeToLoad)
                        },
                        text = { Text(stringResource(R.string.files_pick_video)) }
                    )
                    DropdownMenuItem(
                        onClick = {
                            mimeToLoad = "*/*"
                            mostrarMenuTipoArchivo = false
                            launcher.launch(mimeToLoad)
                        },
                        text = { Text(stringResource(R.string.files_pick_other)) }
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
                // Estado de carga (centrado)
                estaCargando -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                // Estado vacío: carpeta sin archivos (textos localizados)
                archivos.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.files_folder_empty_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.files_folder_empty_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
                // Grid de archivos
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1), // conserva tu diseño (1 columna)
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
                                    mostrarDialogoRenombrar = true
                                },
                                onEliminar = {
                                    viewModel.eliminarArchivo(archivo.id, carpetaId)
                                }
                            )
                        }
                    }
                }
            }

            // ======= Menú contextual (archivo seleccionado) =======
            if (mostrarMenu && selectedArchivo != null) {
                AlertDialog(
                    onDismissRequest = { mostrarMenu = false },
                    title = {
                        Text(
                            // "¿Qué deseas hacer con ‘%1$s’?"
                            text = stringResource(
                                R.string.files_action_with_item,
                                selectedArchivo?.nombre.orEmpty()
                            )
                        )
                    },
                    confirmButton = {
                        Column {
                            TextButton(onClick = {
                                // Renombrar archivo
                                nuevoNombre = selectedArchivo?.nombre ?: ""
                                mostrarDialogoRenombrar = true
                                mostrarMenu = false
                            }) {
                                Text(stringResource(R.string.common_rename))
                            }
                            TextButton(onClick = {
                                // Compartir por WhatsApp (si está instalado)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "*/*"
                                    putExtra(Intent.EXTRA_TEXT, selectedArchivo?.url)
                                    setPackage("com.whatsapp")
                                }
                                context.startActivity(intent)
                                mostrarMenu = false
                            }) {
                                Text(stringResource(R.string.common_send_whatsapp))
                            }
                            TextButton(onClick = {
                                // Enviar por Email
                                val subject = context.getString(
                                    R.string.files_email_subject_from_app,
                                    context.getString(R.string.app_name)
                                )
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:".toUri()
                                    putExtra(Intent.EXTRA_SUBJECT, subject)
                                    putExtra(Intent.EXTRA_TEXT, selectedArchivo?.url)
                                }
                                context.startActivity(intent)
                                mostrarMenu = false
                            }) {
                                Text(stringResource(R.string.common_send_email))
                            }
                            TextButton(onClick = {
                                // Eliminar archivo
                                viewModel.eliminarArchivo(selectedArchivo!!.id, carpetaId)
                                mostrarMenu = false
                                mostrarDialogoEliminar = true
                            }) {
                                Text(
                                    text = stringResource(R.string.common_delete),
                                    color = Color.Red
                                )
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarMenu = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            // ======= Diálogo: renombrar archivo =======
            if (mostrarDialogoRenombrar && selectedArchivo != null) {
                val nombreCompleto = selectedArchivo!!.nombre
                val nombreBase = nombreCompleto.substringBeforeLast(".")
                val extension = nombreCompleto.substringAfterLast(".", "")

                // Campo editable separado para el nombre (sin extensión)
                var nombreSinExtension by remember { mutableStateOf(nombreBase) }

                AlertDialog(
                    onDismissRequest = { mostrarDialogoRenombrar = false },
                    title = { Text(stringResource(R.string.files_rename_file_title)) },
                    text = {
                        Row {
                            OutlinedTextField(
                                value = nombreSinExtension,
                                onValueChange = { nombreSinExtension = it },
                                label = { Text(stringResource(R.string.files_rename_file_label)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (extension.isNotBlank()) ".$extension" else "",
                                modifier = Modifier.alignByBaseline(),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (nombreSinExtension.isNotBlank()) {
                                val nombreFinal = if (extension.isNotBlank())
                                    "${nombreSinExtension.trim()}.$extension"
                                else
                                    nombreSinExtension.trim()

                                selectedArchivo?.let { archivo ->
                                    viewModel.renombrarArchivo(
                                        archivo.id,
                                        nombreFinal,
                                        carpetaId
                                    )
                                }
                                mostrarDialogoRenombrar = false
                            }
                        }) { Text(stringResource(R.string.common_rename)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoRenombrar = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }

            // ======= Diálogo: eliminar carpeta =======
            if (mostrarDialogoEliminar) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoEliminar = false },
                    title = { Text(stringResource(R.string.files_delete_folder_title)) },
                    text = { Text(stringResource(R.string.files_delete_folder_text)) },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.eliminarCarpeta(carpetaId)
                            mostrarDialogoEliminar = false
                            navController.popBackStack() // volver atrás tras borrar
                        }) {
                            Text(
                                text = stringResource(R.string.common_delete),
                                color = Color.Red
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoEliminar = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }
        }
    }
}

