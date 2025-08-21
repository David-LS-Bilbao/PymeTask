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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <-- i18n
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.R
import com.dls.pymetask.presentation.commons.UiText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarContactoScreen(
    navController: NavController,
    contactoId: String,
    viewModel: ContactoViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Escuchar eventos del VM para toasts localizados
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiText: UiText ->
            Toast.makeText(context, uiText.asString(context), Toast.LENGTH_SHORT).show()
        }
    }

    // Cargar contactos al entrar
    LaunchedEffect(Unit) { viewModel.getContactos(context) }

    // Mientras no haya contactos, muestra loader
    val contactos = viewModel.contactos
    val isUploading by viewModel.isUploading
    if (contactos.isEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.contacts_edit_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    // Buscar contacto
    val contacto = contactos.find { it.id == contactoId }
    if (contacto == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.contacts_edit_title)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.contacts_not_found))
            }
        }
        return
    }

    // Estados de edición (inicialización segura cuando llega el contacto)
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

    // Picker de imagen
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.subirImagen(context, it, contacto.id) { url -> fotoUrl = url }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.contacts_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
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

            // Foto (CD localizada)
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
                        contentDescription = stringResource(R.string.contacts_photo_cd_generic),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.AddAPhoto, contentDescription = stringResource(R.string.contacts_photo_add))
                }
            }

            // Campos (localizados)
            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text(stringResource(R.string.contacts_field_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono, onValueChange = { telefono = it },
                label = { Text(stringResource(R.string.contacts_field_phone)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = direccion, onValueChange = { direccion = it },
                label = { Text(stringResource(R.string.contacts_field_address)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text(stringResource(R.string.contacts_field_email)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Tipo (etiquetas localizadas, valor de dominio guardado en ES)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RadioButton(selected = tipo == "Cliente", onClick = { tipo = "Cliente" })
                Text(stringResource(R.string.contact_type_client))
                RadioButton(selected = tipo == "Proveedor", onClick = { tipo = "Proveedor" })
                Text(stringResource(R.string.contact_type_supplier))
            }

            // Guardar cambios (sin toast local: lo emite el VM)
            Button(
                onClick = {
                    val contactoActualizado = contacto.copy(
                        nombre = nombre.trim(),
                        telefono = telefono.trim(),
                        direccion = direccion.trim(),
                        email = email.trim(),
                        tipo = tipo,
                        fotoUrl = fotoUrl
                    )
                    viewModel.onUpdateContacto(context, contactoActualizado)
                    navController.popBackStack()
                },
                enabled = nombre.isNotBlank() && telefono.isNotBlank() && !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.contacts_save_changes)) }
        }
    }
}




