// PerfilUserScreen.kt
package com.dls.pymetask.presentation.perfil

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import com.dls.pymetask.ui.theme.Poppins



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUserScreen(navController: NavController, viewModel: PerfilUserViewModel = hiltViewModel()) {
    val perfil by viewModel.perfil.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Atrás")
                    }
                },
                title = { Text("Mi Perfil") },
                actions = {
                    IconButton(onClick = { navController.navigate("editar_perfil") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (perfil.fotoUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(perfil.fotoUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Icono perfil",
                    modifier = Modifier.size(120.dp),
                    tint = Color.Gray
                )
            }

            Text(text = perfil.nombre, style = MaterialTheme.typography.titleLarge)
            Text(text = perfil.email, style = MaterialTheme.typography.bodyMedium)
            Text(text = perfil.telefono, style = MaterialTheme.typography.bodyMedium)
            Text(text = perfil.direccion, style = MaterialTheme.typography.bodyMedium)

        }
    }
}

