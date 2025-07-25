package com.dls.pymetask.presentation.perfil

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUserScreen(
    navController: NavController,
    viewModel: PerfilUserViewModel = hiltViewModel()
) {
    val perfil by viewModel.perfil.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("editar_perfil") }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen de perfil o icono por defecto
            if (!perfil.fotoUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(perfil.fotoUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Icono de perfil",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape),
                    tint = Color.Gray
                )
            }

            // Datos del usuario
            Text(
                text = perfil.nombre,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = perfil.email,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = Roboto,
                    color = Color.Gray
                )
            )

            if (perfil.telefono.isNotBlank()) {
                Text(
                    text = "📞 ${perfil.telefono}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                )
            }

            if (perfil.direccion.isNotBlank()) {
                Text(
                    text = "📍 ${perfil.direccion}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
                )
            }
        }
    }
}
