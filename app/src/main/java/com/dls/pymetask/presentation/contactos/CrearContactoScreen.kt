package com.dls.pymetask.presentation.contactos

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dls.pymetask.domain.model.Contacto
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearContactoScreen(
    navController: NavController,
    viewModel: ContactoViewModel = hiltViewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Cliente") }
    var showError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var fotoUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            coroutineScope.launch {
                try {
                    isUploading = true
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef = storageRef.child("fotos_contactos/${UUID.randomUUID()}.jpg")
                    imageRef.putFile(it).await()
                    val downloadUrl = imageRef.downloadUrl.await()
                    fotoUrl = downloadUrl.toString()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nuevo contacto",
                        fontSize = 20.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subir foto
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Subir foto",
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Color.White,
                        strokeWidth = 4.dp
                    )
                }
            }

            if (isUploading) {
                Text("Subiendo imagen...", fontSize = 14.sp, color = Color.Gray)
            }

            if (showError) {
                Text(
                    text = "Nombre y teléfono son obligatorios.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo", modifier = Modifier.align(Alignment.Start))
            Row {
                listOf("Cliente", "Proveedor").forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = tipo == it, onClick = { tipo = it })
                        Text(it)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Guardar
            Button(
                onClick = {
                    if (nombre.isNotBlank() && telefono.isNotBlank()) {
                        val nuevo = Contacto(
                            id = UUID.randomUUID().toString(),
                            nombre = nombre.trim(),
                            telefono = telefono.trim(),
                            direccion = direccion.trim(),
                            tipo = tipo,
                            fotoUrl = fotoUrl,
                            email = correo.trim(),

                        )
                        viewModel.onAddContacto(nuevo)
                        navController.popBackStack()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }

            // Botón Cancelar
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}


