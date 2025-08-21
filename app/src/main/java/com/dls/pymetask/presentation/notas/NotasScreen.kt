package com.dls.pymetask.presentation.notas

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.utils.MyFab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotasScreen(
    viewModel: NotaViewModel = hiltViewModel(),
    navController: NavController,
) {
    val lifecycleOwner = LocalLifecycleOwner.current // Obtiene el propietario del ciclo de vida actual ( escuchador de eventos)
    val notas by viewModel.notas.collectAsState()  // Lista de notas del VM
    val isLoading by viewModel.isLoading // variable de carga del VM


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
                title = { Text(stringResource(R.string.notes_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)) // "Volver"
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


            when {// Loading
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                notas.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.notes_empty), color = Color.Gray)  // "No hay notas aún"
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        items(notas, key = { it.id }) { nota ->
                            NotaCard(nota = nota) {
                                viewModel.seleccionarNota(nota.id)
                                navController.navigate("nota_form?notaId=${nota.id}")
                                Log.d("NotasScreen", "Nota seleccionada: ${nota.id}")
                            }
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




