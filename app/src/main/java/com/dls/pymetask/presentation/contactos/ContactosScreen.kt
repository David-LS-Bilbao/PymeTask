package com.dls.pymetask.presentation.contactos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.dls.pymetask.domain.model.Contacto




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactosScreen(
    viewModel: ContactoViewModel,
    onNavigateToForm: (Contacto?) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    val contactos by viewModel.contactos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var contactoSeleccionado by remember { mutableStateOf<Contacto?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar({ Text("Contactos", fontWeight = FontWeight.Bold) }, navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }, actions = {
                    IconButton(onClick = onHome) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToForm(null) }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Nuevo contacto")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage != null) {
                Text(errorMessage ?: "", color = Color.Red)
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(contactos) { contacto ->
                        ContactoItem(
                            contacto = contacto,
                            onClick = { contactoSeleccionado = contacto },
                            onEdit = { onNavigateToForm(it) },
                            onDelete = {
                                contactoSeleccionado = it
                                showDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            if (showDialog && contactoSeleccionado != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Eliminar contacto") },
                    text = { Text("¿Estás seguro de que deseas eliminar este contacto?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.onDeleteContacto(contactoSeleccionado!!.id)
                            showDialog = false
                        }) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}



@Composable
fun ContactoItem(
    contacto: Contacto,
    onClick: () -> Unit,
    onEdit: (Contacto) -> Unit,
    onDelete: (Contacto) -> Unit
) {
    Card( modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contacto.tipo == "Cliente") Color(0xFFE8F2FB) else Color(0xFFF5EEDC)
        )) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (contacto.fotoUrl != null) {
                Image(
                    painter = rememberImagePainter(contacto.fotoUrl),
                    contentDescription = "Foto de contacto",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (contacto.tipo == "Cliente") Color(0xFFD0E6FA) else Color(0xFFE9DEC3)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contacto.nombre.first().uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(contacto.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(contacto.telefono, fontSize = 14.sp)
            }

            Text(contacto.tipo, fontSize = 14.sp, fontWeight = FontWeight.Medium)

            DropdownMenuButton(contacto = contacto, onEdit = onEdit, onDelete = onDelete)
        }
    }
}

@Composable
fun DropdownMenuButton(
    contacto: Contacto,
    onEdit: (Contacto) -> Unit,
    onDelete: (Contacto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Editar") }, onClick = {
                onEdit(contacto)
                expanded = false
            })
            DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                onDelete(contacto)
                expanded = false
            })
        }
    }
}

