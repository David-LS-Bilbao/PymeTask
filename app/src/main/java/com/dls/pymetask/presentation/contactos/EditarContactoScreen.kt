

// ADAPTADO: EditarContactoScreen.kt
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
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
fun EditarContactoScreen(
    navController: NavController,
    contactoId: String,
    viewModel: ContactoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isUploading by viewModel.isUploading

    // 1) Cargar contactos al entrar (snapshot listener a Firestore)
    LaunchedEffect(Unit) {
        viewModel.getContactos(context)
    }

    // 2) Mientras no haya contactos, muestra un loader (evita pantalla en blanco)
    val contactos = viewModel.contactos
    if (contactos.isEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar contacto") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    // 3) Ya con datos, busca el contacto (SIN return temprano)
    val contacto = contactos.find { it.id == contactoId }

    // 4) Controla el caso "no encontrado"
    if (contacto == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar contacto") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Contacto no encontrado")
            }
        }
        return
    }

    // 5) Estados de UI: se inicializan UNA vez cuando llega el contacto
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf<String?>(null) }
    var inicializado by remember(contactoId) { mutableStateOf(false) }

    LaunchedEffect(contacto) {
        if (!inicializado) {
            nombre = contacto.nombre
            telefono = contacto.telefono
            direccion = contacto.direccion
            email = contacto.email
            tipo = contacto.tipo
            fotoUrl = contacto.fotoUrl
            inicializado = true
        }
    }

    // 6) Picker de galería (mantienes tu lógica de subirImagen)
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.subirImagen(context, it, contacto.id) { url ->
                fotoUrl = url
            }
        }
    }

    // 7) UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar contacto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { galeriaLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (!fotoUrl.isNullOrBlank()) {
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
                RadioButton(selected = tipo == "Cliente", onClick = { tipo = "Cliente" })
                Text("Cliente")
                RadioButton(selected = tipo == "Proveedor", onClick = { tipo = "Proveedor" })
                Text("Proveedor")
            }

            Button(
                onClick = {
                    val contactoActualizado = contacto.copy(
                        nombre = nombre,
                        telefono = telefono,
                        direccion = direccion,
                        email = email,
                        tipo = tipo,
                        fotoUrl = fotoUrl
                    )
                    viewModel.onUpdateContacto(context, contactoActualizado)
                    Toast.makeText(context, "Contacto actualizado", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                enabled = nombre.isNotBlank() && telefono.isNotBlank() && !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
        }
    }
}









//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EditarContactoScreen(
//    navController: NavController,
//    contactoId: String,
//    viewModel: ContactoViewModel = hiltViewModel()
//) {
//    val context = LocalContext.current
//    val contacto = viewModel.contactos.find { it.id == contactoId }
//
//    var nombre by remember { mutableStateOf(contacto.nombre) }
//    var telefono by remember { mutableStateOf(contacto.telefono) }
//    var direccion by remember { mutableStateOf(contacto.direccion) }
//    var email by remember { mutableStateOf(contacto.email) }
//    var tipo by remember { mutableStateOf(contacto.tipo) }
//    var fotoUrl by remember { mutableStateOf(contacto.fotoUrl) }
//    val isUploading by viewModel.isUploading
//
//    val galeriaLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//            viewModel.subirImagen(context, it, contacto.id) { url ->
//                fotoUrl = url
//            }
//        }
//    }
//
//
//
//
//    // 1) Cargar contactos al entrar (snapshot listener)
//    LaunchedEffect(Unit) {
//        viewModel.getContactos(context)
//    }
//
//    // 2) Mientras no haya contactos, muestra un loader (evita el return prematuro)
//    val contactos = viewModel.contactos
//    if (contactos.isEmpty()) {
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text("Editar contacto") },
//                    navigationIcon = {
//                        IconButton(onClick = { navController.popBackStack() }) {
//                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
//                        }
//                    }
//                )
//            }
//        ) { padding ->
//            Box(
//                modifier = Modifier
//                    .padding(padding)
//                    .fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) { CircularProgressIndicator() }
//        }
//        return
//    }
//
//
//
//    Column(
//        modifier = Modifier
//            .padding(16.dp)
//            .fillMaxSize(),
//        verticalArrangement = Arrangement.spacedBy(12.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Box(
//            modifier = Modifier
//                .size(120.dp)
//                .clip(CircleShape)
//                .background(Color.Gray)
//                .clickable { galeriaLauncher.launch("image/*") },
//            contentAlignment = Alignment.Center
//        ) {
//            if (!fotoUrl.isNullOrBlank()) {
//                Image(
//                    painter = rememberAsyncImagePainter(fotoUrl),
//                    contentDescription = "Foto",
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxSize()
//                )
//            } else {
//                Icon(Icons.Default.AddAPhoto, contentDescription = "Añadir foto")
//            }
//        }
//
//        OutlinedTextField(
//            value = nombre,
//            onValueChange = { nombre = it },
//            label = { Text("Nombre") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        OutlinedTextField(
//            value = telefono,
//            onValueChange = { telefono = it },
//            label = { Text("Teléfono") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        OutlinedTextField(
//            value = direccion,
//            onValueChange = { direccion = it },
//            label = { Text("Dirección") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//            RadioButton(
//                selected = tipo == "Cliente",
//                onClick = { tipo = "Cliente" }
//            )
//            Text("Cliente")
//            RadioButton(
//                selected = tipo == "Proveedor",
//                onClick = { tipo = "Proveedor" }
//            )
//            Text("Proveedor")
//        }
//
//        Button(
//            onClick = {
//                val contactoActualizado = contacto.copy(
//                    nombre = nombre,
//                    telefono = telefono,
//                    direccion = direccion,
//                    email = email,
//                    tipo = tipo,
//                    fotoUrl = fotoUrl
//                )
//                viewModel.onUpdateContacto(context, contactoActualizado)
//                Toast.makeText(context, "Contacto actualizado", Toast.LENGTH_SHORT).show()
//                navController.popBackStack()
//            },
//            enabled = nombre.isNotBlank() && telefono.isNotBlank() && !isUploading,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Guardar cambios")
//        }
//    }
//}





//package com.dls.pymetask.presentation.contactos
//
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.pm.ActivityInfo
//import android.net.Uri
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.CameraAlt
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.RadioButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.google.firebase.storage.FirebaseStorage
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//@SuppressLint("SourceLockedOrientationActivity")
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EditarContactoScreen(
//    navController: NavController,
//    contactoId: String,
//    viewModel: ContactoViewModel = androidx.hilt.navigation.compose.hiltViewModel()
//) {
//    val contactos by viewModel.contactos.collectAsState()
//    val contacto = contactos.find { it.id == contactoId }
//    var showDeleteDialog by remember { mutableStateOf(false) }
//
//    if (contacto == null) {
//        Text("Contacto no encontrado", modifier = Modifier.padding(16.dp))
//        return
//    }
//
//    var nombre by remember { mutableStateOf(contacto.nombre) }
//    var telefono by remember { mutableStateOf(contacto.telefono) }
//    var correo by remember { mutableStateOf(contacto.email) }
//    var direccion by remember { mutableStateOf(contacto.direccion) }
//    var tipo by remember { mutableStateOf(contacto.tipo) }
//    var fotoUrl by remember { mutableStateOf(contacto.fotoUrl) }
//    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
//    var showError by remember { mutableStateOf(false) }
//    var isUploading by remember { mutableStateOf(false) }
//
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    //desactivar modo landscape
//
//    val activity = context as? Activity
//    LaunchedEffect(Unit) {
//        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//    }
//    // reactivar modo landscape
//    DisposableEffect(Unit) {
//        onDispose {
//            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//        }
//    }//---------------------------------------------------
//
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//            selectedImageUri = it
//            coroutineScope.launch {
//                try {
//                    isUploading = true
//                    val storageRef = FirebaseStorage.getInstance().reference
//                    // Eliminar la imagen anterior si existe
//                    fotoUrl?.let { previousUrl ->
//                        try {
//                            val previousRef = FirebaseStorage.getInstance().getReferenceFromUrl(previousUrl)
//                            previousRef.delete().await()
//                        } catch (_: Exception) {
//                            // Si falla la eliminación, no interrumpimos la subida
//                        }
//                    }
//
//                    // Subir la nueva imagen
//                    val newImageRef = storageRef.child("fotos_contactos/${contactoId}.jpg")
//                    newImageRef.putFile(it).await()
//                    val downloadUrl = newImageRef.downloadUrl.await()
//                    fotoUrl = downloadUrl.toString()
//
//
//
//                } catch (_: Exception) {
//                    Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
//                } finally {
//                    isUploading = false
//                }
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text("Editar contacto", fontSize = 20.sp, modifier = Modifier.fillMaxWidth())
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .padding(horizontal = 24.dp, vertical = 12.dp)
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState()),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Imagen o selección
//            Box(
//                modifier = Modifier
//                    .size(120.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFFE3F2FD))
//                    .clickable(enabled = !isUploading) { imagePickerLauncher.launch("image/*") },
//                contentAlignment = Alignment.Center
//            ) {
//                if (selectedImageUri != null) {
//                    AsyncImage(
//                        model = selectedImageUri,
//                        contentDescription = "Imagen seleccionada",
//                        modifier = Modifier.fillMaxSize()
//                    )
//                } else if (!fotoUrl.isNullOrBlank()) {
//                    AsyncImage(
//                        model = fotoUrl,
//                        contentDescription = "Foto actual",
//                        modifier = Modifier.fillMaxSize()
//                    )
//                } else {
//                    Icon(
//                        imageVector = Icons.Default.CameraAlt,
//                        contentDescription = "Subir foto",
//                        tint = Color(0xFF1976D2),
//                        modifier = Modifier.size(36.dp)
//                    )
//                }
//
//                if (isUploading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(40.dp),
//                        color = Color.White,
//                        strokeWidth = 4.dp
//                    )
//                }
//            }
//
//            if (isUploading) {
//                Text("Subiendo imagen...", fontSize = 14.sp, color = Color.Gray)
//            }
//
//            if (showError) {
//                Text(
//                    text = "Nombre y teléfono son obligatorios.",
//                    color = MaterialTheme.colorScheme.error
//                )
//            }
//
//            if (!fotoUrl.isNullOrBlank() || selectedImageUri != null) {
//                TextButton(
//                    onClick = {
//                        coroutineScope.launch {
//                            try {
//                                isUploading = true
//
//                                // Eliminar imagen previa (solo si viene de Firebase URL)
//                                fotoUrl?.let { url ->
//                                    if (url.contains("firebasestorage")) {
//                                        try {
//                                            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
//                                            ref.delete().await()
//                                        } catch (_: Exception) {
//                                            Toast.makeText(context, "Error al eliminar la foto", Toast.LENGTH_SHORT).show()
//                                        }
//                                    }
//                                }
//
//                                // Limpiar estado
//                                fotoUrl = null
//                                selectedImageUri = null
//
//                            } finally {
//                                isUploading = false
//                            }
//                        }
//                    },
//                    enabled = !isUploading
//                ) {
//                    Text("Eliminar foto")
//                }
//            }
//
//
//            OutlinedTextField(
//                value = nombre,
//                onValueChange = { nombre = it },
//                label = { Text("Nombre") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            OutlinedTextField(
//                value = telefono,
//                onValueChange = { telefono = it },
//                label = { Text("Teléfono") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            OutlinedTextField(
//                value = correo,
//                onValueChange = { correo = it },
//                label = { Text("Correo electrónico") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            OutlinedTextField(
//                value = direccion,
//                onValueChange = { direccion = it },
//                label = { Text("Dirección") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Text("Tipo", modifier = Modifier.align(Alignment.Start))
//            Row {
//                listOf("Cliente", "Proveedor").forEach {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        RadioButton(selected = tipo == it, onClick = { tipo = it })
//                        Text(it)
//                        Spacer(modifier = Modifier.width(16.dp))
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = {
//                    if (nombre.isNotBlank() && telefono.isNotBlank()) {
//                        val actualizado = contacto.copy(
//                            nombre = nombre.trim(),
//                            telefono = telefono.trim(),
//                            direccion = direccion.trim(),
//                            tipo = tipo,
//                            email = correo.trim(),
//                            fotoUrl = fotoUrl
//                        )
//                        viewModel.onUpdateContacto(actualizado)
//                        navController.popBackStack()
//                    } else {
//                        showError = true
//                    }
//                },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !isUploading
//            ) {
//                Text("Guardar")
//            }
//
//            OutlinedButton(
//                onClick = { navController.popBackStack() },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !isUploading
//            ) {
//                Text("Cancelar")
//            }
//            OutlinedButton(
//                onClick = { showDeleteDialog = true },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !isUploading,
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
//            ) {
//                Text("Eliminar contacto")
//            }
//            // Dialog de confirmación
//            if (showDeleteDialog) {
//                AlertDialog(
//                    onDismissRequest = { showDeleteDialog = false },
//                    title = { Text("¿Eliminar contacto?") },
//                    text = { Text("Esta acción no se puede deshacer. ¿Deseas continuar?") },
//                    confirmButton = {
//                        TextButton(
//                            onClick = {
//                                coroutineScope.launch {
//                                    isUploading = true
//
//                                    // Eliminar imagen si existe
//                                    fotoUrl?.let { url ->
//                                        if (url.contains("firebasestorage")) {
//                                            try {
//                                                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(url)
//                                                ref.delete().await()
//                                            } catch (_: Exception) {
//                                                Toast.makeText(context, "No se pudo eliminar la imagen.", Toast.LENGTH_SHORT).show()
//                                            }
//                                        }
//                                    }
//
//                                    // Eliminar contacto
//                                    viewModel.onDeleteContacto(contactoId)
//
//                                    isUploading = false
//                                    showDeleteDialog = false
//                                    navController.popBackStack()
//                                }
//                            }
//                        ) {
//                            Text("Eliminar", color = Color.Red)
//                        }
//                    },
//                    dismissButton = {
//                        TextButton(onClick = { showDeleteDialog = false }) {
//                            Text("Cancelar")
//                        }
//                    }
//                )
//            }
//        }
//    }
//}



