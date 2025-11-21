
package com.dls.pymetask.presentation.contactos

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <-- i18n en Compose
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.presentation.commons.UiText
import com.dls.pymetask.utils.MyFab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactosScreen(
    navController: NavController,
    viewModel: ContactoViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // --- Estados provenientes del VM ---
    val contactos = viewModel.contactos
    val isUploading = viewModel.isUploading

    // --- Estados locales de la pantalla ---
    var searchQuery by remember { mutableStateOf("") }
    var contactoSeleccionado by remember { mutableStateOf<Contacto?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }


    val contactosFiltrados by remember(searchQuery) {
        derivedStateOf {
            // Leer 'contactos' aquí hace que Compose observe sus cambios
            viewModel.contactos
                .sortedBy { it.nombre.lowercase() }
                .filter {
                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                            it.telefono.contains(searchQuery)
                }
        }
    }

    if (contactosFiltrados.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.contacts_empty_hint))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(contactosFiltrados) { contacto -> /* ... */ }
        }
    }




    // Carga contactos al entrar en pantalla
    LaunchedEffect(Unit) { viewModel.getContactos(context) }

    // Escucha de eventos i18n del VM (toasts traducidos)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiText: UiText ->
            Toast.makeText(context, uiText.asString(context), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.contacts_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB: navegar a crear contacto
            MyFab.Default(onClick = { navController.navigate("crear_contacto") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(Modifier.fillMaxSize()) {

                // ---- Buscador ----
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.contacts_search_cd)
                        )
                    },
                    label = { Text(stringResource(R.string.contacts_search_label)) },
                    placeholder = { Text(stringResource(R.string.contacts_search_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                // ---- Lista ----
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(contactosFiltrados) { contacto ->
                        ContactoItemCard(
                            contacto = contacto,
                            onClick = { navController.navigate("detalle_contacto/${contacto.id}") },
                            onDeleteClick = {
                                contactoSeleccionado = contacto
                                showConfirmDialog = true
                            }
                        )
                    }
                }
            }

            // (Opcional) indicador de subida de foto
            if (isUploading.value) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // ---- Diálogo confirmación eliminar ----
            if (showConfirmDialog && contactoSeleccionado != null) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text(stringResource(R.string.contacts_delete_title)) },
                    text = {
                        Text(
                            text = stringResource(
                                R.string.contacts_delete_text,
                                contactoSeleccionado?.nombre.orEmpty()
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            // Delegamos la eliminación COMPLETA al ViewModel
                            contactoSeleccionado?.let { contacto ->
                                viewModel.onDeleteContacto(
                                    context = context,
                                    contactoId = contacto.id,
                                    fotoUrl = contacto.fotoUrl
                                )
                            }
                            showConfirmDialog = false
                        }) {
                            Text(
                                text = stringResource(R.string.common_delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }
        }
    }
}

