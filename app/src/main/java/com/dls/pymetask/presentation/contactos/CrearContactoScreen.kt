// ADAPTADO: CrearContactoScreen.kt
package com.dls.pymetask.presentation.contactos

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.domain.model.Contacto
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearContactoScreen(
    navController: NavController,
    viewModel: ContactoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isUploading by viewModel.isUploading

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Cliente") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var fotoUrl by remember { mutableStateOf<String?>(null) }

    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoUri = it
            viewModel.subirImagen(context, it, UUID.randomUUID().toString()) { url ->
                fotoUrl = url
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Imagen del contacto
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable { galeriaLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (fotoUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(fotoUrl),
                    contentDescription = "Foto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Añadir foto")
            }
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
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RadioButton(
                selected = tipo == "Cliente",
                onClick = { tipo = "Cliente" }
            )
            Text("Cliente")
            RadioButton(
                selected = tipo == "Proveedor",
                onClick = { tipo = "Proveedor" }
            )
            Text("Proveedor")
        }

        Button(
            onClick = {
                val nuevoContacto = Contacto(
                    id = UUID.randomUUID().toString(),
                    nombre = nombre,
                    telefono = telefono,
                    direccion = direccion,
                    email = email,
                    tipo = tipo,
                    fotoUrl = fotoUrl
                )
                viewModel.onAddContacto(context, nuevoContacto)
                Toast.makeText(context, "Contacto guardado", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            enabled = nombre.isNotBlank() && telefono.isNotBlank() && !isUploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar contacto")
        }
    }
}

