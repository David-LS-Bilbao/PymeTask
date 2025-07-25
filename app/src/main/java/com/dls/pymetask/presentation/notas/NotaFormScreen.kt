package com.dls.pymetask.presentation.notas

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.utils.Constants
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotaFormScreen(
    navController: NavController,
    notaId: String? = null,
    viewModel: NotaViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val notaActual = viewModel.notaActual

    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    var backgroundColor by remember { mutableStateOf(Color(0xFFFFF9C4)) }

    // Menú de envío desplegable
    var mostrarMenuEnvio by remember { mutableStateOf(false) }

    // Diálogo de confirmación para eliminar
    var mostrarConfirmacionBorrado by remember { mutableStateOf(false) }

    // Historial para deshacer y rehacer
    val tituloUndoStack = remember { mutableStateListOf<String>() }
    val tituloRedoStack = remember { mutableStateListOf<String>() }
    val contenidoUndoStack = remember { mutableStateListOf<String>() }
    val contenidoRedoStack = remember { mutableStateListOf<String>() }

    // Selector de color
    val coloresDisponibles = Constants.coloresDisponibles

    var mostrarSelectorColor by remember { mutableStateOf(false) }

    // Carga de nota si notaId está presente
    LaunchedEffect(notaId) {
        if (notaId != null) {
            viewModel.seleccionarNota(notaId)
        } else {
            viewModel.limpiarNotaActual()
        }
    }

    // Copia los datos de la nota seleccionada al formulario
    LaunchedEffect(notaActual) {
        notaActual?.let { nota ->
            if (titulo.isBlank() && contenido.isBlank()) {
                titulo = nota.titulo
                contenido = nota.contenido
                backgroundColor = try {
                    Color(nota.colorHex.toColorInt())
                } catch (_: Exception) {
                    Color(0xFFFFF9C4)
                }
                // Inicia historial
                tituloUndoStack.clear(); tituloRedoStack.clear()
                contenidoUndoStack.clear(); contenidoRedoStack.clear()
                tituloUndoStack.add(titulo)
                contenidoUndoStack.add(contenido)
            }
        }
    }

    // Guardado al pulsar atrás físico
    BackHandler {
        guardarYSalir(
            context = context,
            navController = navController,
            viewModel = viewModel,
            notaId = notaId,
            titulo = titulo,
            contenido = contenido,
            color = backgroundColor
        )
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = titulo.ifBlank { "Nota" }) },
                navigationIcon = {
                    IconButton(onClick = {
                        guardarYSalir(context, navController, viewModel, notaId, titulo, contenido, backgroundColor)

                    }){
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Atras")
                    }
                },
                actions = {
                    // Menú desplegable de envío
                    Box {
                        IconButton(onClick = { mostrarMenuEnvio = true }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                        }
                        DropdownMenu(
                            expanded = mostrarMenuEnvio,
                            onDismissRequest = { mostrarMenuEnvio = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("WhatsApp") },
                                onClick = {
                                    compartirNota(context, titulo, contenido, "whatsapp")
                                    mostrarMenuEnvio = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Email") },
                                onClick = {
                                    compartirNota(context, titulo, contenido, "email")
                                    mostrarMenuEnvio = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("SMS") },
                                onClick = {
                                    compartirNota(context, titulo, contenido, "sms")
                                    mostrarMenuEnvio = false
                                }
                            )
                        }
                    }

                    // Botón borrar
                    IconButton(onClick = { mostrarConfirmacionBorrado = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar nota")
                    }

                    // Selector color
                    IconButton(onClick = { mostrarSelectorColor = !mostrarSelectorColor }) {
                        Icon(Icons.Default.Palette, contentDescription = "Color")
                    }
                }
            )
        },
        bottomBar = {
            // Barra inferior con botones Undo / Redo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    enabled = tituloUndoStack.size > 1 || contenidoUndoStack.size > 1,
                    onClick = {
                        if (tituloUndoStack.size > 1) {
                            tituloRedoStack.add(tituloUndoStack.removeAt(tituloUndoStack.lastIndex))
                            titulo = tituloUndoStack.last()
                        }
                        if (contenidoUndoStack.size > 1) {
                            contenidoRedoStack.add(contenidoUndoStack.removeAt(contenidoUndoStack.lastIndex))
                            contenido = contenidoUndoStack.last()
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Deshacer")
                }

                IconButton(
                    enabled = tituloRedoStack.isNotEmpty() || contenidoRedoStack.isNotEmpty(),
                    onClick = {
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
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Rehacer")
                }

                // Botón guardar
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
                    coloresDisponibles.forEach { (_,hex) ->
                        val color = Color(hex.toColorInt())
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

        // Diálogo de confirmación para borrar
        if (mostrarConfirmacionBorrado) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmacionBorrado = false },
                confirmButton = {
                    TextButton(onClick = {
//                        notaActual?.let {
//                            viewModel.eliminarNota(it)
//                        }
//                        mostrarConfirmacionBorrado = false
//                        navController.popBackStack()
                        val idNota = notaId ?: viewModel.notaActual?.id
                        if (idNota != null) {
                            viewModel.eliminarNotaPorId(idNota)
                            Toast.makeText(context, "Nota eliminada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error: nota no encontrada", Toast.LENGTH_SHORT).show()
                        }
                        mostrarConfirmacionBorrado = false
                        navController.popBackStack()

                    }) {
                        Text("Sí, borrar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmacionBorrado = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("¿Eliminar nota?") },
                text = { Text("Esta acción no se puede deshacer.") }
            )
        }
    }
}

// Funciones para compartir nota---------------------------------------------------------------------

fun compartirNota(context: Context, titulo: String, contenido: String, via: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_SUBJECT, titulo)
        putExtra(Intent.EXTRA_TEXT, contenido)
        type = "text/plain"
    }

    when (via) {
        "whatsapp" -> intent.setPackage("com.whatsapp")
        "email" -> intent.type = "message/rfc822"
        "sms" -> intent.setPackage("com.android.mms")
    }

    val chooser = Intent.createChooser(intent, "Compartir nota con...")
    context.startActivity(chooser)
}


// Guardado con Toast y salida----------------------------------------------------------------------
fun guardarYSalir(
    context: Context,
    navController: NavController,
    viewModel: NotaViewModel,
    notaId: String?,
    titulo: String,
    contenido: String,
    color: Color
) {
    if (titulo.isNotBlank() || contenido.isNotBlank()) {
        viewModel.guardarNota(
            Nota(
                id = notaId ?: UUID.randomUUID().toString(),
                titulo = titulo,
                contenido = contenido,
                fecha = System.currentTimeMillis(),
                colorHex = "#%02x%02x%02x".format(
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                )
            )
        )
        Toast.makeText(context, "Nota guardada", Toast.LENGTH_SHORT).show()
    }
    navController.popBackStack()
}






