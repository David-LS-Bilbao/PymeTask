package com.dls.pymetask.presentation.notas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Nota
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaFormScreen(
    navController: NavController,
    notaId: String? = null,
    viewModel: NotaViewModel = hiltViewModel()
) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }

    val notaActual = viewModel.notaActual

    LaunchedEffect(notaId) {
        if (notaId != null) {
            viewModel.seleccionarNota(notaId)
        }
    }

    LaunchedEffect(notaActual) {
        notaActual?.let {
            titulo = it.titulo
            contenido = it.contenido
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (notaId == null) "Nueva nota" else "Editar nota")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val nota = Nota(
                            id = notaId ?: UUID.randomUUID().toString(),
                            titulo = titulo,
                            contenido = contenido,
                            fecha = System.currentTimeMillis()
                        )
                        viewModel.guardarNota(nota)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contenido,
                onValueChange = { contenido = it },
                label = { Text("Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                singleLine = false,
                maxLines = 10
            )
        }
    }
}