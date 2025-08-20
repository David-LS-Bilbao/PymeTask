// ADAPTADO: CrearContactoScreen.kt
package com.dls.pymetask.presentation.contactos

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    var fotoUrl by remember { mutableStateOf<String?>(null) }

    // ---------- IMAGEN
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.subirImagen(context, it, UUID.randomUUID().toString()) { url ->
                fotoUrl = url
            }
        }
    }

    // Launcher para elegir contacto de la agenda
    val pickPhoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val dataUri = res.data?.data ?: return@rememberLauncherForActivityResult
        context.contentResolver.query(
            dataUri,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            ),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(0) ?: ""
                val numberRaw = cursor.getString(1) ?: ""
                val contactId = cursor.getString(2) ?: ""

                val number = numberRaw.replace(" ", "").replace("-", "")

                nombre = name
                telefono = number

                val mail = cargarEmailPorContactId(context, contactId)
                if (!mail.isNullOrBlank()) email = mail
            }
        }
    }

// Launcher para pedir permiso de contactos
    val requestContactsPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent =
                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            pickPhoneLauncher.launch(intent)
        } else {
            Toast.makeText(context, "Permiso de contactos denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear contacto nuevo") },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------- Tu selector de imagen tal cual
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
                RadioButton(selected = tipo == "Cliente", onClick = { tipo = "Cliente" })
                Text("Cliente")
                RadioButton(selected = tipo == "Proveedor", onClick = { tipo = "Proveedor" })
                Text("Proveedor")
            }

            // ---------- Botón Guardar
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
            ) { Text("Guardar contacto") }

            // ---------- 🔵 NUEVO: botón Importar
            OutlinedButton(
                onClick = {
                    // Comprobamos permiso en caliente
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        val intent = Intent(
                            Intent.ACTION_PICK,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                        )
                        pickPhoneLauncher.launch(intent)
                    } else {
                        requestContactsPermission.launch(android.Manifest.permission.READ_CONTACTS)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Importar de la agenda") }


        }
    }
}

/**
 *  intenta recuperar un email del contacto usando su CONTACT_ID.
 * Devuelve el primero que encuentre o null si no hay.
 */
private fun cargarEmailPorContactId(context: Context, contactId: String): String? {
    val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
    val selection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID}=?"
    val selectionArgs = arrayOf(contactId)

    context.contentResolver.query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            return cursor.getString(0)
        }
    }
    return null
}




//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CrearContactoScreen(
//    navController: NavController,
//    viewModel: ContactoViewModel = hiltViewModel()
//) {
//    val context = LocalContext.current
//    val isUploading by viewModel.isUploading
//    var nombre by remember { mutableStateOf("") }
//    var telefono by remember { mutableStateOf("") }
//    var direccion by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var tipo by remember { mutableStateOf("Cliente") }
//    var fotoUrl by remember { mutableStateOf<String?>(null) }
//
//    // Launcher para seleccionar imagen de la galería
//    val galeriaLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//            viewModel.subirImagen(context, it, UUID.randomUUID().toString()) { url ->
//                fotoUrl = url
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .padding(16.dp)
//            .fillMaxSize(),
//        verticalArrangement = Arrangement.spacedBy(12.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        // Imagen del contacto
//        Box(
//            modifier = Modifier
//                .size(120.dp)
//                .clip(CircleShape)
//                .background(Color.Gray)
//                .clickable { galeriaLauncher.launch("image/*") },
//            contentAlignment = Alignment.Center
//        ) {
//            if (fotoUrl != null) {
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
//                val nuevoContacto = Contacto(
//                    id = UUID.randomUUID().toString(),
//                    nombre = nombre,
//                    telefono = telefono,
//                    direccion = direccion,
//                    email = email,
//                    tipo = tipo,
//                    fotoUrl = fotoUrl
//                )
//                viewModel.onAddContacto(context, nuevoContacto)
//                Toast.makeText(context, "Contacto guardado", Toast.LENGTH_SHORT).show()
//                navController.popBackStack()
//            },
//            enabled = nombre.isNotBlank() && telefono.isNotBlank() && !isUploading,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Guardar contacto")
//        }
//    }
//}

