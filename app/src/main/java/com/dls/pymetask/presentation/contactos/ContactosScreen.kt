package com.dls.pymetask.presentation.contactos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Contacto
import com.google.firebase.storage.FirebaseStorage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactosScreen(
    navController: NavController,
    viewModel: ContactoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val contactos by viewModel.contactos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    var contactoSeleccionado by remember { mutableStateOf<Contacto?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contactos") },
                actions = {
                    // Botón para volver al dashboard
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver al dashboard")
                    }
                    // Botón para crear un nuevo contacto
                    IconButton(onClick = { navController.navigate("crear_contacto") }) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo contacto")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                error != null -> {
                    Text(
                        text = error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(contactos) { contacto ->
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

                    if (showConfirmDialog && contactoSeleccionado != null) {
                        AlertDialog(
                            onDismissRequest = { showConfirmDialog = false },
                            title = { Text("Eliminar contacto") },
                            text = { Text("¿Estás seguro de que deseas eliminar a ${contactoSeleccionado?.nombre}?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    // Si hay foto, eliminar de Firebase Storage
                                    contactoSeleccionado?.fotoUrl?.let { url ->
                                        if (url.contains("firebasestorage")) {
                                            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                                            ref.delete() // opcional: .addOnFailureListener { ... }
                                        }
                                    }

                                    // Eliminar de Firestore
                                    viewModel.onDeleteContacto(contactoSeleccionado!!.id)
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
    }
}


