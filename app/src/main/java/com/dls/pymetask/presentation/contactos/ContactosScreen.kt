package com.dls.pymetask.presentation.contactos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.domain.model.Contacto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactosScreen(
    navController: NavController,
    viewModel: ContactoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val contactos by viewModel.contactos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

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
                                    navController.navigate("editar_contacto/${contacto.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun ContactoItemCard(
    contacto: Contacto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contacto.tipo == "Cliente") Color(0xFFE3F2FD) else Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto o inicial
            if (!contacto.fotoUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(contacto.fotoUrl),
                    contentDescription = "Foto de ${contacto.nombre}",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (contacto.tipo == "Cliente") Color(0xFF90CAF9) else Color(0xFFFFCC80)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contacto.nombre.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(contacto.nombre, style = MaterialTheme.typography.titleMedium)
                Text("📞 ${contacto.telefono}", fontSize = 13.sp)
                Text("🏠 ${contacto.direccion}", fontSize = 13.sp)
            }

            Text(
                text = contacto.tipo,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}



