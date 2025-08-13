
package com.dls.pymetask.presentation.notas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.utils.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import android.graphics.Color as AColor

@SuppressLint("SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotaFormScreen(
    navController: NavController,
    notaId: String? = null,
    viewModel: NotaViewModel = hiltViewModel()
) {
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density) // alto actual del teclado en px
    val gapPx = with(density) { 8.dp.roundToPx() }        // margen visible sobre el teclado

    // ... dentro de NotaFormScreen
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }                  // NUEVO
    rememberCoroutineScope()

// altura aprox del overlay para no tapar el campo
    val overlayHeight = 56.dp

    val notaActual = viewModel.notaActual
    // Datos de la nota
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
    val onBg = if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
    val context = LocalContext.current
    val componentActivity = context as? ComponentActivity  // 👈 OJO: ComponentActivity
    var mostrarSelectorColor by remember { mutableStateOf(false) }
    // 📏 Saber si el teclado está visible (IME)
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0
    // 🔎 Preparar "bring-into-view" para el TextField de contenido
    val contenidoBringRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    val useDarkIcons = backgroundColor.luminance() > 0.5f
    val scrolBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    //desactivar modo landscape
    LaunchedEffect(Unit) {
        componentActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    // reactivar modo landscape
    DisposableEffect(Unit) {
        onDispose {
            componentActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }//---------------------------------------------------
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
    SideEffect {
        // Le pedimos al sistema iconos claros u oscuros. La barra es transparente,
        // y se ve TU color porque el Scaffold pinta todo el fondo.

        componentActivity?.enableEdgeToEdge(
            statusBarStyle = if (useDarkIcons)
                SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
            else
                SystemBarStyle.dark(AColor.TRANSPARENT),
            navigationBarStyle = if (useDarkIcons)
                SystemBarStyle.light(AColor.TRANSPARENT, AColor.TRANSPARENT)
            else
                SystemBarStyle.dark(AColor.TRANSPARENT)
        )
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


    LaunchedEffect(isKeyboardOpen, contenido) {
        if (isKeyboardOpen) {
            // Pequeña espera para que el IME estabilice su tamaño
            delay(50)
            contenidoBringRequester.bringIntoView()
        }
    }


    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = backgroundColor,
            contentWindowInsets = WindowInsets.safeDrawing.only(//ignora los insets inferiores en el contenido
                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
            ),
            topBar = {
                TopAppBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    scrollBehavior = scrolBehavior,
                    title = { Text(text = titulo.ifBlank { "Nota" }, color = onBg) },
                    navigationIcon = {
                        IconButton(onClick = {
                            guardarYSalir(
                                context,
                                navController,
                                viewModel,
                                notaId,
                                titulo,
                                contenido,
                                backgroundColor
                            )

                        }) {
                            Icon(
                                Icons.Default.ArrowBackIosNew,
                                contentDescription = "Atras y guardar"
                            )
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
                            //navController.popBackStack()
                            Toast.makeText(context, "Nota guardada", Toast.LENGTH_SHORT).show()

                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Guardar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,           // ✅ deja ver el fondo
                        titleContentColor = onBg,
                        navigationIconContentColor = onBg,
                        actionIconContentColor = onBg
                    )
                )
            },

        ) { inner ->


            // nueva:
            Column(
                modifier = Modifier
                    .padding(
                        start = inner.calculateStartPadding(LocalLayoutDirection.current),
                        end = inner.calculateEndPadding(LocalLayoutDirection.current),
                        top = inner.calculateTopPadding()
                    )
                    .fillMaxSize()
                    .background(backgroundColor)
                    .verticalScroll(scrollState)   // 👈 ahora puede desplazarse
                    .imeNestedScroll()             // 👈 deja que el IME pida scroll
                    .imePadding()                  // 👈 empuja el contenido sobre el teclado
                    .padding(bottom = overlayHeight) // 👈 hueco para el overlay inferior
            ) {
                // ... (tu selector de color y el OutlinedTextField de título igual)
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
                    label = { Text("Contenido", color = Color.Black) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)                // 👈 evita forzar toda la altura; deja espacio real de scroll
                        .bringIntoViewRequester(contenidoBringRequester)
                        .focusRequester(focusRequester)
                        .onFocusEvent { f ->
                            if (f.isFocused) {
                                scope.launch {
                                    // espera breve a la animación del IME y trae a vista
                                    delay(150)
                                    contenidoBringRequester.bringIntoView()
                                }
                            }
                        },
                    maxLines = Int.MAX_VALUE
                )
            }










//            Column(
//                modifier = Modifier
//                    .padding(
//                        start = inner.calculateStartPadding(LocalLayoutDirection.current),
//                        end = inner.calculateEndPadding(LocalLayoutDirection.current),
//                        top = inner.calculateTopPadding()
//                    )
//                  //  .padding(4.dp) // Aumenta el padding de la columna
//                    .fillMaxSize()
//                    .background(backgroundColor)
//            ) {
//                // boton color
//                if (mostrarSelectorColor) {
//                    Spacer(modifier = Modifier.height(12.dp))
//                    Text("Color de fondo", style = MaterialTheme.typography.labelSmall)
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceEvenly
//                    ) {
//                        coloresDisponibles.forEach { (_, hex) ->
//                            val color = Color(hex.toColorInt())
//                            Surface(
//                                shape = CircleShape,
//                                color = color,
//                                modifier = Modifier
//                                    .size(36.dp)
//                                    .clickable { backgroundColor = color }
//                                    .border(
//                                        width = 2.dp,
//                                        color = if (color == backgroundColor) Color.Black else Color.Transparent,
//                                        shape = CircleShape
//                                    )
//                            ) {}
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.height(6.dp))
//                // titulo
//                OutlinedTextField(
//                    value = titulo,
//                    onValueChange = {
//                        if (titulo != it) {
//                            tituloUndoStack.add(it)
//                            tituloRedoStack.clear()
//                            titulo = it
//                        }
//                    },
//                    label = { Text("Título") },
//                    singleLine = true,
//                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(12.dp))
//                // Cuadro texto para contenido
//                OutlinedTextField(
//                    value = contenido,
//                    onValueChange = {
//                        if (contenido != it) {
//                            contenidoUndoStack.add(it)
//                            contenidoRedoStack.clear()
//                            contenido = it
//                        }
//                    },
//                    label = { Text("Contenido", color = Color.Black) },
//                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f)// mantiene altura flexible; el TextField ya hace scroll interno
//                        .bringIntoViewRequester(contenidoBringRequester)   // 👈 preparado
//                        .onFocusEvent { focus ->
//                            if (focus.isFocused) {
//                                // Pequeño retardo para esperar la animación del teclado y luego traer a vista
//                                scope.launch {
//                                    delay(200)
//                                    contenidoBringRequester.bringIntoView()
//                                }
//                            }
//                        },
//                    maxLines = Int.MAX_VALUE
//                )
//            }
            // Diálogo de confirmación para borrar
            if (mostrarConfirmacionBorrado) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmacionBorrado = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val idNota = notaId ?: viewModel.notaActual?.id
                            if (idNota != null) {
                                viewModel.eliminarNotaPorId(idNota)
                                Toast.makeText(context, "Nota eliminada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error: nota no encontrada",
                                    Toast.LENGTH_SHORT
                                ).show()
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
        //  Overlay: solo esta fila “sube”


        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding() // base cuando NO hay teclado
                .offset {                 // sube exactamente hasta el IME
                    val lift = (imeBottomPx - gapPx).coerceAtLeast(0)
                    IntOffset(0, -lift)
                }
                .padding(horizontal = 12.dp, vertical = 4.dp), // menos altura extra
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IconButtons compactos (sin touch target forzado a 48dp)
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
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
                    },
                    modifier = Modifier.size(36.dp) // ↓ botón más bajo
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Deshacer",
                        modifier = Modifier.size(20.dp) // ↓ icono más compacto
                    )
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
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Rehacer",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
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






