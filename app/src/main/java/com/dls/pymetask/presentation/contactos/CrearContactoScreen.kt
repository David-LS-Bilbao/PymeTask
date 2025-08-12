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








//package com.dls.pymetask.presentation.contactos
//
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.pm.ActivityInfo
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.provider.ContactsContract
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.CameraAlt
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.content.ContextCompat
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.dls.pymetask.domain.model.Contacto
//import com.google.firebase.storage.FirebaseStorage
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//import java.util.*
//
//@SuppressLint("SourceLockedOrientationActivity")
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CrearContactoScreen(
//    navController: NavController,
//    viewModel: ContactoViewModel = hiltViewModel()
//) {
//    var nombre by remember { mutableStateOf("") }
//    var telefono by remember { mutableStateOf("") }
//    var correo by remember { mutableStateOf("") }
//    var direccion by remember { mutableStateOf("") }
//    var tipo by remember { mutableStateOf("Cliente") }
//    var showError by remember { mutableStateOf(false) }
//
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
//    var fotoUrl by remember { mutableStateOf<String?>(null) }
//    var isUploading by remember { mutableStateOf(false) }
//
//    val contactPermission = android.Manifest.permission.READ_CONTACTS
//
//    //desactivar modo landscape
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
//                    val imageRef = storageRef.child("fotos_contactos/${UUID.randomUUID()}.jpg")
//                    imageRef.putFile(it).await()
//                    val downloadUrl = imageRef.downloadUrl.await()
//                    fotoUrl = downloadUrl.toString()
//                } catch (_: Exception) {
//                    Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
//                } finally {
//                    isUploading = false
//                }
//            }
//        }
//    }
//
//    val contactPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickContact()
//    ) { uri: Uri? ->
//        uri?.let {
//            val resolver = context.contentResolver
//            val cursor = resolver.query(it, null, null, null, null)
//            cursor?.use { it ->
//                if (it.moveToFirst()) {
//                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
//                    val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
//                    val contactName = it.getString(nameIndex)
//                    val contactId = it.getString(idIndex)
//
//                    val phonesCursor = resolver.query(
//                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                        null,
//                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
//                        arrayOf(contactId),
//                        null
//                    )
//                    phonesCursor?.use { pc ->
//                        if (pc.moveToFirst()) {
//                            val phoneIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
//                            val phone = pc.getString(phoneIndex)
//
//                            // Rellenar campos
//                            nombre = contactName
//                            telefono = phone
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            contactPickerLauncher.launch(null)
//        } else {
//            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Nuevo contacto",
//                        fontSize = 20.sp,
//                        modifier = Modifier.fillMaxWidth(),
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
//            // Subir foto
//            Box(
//                modifier = Modifier
//                    .size(120.dp)
//                    .clip(CircleShape)
//                    .background(Color(0xFFE3F2FD))
//                    .clickable { imagePickerLauncher.launch("image/*") },
//                contentAlignment = Alignment.Center
//            ) {
//                if (selectedImageUri != null) {
//                    AsyncImage(
//                        model = selectedImageUri,
//                        contentDescription = "Imagen seleccionada",
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
//            OutlinedTextField(
//                value = nombre,
//                onValueChange = { nombre = it },
//                label = { Text("Nombre") },
//                maxLines = 1,
//                // pasar al sigiente cuadro de texto al pulsar enter
//                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            OutlinedTextField(
//                value = telefono,
//                onValueChange = { telefono = it },
//                label = { Text("Teléfono") },
//                // modo de texto para el teléfono
//                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            OutlinedTextField(
//                value = correo,
//                onValueChange = { correo = it },
//                label = { Text("Correo electrónico") },
//                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            OutlinedTextField(
//                value = direccion,
//                onValueChange = { direccion = it },
//                label = { Text("Dirección") },
//                maxLines = 1,
//                // pasar al sigiente cuadro de texto al pulsar enter
//                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
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
//            Button(
//                onClick = {
//                    if (ContextCompat.checkSelfPermission(context, contactPermission)
//                        == PackageManager.PERMISSION_GRANTED) {
//                    contactPickerLauncher.launch(null)
//                } else {
//                    permissionLauncher.launch(contactPermission)
//                } },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Importar contacto")
//            }
//            // Botón Guardar
//            Button(
//                onClick = {
//                    if (nombre.isNotBlank() && telefono.isNotBlank()) {
//                        val nuevo = Contacto(
//                            id = UUID.randomUUID().toString(),
//                            nombre = nombre.trim(),
//                            telefono = telefono.trim(),
//                            direccion = direccion.trim(),
//                            tipo = tipo,
//                            fotoUrl = fotoUrl,
//                            email = correo.trim(),
//
//                        )
//                        viewModel.onAddContacto(nuevo)
//                        navController.popBackStack()
//                    } else {
//                        showError = true
//                    }
//                },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Guardar")
//            }
//
//            // Botón Cancelar
//            OutlinedButton(
//                onClick = { navController.popBackStack() },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Cancelar")
//            }
//        }
//    }
//}


