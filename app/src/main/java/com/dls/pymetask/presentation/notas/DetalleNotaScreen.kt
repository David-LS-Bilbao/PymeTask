package com.dls.pymetask.presentation.notas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Nota

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleNotaScreen(
    navController: NavController,
    notaId: String,
    viewModel: NotaViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(notaId) {
        viewModel.seleccionarNota(notaId)
    }

    val nota = viewModel.notaActual

    nota?.let { notaData ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(notaData.titulo) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("nota_form?notaId=${notaData.id}")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar nota")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = notaData.contenido,
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // WhatsApp
                    IconButton(onClick = {
                        val uri = Uri.parse("https://wa.me/?text=${Uri.encode(notaData.contenido)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_whatsapp),
                            contentDescription = "WhatsApp"
                        )
                    }

                    // Email
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_SUBJECT, notaData.titulo)
                            putExtra(Intent.EXTRA_TEXT, notaData.contenido)
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Email, contentDescription = "Email")
                    }

                    // SMS
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("sms:")
                            putExtra("sms_body", notaData.contenido)
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Message, contentDescription = "SMS")
                    }
                }
            }
        }
    }
}