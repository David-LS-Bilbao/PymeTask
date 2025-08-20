
package com.dls.pymetask.presentation.movimientos

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.presentation.components.FechaSelector
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto

@SuppressLint("DefaultLocale")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarMovimientoScreen(
    movimientoId: String?,
    navController: NavController,
    viewModel: MovimientosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val movimientos by viewModel.movimientos.collectAsState()
    val movimiento = movimientos.find { it.id == movimientoId }
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (movimientoId != null && movimiento == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Campos de edición
    var titulo by remember { mutableStateOf(movimiento?.titulo.orEmpty()) }
    var subtitulo by remember { mutableStateOf(movimiento?.subtitulo.orEmpty()) }
    var cantidad by remember { mutableStateOf(movimiento?.cantidad?.let { "%.2f".format(it) }.orEmpty()) }
    var tipoIngreso by remember { mutableStateOf(movimiento?.ingreso ?: true) }
    var cantidadFocus by remember { mutableStateOf(false) }
    var (fechaSeleccionada, _) = remember { mutableStateOf(movimiento?.fecha?.let { java.util.Date(it) }) }

    fun guardarMovimientoYVolver() {
        val nuevaCantidad = cantidad.replace(",", ".").toDoubleOrNull() ?: movimiento?.cantidad ?: 0.0
        val fechaFinal = (fechaSeleccionada ?: movimiento?.fecha?.let { java.util.Date(it) })?.time ?: System.currentTimeMillis()

        movimiento?.copy(
            titulo = titulo,
            subtitulo = subtitulo,
            cantidad = nuevaCantidad,
            ingreso = tipoIngreso,
            fecha = fechaFinal
        )?.let {
            viewModel.updateMovimiento(it)
            Toast.makeText(context, "Movimiento actualizado", Toast.LENGTH_SHORT).show()
        }

        navController.navigate("movimientos") {
            popUpTo("movimientos") { inclusive = true }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar movimiento", fontFamily = Poppins, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                        if (!navController.popBackStack()) {
                            navController.navigate("movimientos") {
                                popUpTo("movimientos") { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(Icons.Filled.Home, contentDescription = "Inicio")
                    }
                    if (movimiento != null) {
                        IconButton(onClick = { showConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
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
            FechaSelector(
                context = context,
                fechaInicial = fechaSeleccionada,
                onFechaSeleccionada = { fechaSeleccionada = it }
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = subtitulo,
                onValueChange = { subtitulo = it },
                label = { Text("Descripción / Cliente / Proveedor") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad (€)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { it ->
                        cantidadFocus = it.isFocused
                        if (!cantidadFocus) {
                            cantidad = cantidad.replace(",", ".").toDoubleOrNull()?.let {
                                "%.2f".format(it)
                            } ?: cantidad
                        }
                    },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { tipoIngreso = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipoIngreso) Color(0xFF1976D2) else Color.LightGray
                    )
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ingreso")
                }

                Button(
                    onClick = { tipoIngreso = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!tipoIngreso) Color(0xFF1976D2) else Color.LightGray
                    )
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gasto")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { guardarMovimientoYVolver() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar", fontFamily = Roboto)
            }
        }
    }

    if (showConfirmDialog && movimiento != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteMovimiento(movimiento.id)
                    Toast.makeText(context, "Movimiento eliminado", Toast.LENGTH_SHORT).show()
                    showConfirmDialog = false
                    navController.navigate("movimientos") {
                        popUpTo("movimientos") { inclusive = true }
                    }
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar este movimiento?") }
        )
    }

    BackHandler {
        guardarMovimientoYVolver()
    }
}





//package com.dls.pymetask.presentation.movimientos
//
//import android.annotation.SuppressLint
//import android.os.Build
//import android.widget.Toast
//import androidx.activity.compose.BackHandler
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowDownward
//import androidx.compose.material.icons.filled.ArrowUpward
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//
//import com.dls.pymetask.presentation.components.FechaSelector
//import com.dls.pymetask.ui.theme.Poppins
//import com.dls.pymetask.ui.theme.Roboto
//
//
//@SuppressLint("DefaultLocale")
//@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EditarMovimientoScreen(
//    movimientoId: String?,
//    navController: NavController,
//    viewModel: MovimientosViewModel = hiltViewModel()
//) {
//    val context = LocalContext.current
//    val movimientos by viewModel.movimientos.collectAsState()
//    val movimiento = movimientos.find { it.id == movimientoId }
//    var showConfirmDialog by remember { mutableStateOf(false) }
//    var cantidadEditadoManualmente by remember { mutableStateOf(false) }
//    var cantidadFocus by remember { mutableStateOf(false) }
//
//    if (movimientoId != null && movimiento == null) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            CircularProgressIndicator()
//        }
//        return
//    }
//
//    var titulo by remember { mutableStateOf(movimiento?.titulo.orEmpty()) }
//    var subtitulo by remember { mutableStateOf(movimiento?.subtitulo.orEmpty()) }
//    var cantidad by remember { mutableStateOf(movimiento?.cantidad?.let { String.format("%.2f", it) }.orEmpty()) }
//    var tipoIngreso by remember { mutableStateOf(movimiento?.ingreso ?: true) }
//
//    var fechaSeleccionada by remember { mutableStateOf(movimiento?.fecha?.toDate()) }
//
//    LaunchedEffect(movimientoId) {
//        if (movimiento != null) {
//            titulo = movimiento.titulo
//            subtitulo = movimiento.subtitulo
//            cantidad = String.format("%.2f", movimiento.cantidad)
//            tipoIngreso = movimiento.ingreso
//            cantidadEditadoManualmente = false
//        }
//    }
//
//    if (showConfirmDialog && movimiento != null) {
//        AlertDialog(
//            onDismissRequest = { showConfirmDialog = false },
//            confirmButton = {
//                Button(onClick = {
//                    viewModel.deleteMovimiento(movimiento.id)
//                    Toast.makeText(context, "Movimiento eliminado", Toast.LENGTH_SHORT).show()
//                    showConfirmDialog = false
//                    navController.navigate("movimientos") {
//                        popUpTo("movimientos") { inclusive = true }
//                    }
//                }) {
//                    Text("Eliminar")
//                }
//            },
//            dismissButton = {
//                Button(onClick = { showConfirmDialog = false }) {
//                    Text("Cancelar")
//                }
//            },
//            title = { Text("Confirmar eliminación") },
//            text = { Text("¿Estás seguro de que deseas eliminar este movimiento?") }
//        )
//    }
//
//    fun guardarMovimientoYVolver() {
//        val nuevaCantidad = cantidad.toDoubleOrNull()
//            ?: movimiento?.cantidad
//            ?: 0.0
//
//        if (movimiento != null) {
//            val actualizado = movimiento.copy(
//                titulo = titulo,
//                subtitulo = subtitulo,
//                cantidad = nuevaCantidad,
//                ingreso = tipoIngreso,
//                fecha = com.google.firebase.Timestamp(fechaSeleccionada ?: movimiento.fecha.toDate())
//            )
//            viewModel.updateMovimiento(actualizado)
//            Toast.makeText(context, "Movimiento actualizado", Toast.LENGTH_SHORT).show()
//        }
//
//        navController.navigate("movimientos") {
//            popUpTo("movimientos") { inclusive = true }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text("Editar movimiento", fontFamily = Poppins, fontSize = 20.sp)
//                },
//
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack()
//                    val popped = navController.popBackStack()
//                        if (!popped) {
//                            navController.navigate("movimientos") {
//                                popUpTo("movimientos") { inclusive = true }
//                            }
//                        }
//                    }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
//                    }
//                },
//
//                actions = {
//                    IconButton(onClick = { navController.navigate("dashboard") }) {
//                        Icon(Icons.Filled.Home, contentDescription = "Inicio")
//                    }
//                    if (movimiento != null) {
//                        IconButton(onClick = { showConfirmDialog = true }) {
//                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
//                        }
//                    }
//                }
//            )
//        }
//    ) { padding ->
//
//
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {    FechaSelector(
//            context = context,
//            fechaInicial = fechaSeleccionada,
//            onFechaSeleccionada = { fechaSeleccionada = it }
//        )
//
//
//            // Campos de edición de movimiento
//
//            OutlinedTextField(
//                value = titulo,
//                onValueChange = { titulo = it },
//                label = { Text("Título") },
//                maxLines = 1,
//                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            OutlinedTextField(
//                value = subtitulo,
//                onValueChange = { subtitulo = it },
//                label = { Text("Descripción / Cliente / Proveedor") },
//                maxLines = 1,
//                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            OutlinedTextField(
//                value = cantidad,
//                onValueChange = {
//                    // Solo se actualiza lo que el usuario escribe
//                    cantidad = it
//                },
//                label = { Text("Cantidad (€)") },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .onFocusChanged { focusState ->
//                        cantidadFocus = focusState.isFocused
//                        if (!cantidadFocus) {
//                            // Cuando se pierde el foco, formateamos a dos decimales
//                            val valor = cantidad.replace(",", ".").toDoubleOrNull()
//                            if (valor != null) {
//                                cantidad = String.format("%.2f", valor)
//                            }
//                        }
//                },
//                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal,imeAction = ImeAction.Done),
//                maxLines = 1,
//
//            )
//
//            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                Button(
//                    onClick = { tipoIngreso = true },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (tipoIngreso) Color(0xFF1976D2) else Color.LightGray
//                    )
//                ) {
//                    Icon(Icons.Default.ArrowDownward, contentDescription = null)
//                    Spacer(Modifier.width(8.dp))
//                    Text("Ingreso")
//                }
//
//                Button(
//                    onClick = { tipoIngreso = false },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (!tipoIngreso) Color(0xFF1976D2) else Color.LightGray
//                    )
//                ) {
//                    Icon(Icons.Default.ArrowUpward, contentDescription = null)
//                    Spacer(Modifier.width(8.dp))
//                    Text("Gasto")
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Botón para guardar los cambios
//            Button(
//                onClick = { guardarMovimientoYVolver() },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Guardar", fontFamily = Roboto)
//            }
//        }
//    }
//    BackHandler {
//        guardarMovimientoYVolver()
//    }
//}








