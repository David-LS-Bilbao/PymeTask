package com.dls.pymetask.presentation.contactos

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Contacto
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleContactoScreen(
    navController: NavController,
    contactoId: String,
    viewModel: ContactoViewModel = hiltViewModel(),
) {
    val contactos by viewModel.contactos.collectAsState()
    val contacto = contactos.find { it.id == contactoId }
    val context = LocalContext.current

    if (contacto == null) {
        Text("Contacto no encontrado", modifier = Modifier.padding(16.dp))
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contacto.nombre) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        fontSize = 36.sp,
                        color = Color(0xFF1976D2)
                    )
                }
            }

            Text("📞 ${contacto.telefono}")
            Text("✉️ ${contacto.email}")
            Text("🏠 ${contacto.direccion}")
            Text("Tipo: ${contacto.tipo}")

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, "tel:${contacto.telefono}".toUri())
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Phone, contentDescription = "Llamar")
                }

                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "smsto:${contacto.telefono}".toUri()
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Message, contentDescription = "SMS")
                }

                IconButton(onClick = {
                    val numero = contacto.telefono.replace(" ", "").replace("-", "")
                    val uri = "https://wa.me/$numero".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_whatsapp)
                        , contentDescription = "WhatsApp")
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
                    Icon(Icons.Default.Email, contentDescription = "Email")
                }
            }
        }
    }
}
