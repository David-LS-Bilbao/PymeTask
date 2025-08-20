
// ADAPTADO: DetalleContactoScreen.kt
package com.dls.pymetask.presentation.contactos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dls.pymetask.R

@SuppressLint("SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleContactoScreen(
    navController: NavController,
    contactoId: String,
    viewModel: ContactoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val contactos = viewModel.contactos
    val contacto = contactos.find { it.id == contactoId }

    // desactivar modo landscape
    val activity = context as? Activity
    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // 🔹 Lanza la carga de contactos de Firebase al entrar a la pantalla (snapshot listener)
    LaunchedEffect(Unit) {
        viewModel.getContactos(context)
    }

    // 🔹 Mientras aún no haya llegado ningún contacto, muestra loader centrado
    if (contactos.isEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Contacto") },
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


    if (contacto == null) {
        // texto centrado
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Contacto no encontrado\n\n\n", fontSize = 24.sp)
            Button(onClick = {
                navController.popBackStack() }
            ) { Text("Volver") }
        }

        return
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contacto.nombre) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("editar_contacto/${contacto.id}")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen o inicial
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                if (!contacto.fotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = contacto.fotoUrl,
                        contentDescription = "Foto de contacto",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = contacto.nombre.first().uppercase(),
                        fontSize = 60.sp,
                        color = Color(0xFF1976D2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("📞 ${contacto.telefono}", fontSize = 24.sp)
            Text("✉️ ${contacto.email}", fontSize = 24.sp)
            Text("🏠 ${contacto.direccion}", fontSize = 24.sp)
            Text("Tipo: ${contacto.tipo}", fontSize = 24.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, "tel:${contacto.telefono}".toUri())
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Llamar",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "smsto:${contacto.telefono}".toUri()
                    }
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "SMS",
                        tint = Color(0xFF03A9F4),
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(onClick = {
                    val numero = contacto.telefono.replace(" ", "").replace("-", "")
                    val uri = "https://wa.me/$numero".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_whatsapp),
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(48.dp)
                    )
                }

                IconButton(onClick = {
                    if (contacto.email.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:${contacto.email}".toUri()
                        }
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Este contacto no tiene email", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color(0xFF673AB7),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}


