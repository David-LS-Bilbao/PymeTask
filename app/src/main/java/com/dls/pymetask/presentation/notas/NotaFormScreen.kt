package com.dls.pymetask.presentation.notas

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavBackStackEntry
import com.dls.pymetask.domain.model.Nota
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaFormScreen(
    navController: NavController,
    notaId: String? = null,
    viewModel: NotaViewModel = hiltViewModel()
) {
    val notaActual = viewModel.notaActual

    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    var backgroundColor by remember { mutableStateOf(Color(0xFFFFF9C4)) }

    val coloresDisponibles = listOf("#FFF9C4", "#C8E6C9", "#BBDEFB", "#FFCDD2", "#FFE0B2")

    var mostrarSelectorColor by remember { mutableStateOf(false) }

    // Historial de cambios
    val tituloUndoStack = remember { mutableStateListOf<String>() }
    val tituloRedoStack = remember { mutableStateListOf<String>() }

    val contenidoUndoStack = remember { mutableStateListOf<String>() }
    val contenidoRedoStack = remember { mutableStateListOf<String>() }

    LaunchedEffect(notaId) {
        if (notaId != null) {
            viewModel.seleccionarNota(notaId)
        } else {
            viewModel.limpiarNotaActual()
        }
    }

    LaunchedEffect(notaActual) {
        notaActual?.let { nota ->
            if (titulo.isBlank() && contenido.isBlank()) {
                titulo = nota.titulo
                contenido = nota.contenido
                backgroundColor = try {
                    Color(android.graphics.Color.parseColor(nota.colorHex))
                } catch (e: Exception) {
                    Color(0xFFFFF9C4)
                }
                // Historial inicial
                tituloUndoStack.clear()
                contenidoUndoStack.clear()
                tituloUndoStack.add(titulo)
                contenidoUndoStack.add(contenido)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = titulo.ifBlank { "Nueva nota" }) },
                actions = {
                    IconButton(onClick = { mostrarSelectorColor = !mostrarSelectorColor }) {
                        Icon(Icons.Default.Palette, contentDescription = "Cambiar color")
                    }
                    IconButton(
                        enabled = tituloUndoStack.size > 1 || contenidoUndoStack.size > 1,
                        onClick = {
                            // Deshacer
                            if (tituloUndoStack.size > 1) {
                                val last = tituloUndoStack.removeAt(tituloUndoStack.lastIndex)
                                tituloRedoStack.add(last)
                                titulo = tituloUndoStack.last()
                            }
                            if (contenidoUndoStack.size > 1) {
                                val last = contenidoUndoStack.removeAt(contenidoUndoStack.lastIndex)
                                contenidoRedoStack.add(last)
                                contenido = contenidoUndoStack.last()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Deshacer")
                    }
                    IconButton(
                        enabled = tituloRedoStack.isNotEmpty() || contenidoRedoStack.isNotEmpty(),
                        onClick = {
                            // Rehacer
                            if (tituloRedoStack.isNotEmpty()) {
                                val next = tituloRedoStack.removeAt(tituloRedoStack.lastIndex)
                                tituloUndoStack.add(next)
                                titulo = next
                            }
                            if (contenidoRedoStack.isNotEmpty()) {
                                val next = contenidoRedoStack.removeAt(contenidoRedoStack.lastIndex)
                                contenidoUndoStack.add(next)
                                contenido = next
                            }
                        }
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "Rehacer")
                    }
                    IconButton(onClick = {
                        viewModel.guardarNota(
                            Nota(
                                id = notaId ?: UUID.randomUUID().toString(),
                                titulo = titulo,
                                contenido = contenido,
                                fecha = System.currentTimeMillis(),
                                colorHex = "#%02x%02x%02x".format(
                                    (backgroundColor.red * 255).toInt(),
                                    (backgroundColor.green * 255).toInt(),
                                    (backgroundColor.blue * 255).toInt()
                                )
                            )
                        )
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
                    }
                }
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    if (titulo != it) {
                        tituloUndoStack.add(it)
                        tituloRedoStack.clear()
                        titulo = it
                    }
                },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = contenido,
                onValueChange = {
                    if (contenido != it) {
                        contenidoUndoStack.add(it)
                        contenidoRedoStack.clear()
                        contenido = it
                    }
                },
                label = { Text("Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = Int.MAX_VALUE
            )

            if (mostrarSelectorColor) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Color de fondo", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    coloresDisponibles.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Surface(
                            shape = CircleShape,
                            color = color,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { backgroundColor = color }
                                .border(
                                    width = 2.dp,
                                    color = if (color == backgroundColor) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {}
                    }
                }
            }
        }
    }
}


