package com.dls.pymetask.presentation.notas

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.utils.Constants
import com.dls.pymetask.utils.MyFab
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    viewModel: NotaViewModel = hiltViewModel(),
    navController: NavController,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val notas by viewModel.notas.collectAsState()


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargarNotas()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },

        //MyFab
        floatingActionButton = {
            MyFab.Default(
                onClick = { navController.navigate("nota_form")}
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (notas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay notas aún", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(notas, key = { it.id }) { nota ->
                        NotaCard(nota = nota) {
                            viewModel.seleccionarNota(nota.id)
                            navController.navigate("nota_form?notaId=${nota.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotaCard(nota: Nota, elevation: Dp = 4.dp, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val safeColor = try {
        Color(nota.colorHex.ifBlank { "#FFF9C4" }.toColorInt())
    } catch (e: IllegalArgumentException) {
        Color(0xFFFFF9C4)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = elevation,
        color = safeColor,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = nota.titulo,
            color = Color.Black,
                style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = nota.contenido.take(40),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
                maxLines = 2
            )
        }
    }
}




