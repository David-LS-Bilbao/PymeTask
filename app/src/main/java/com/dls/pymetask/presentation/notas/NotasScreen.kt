package com.dls.pymetask.presentation.notas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Nota

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    viewModel: NotaViewModel = hiltViewModel(),
    navController: NavController
) {
    val notas by remember { derivedStateOf { viewModel.notas } }

    LaunchedEffect(Unit) {
        viewModel.cargarNotas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("nota_form")
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva Nota")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(notas) { nota ->
                NotaListItem(nota = nota) {
                    navController.navigate("detalle_nota?notaId=${nota.id}")
                }
            }
        }
    }
}

@Composable
fun NotaListItem(nota: Nota, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(nota.titulo) },
        supportingContent = { Text(nota.contenido.take(50)) },
        modifier = Modifier.clickable { onClick() }
    )
}