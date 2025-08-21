

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
import androidx.compose.ui.res.stringResource // <-- i18n
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.presentation.commons.UiText
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearContactoScreen(
    navController: NavController,
    viewModel: ContactoViewModel = hiltViewModel()
) {
    // Contexto y eventos del VM (mostramos toasts localizados)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { uiText: UiText ->
            Toast.makeText(context, uiText.asString(context), Toast.LENGTH_SHORT).show()
        }
    }

    // Estado de subida de imagen
    val isUploading by viewModel.isUploading

    // Estados del formulario
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Cliente") } // guardamos valor de dominio
    var fotoUrl by remember { mutableStateOf<String?>(null) }

    // ---------- Selector de imagen (galería)
    val galeriaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Subir imagen y guardar URL
            viewModel.subirImagen(context, it, UUID.randomUUID().toString()) { url ->
                fotoUrl = url
            }
        }
    }

    // ---------- Importar desde agenda (CONTACTS)
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

    // Permiso de contactos
    val requestContactsPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            pickPhoneLauncher.launch(intent)
        } else {
            Toast.makeText(context,context.getString(R.string.contacts_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.contacts_create_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------- Imagen del contacto (CD localizada)
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
                        contentDescription = stringResource(R.string.contacts_photo_cd_generic),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = stringResource(R.string.contacts_photo_add)
                    )
                }
            }

            // ---------- Campos (localizados)
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(stringResource(R.string.contacts_field_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text(stringResource(R.string.contacts_field_phone)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text(stringResource(R.string.contacts_field_address)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.contacts_field_email)) },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Tipo Cliente/Proveedor (etiquetas localizadas)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RadioButton(selected = tipo == "Cliente", onClick = { tipo = "Cliente" })
                Text(stringResource(R.string.contact_type_client))
                RadioButton(selected = tipo == "Proveedor", onClick = { tipo = "Proveedor" })
                Text(stringResource(R.string.contact_type_supplier))
            }

            // ---------- Guardar (sin toast local: lo emite el VM)
            Button(
                onClick = {
                    val nuevoContacto = Contacto(
                        id = UUID.randomUUID().toString(),
                        nombre = nombre,
                        telefono = telefono,
                        direccion = direccion,
                        email = email,
                        tipo = tipo,        // guardamos valor de dominio "Cliente"/"Proveedor"
                        fotoUrl = fotoUrl
                    )
                    viewModel.onAddContacto(context, nuevoContacto)
                    navController.popBackStack()
                },
                enabled = nombre.isNotBlank() && telefono.isNotBlank() && !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.contacts_save)) }

            // ---------- Importar de la agenda
            OutlinedButton(
                onClick = {
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
            ) { Text(stringResource(R.string.contacts_import_from_phonebook)) }
        }
    }
}

/** Busca un email por CONTACT_ID en la agenda; devuelve el primero o null. */
private fun cargarEmailPorContactId(context: Context, contactId: String): String? {
    val projection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
    val selection = "${ContactsContract.CommonDataKinds.Email.CONTACT_ID}=?"
    val selectionArgs = arrayOf(contactId)

    context.contentResolver.query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
        projection, selection, selectionArgs, null
    )?.use { cursor ->
        if (cursor.moveToFirst()) return cursor.getString(0)
    }
    return null
}





