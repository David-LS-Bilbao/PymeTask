
package com.dls.pymetask.presentation.archivos

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <-- i18n en Compose
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.R
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

    // Control de orientación
    val activity = context as? Activity
    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiText ->
            Toast.makeText(context, uiText.asString(context), Toast.LENGTH_SHORT).show()
        }
    }


    // Carga inicial de archivos
    LaunchedEffect(Unit) { viewModel.cargarArchivos() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Título localizado
                    Text(
                        text = stringResource(R.string.files_title),
                        fontSize = 22.sp,
                        fontFamily = Poppins
                    )
                },
                navigationIcon = {
                    // Botón volver con contentDescription localizado
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        // FAB para crear carpeta; descripción accesible localizada
        floatingActionButton = {

            FloatingActionButton(onClick = { mostrarDialogo = true }) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = stringResource(R.string.files_create_folder)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
              //  .verticalScroll(rememberScrollState())

        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    estaCargando -> {
                        // Estado de carga centrado
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                    archivos.isEmpty() -> {
                        // Estado vacío: textos localizados
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.files_empty_title),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.files_empty_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                    else -> {
                        // Grid de carpetas
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
                                        val nombre = Uri.encode(archivo.nombre) // evita problemas con espacios y tildes
                                        navController.navigate("contenido_carpeta/${archivo.id}?nombre=$nombre")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo: crear nueva carpeta
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
                nombreCarpeta = ""
            },
            title = { Text(stringResource(R.string.files_dialog_new_folder_title)) },
            text = {
                OutlinedTextField(
                    value = nombreCarpeta,
                    onValueChange = { nombreCarpeta = it },
                    label = { Text(stringResource(R.string.files_dialog_name_label)) },
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
                }) { Text(stringResource(R.string.common_create)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    nombreCarpeta = ""
                }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}
