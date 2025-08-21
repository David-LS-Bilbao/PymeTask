
package com.dls.pymetask.presentation.perfil

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import com.dls.pymetask.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    onBack: () -> Unit,
    viewModel: EditarPerfilViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val perfil by viewModel.perfil.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imagenUri = uri
    }

    LaunchedEffect(perfil) {
        if (nombre.isBlank()) nombre = perfil.nombre
        if (telefono.isBlank()) telefono = perfil.telefono
        if (direccion.isBlank()) direccion = perfil.direccion
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_edit_title)) },  // "Editar perfil"
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Imagen de perfil
            Box(modifier = Modifier.size(200.dp)) {
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(imagenUri ?: perfil.fotoUrl)
                        .crossfade(true)
                        .build()
                )
                Image(
                    painter = painter,
                    contentDescription = stringResource(R.string.profile_photo_cd), // "Foto de perfil"
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                )
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(R.string.profile_change_image), // "Cambiar imagen"
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { imageLauncher.launch("image/*") }
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp)
                )
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text(stringResource(R.string.profile_full_name)) },  // "Nombre completo"
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text(stringResource(R.string.profile_phone)) },      // "Teléfono"
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text(stringResource(R.string.profile_address)) },    // "Dirección"
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (nombre.isBlank()) {
                        Toast.makeText(context, context.getString(R.string.profile_name_required), Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.actualizarPerfil(
                        nuevoNombre = nombre,
                        nuevoTelefono = telefono,
                        nuevaDireccion = direccion,
                        nuevaFotoUri = imagenUri,
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        onError = {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_save)) // "Guardar"
            }
        }
    }
}



