


// ADAPTADO: ContactosScreen.kt
package com.dls.pymetask.presentation.contactos

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.utils.MyFab
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactosScreen(
    navController: NavController,
    viewModel: ContactoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val contactos = viewModel.contactos
    val isUploading = viewModel.isUploading
    var searchQuery by remember { mutableStateOf("") }
    var contactoSeleccionado by remember { mutableStateOf<Contacto?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val contactosFiltrados = contactos
        .sortedBy { it.nombre.lowercase() }
        .filter {
            it.nombre.contains(searchQuery, ignoreCase = true) ||
                    it.telefono.contains(searchQuery)
        }

    // Cargar contactos al iniciar
    remember {
        viewModel.getContactos(context)
        true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contactos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            MyFab.Default(
                onClick = { navController.navigate("crear_contacto") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    label = { Text("Buscar contacto") },
                    placeholder = { Text("Introduce nombre o teléfono...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(contactosFiltrados) { contacto ->
                        ContactoItemCard(
                            contacto = contacto,
                            onClick = {
                                navController.navigate("detalle_contacto/${contacto.id}")
                            },
                            onDeleteClick = {
                                contactoSeleccionado = contacto
                                showConfirmDialog = true
                            }
                        )
                    }
                }
            }

            if (showConfirmDialog && contactoSeleccionado != null) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Eliminar contacto") },
                    text = { Text("¿Estás seguro de que deseas eliminar a ${contactoSeleccionado?.nombre}?") },
                    confirmButton = {
                        TextButton(onClick = {
                            contactoSeleccionado?.fotoUrl?.let { url ->
                                if (url.contains("firebasestorage")) {
                                    val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                                    ref.delete()
                                }
                            }
                            viewModel.onDeleteContacto(
                                context = context,
                                contactoId = contactoSeleccionado!!.id,
                                fotoUrl = contactoSeleccionado!!.fotoUrl
                            )
                            showConfirmDialog = false
                        }) {
                            Text("Eliminar", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}











//package com.dls.pymetask.presentation.contactos
//
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import com.dls.pymetask.domain.model.Contacto
//import com.dls.pymetask.utils.MyFab
//import com.google.firebase.storage.FirebaseStorage
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ContactosScreen(
//    navController: NavController,
//    viewModel: ContactoViewModel = hiltViewModel()
//) {
//    val contactos  by viewModel.contactos.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val error by viewModel.errorMessage.collectAsState()
//    var searchQuery by remember { mutableStateOf("") }
//    var contactoSeleccionado by remember { mutableStateOf<Contacto?>(null) }
//    var showConfirmDialog by remember { mutableStateOf(false) }
//
//    // Lista filtrada por búsqueda en tiempo real (nombre, puedes añadir teléfono/email)
//    val contactosFiltrados = contactos
//        .sortedBy { it.nombre.lowercase() } // Orden alfabético ignorando mayúsculas
//        .filter {
//            it.nombre.contains(searchQuery, ignoreCase = true) ||
//                    it.telefono.contains(searchQuery)
//        }
//
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Contactos")},
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
//                    }
//                }
//            )
//        },
//
//        // MyFab
//        floatingActionButton = {
//            MyFab.Default(
//                onClick = { navController.navigate("crear_contacto") }
//            )
//        }
//
//    ) { padding ->
//        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
//            Column(modifier = Modifier.fillMaxSize()) {
//
//                // CAMPO DE BÚSQUEDA
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = { searchQuery = it },
//                    leadingIcon = {
//                        Icon(Icons.Default.Search, contentDescription = "Buscar")
//                    },
//                    label = { Text("Buscar contacto") },
//                    placeholder = { Text("Introduce nombre o teléfono...") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                )
//
//                // LISTA DE CONTACTOS FILTRADA
//                when {
//                    isLoading -> {
//                        Box(modifier = Modifier.fillMaxSize()) {
//                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                        }
//                    }
//                    error != null -> {
//                        Box(modifier = Modifier.fillMaxSize()) {
//                            Text(
//                                text = error ?: "Error desconocido",
//                                color = MaterialTheme.colorScheme.error,
//                                modifier = Modifier.align(Alignment.Center)
//                            )
//                        }
//                    }
//                    else -> {
//                        LazyColumn(
//                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
//                            verticalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            items(contactosFiltrados) { contacto ->
//                                ContactoItemCard(
//                                    contacto = contacto,
//                                    onClick = {
//                                        navController.navigate("detalle_contacto/${contacto.id}")
//                                    },
//                                    onDeleteClick = {
//                                        contactoSeleccionado = contacto
//                                        showConfirmDialog = true
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // DIALOGO CONFIRMAR ELIMINACIÓN (igual que lo tienes)
//            if (showConfirmDialog && contactoSeleccionado != null) {
//                AlertDialog(
//                    onDismissRequest = { showConfirmDialog = false },
//                    title = { Text("Eliminar contacto") },
//                    text = { Text("¿Estás seguro de que deseas eliminar a ${contactoSeleccionado?.nombre}?") },
//                    confirmButton = {
//                        TextButton(onClick = {
//                            // Si hay foto, eliminar de Firebase Storage
//                            contactoSeleccionado?.fotoUrl?.let { url ->
//                                if (url.contains("firebasestorage")) {
//                                    val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
//                                    ref.delete()
//                                }
//                            }
//                            viewModel.onDeleteContacto(contactoSeleccionado!!.id)
//                            showConfirmDialog = false
//                        }) {
//                            Text("Eliminar", color = Color.Red)
//                        }
//                    },
//                    dismissButton = {
//                        TextButton(onClick = { showConfirmDialog = false }) {
//                            Text("Cancelar")
//                        }
//                    }
//                )
//            }
//        }
//    }
//}



