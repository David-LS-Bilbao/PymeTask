package com.dls.pymetask.presentation.movimientos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarMovimientoScreen(
    movimientoId: String?,
    navController: NavController,
    viewModel: MovimientosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val movimiento = remember(movimientoId) {
        viewModel.getMovimientoById(movimientoId ?: "")
    }

    // Estados del formulario
    var titulo by remember { mutableStateOf(movimiento?.titulo.orEmpty()) }
    var subtitulo by remember { mutableStateOf(movimiento?.subtitulo.orEmpty()) }
    var cantidad by remember { mutableStateOf(movimiento?.cantidad?.toString().orEmpty()) }
    var tipoIngreso by remember { mutableStateOf(movimiento?.ingreso ?: true) }
//    var imageUrl by remember { mutableStateOf(movimiento?.imagenUrl.orEmpty()) }

    LaunchedEffect(movimientoId) {
        if (movimientoId != null) {
            titulo = movimiento?.titulo.orEmpty()
            subtitulo = movimiento?.subtitulo.orEmpty()
            cantidad = movimiento?.cantidad?.toString().orEmpty()
            tipoIngreso = movimiento?.ingreso ?: true
//            imageUrl = movimiento?.imagenUrl.orEmpty()
        }
        else{
            titulo = ""
            subtitulo = ""
            cantidad = ""
            tipoIngreso = true
//            imageUrl = ""
        }

    }


    // Lanzador para seleccionar imagen de galería
//    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        uri?.let {
//            uploadImageToFirebase(context, it) { url ->
//                imageUrl = url
//            }
//        }
//    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${if (movimiento == null) "Nuevo" else "Editar"} movimiento", fontFamily = Poppins, fontSize = 20.sp)
                },
                actions = {
                    if (movimiento != null) {
                        IconButton(onClick = {
                            viewModel.deleteMovimiento(movimiento.id)
                            navController.popBackStack()
                        }) {
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
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = subtitulo,
                onValueChange = { subtitulo = it },
                label = { Text("Descripción / Cliente / Proveedor") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad (€)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
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

//            Button(onClick = { galleryLauncher.launch("image/*") }) {
//                Icon(Icons.Default.Image, contentDescription = null)
//                Spacer(Modifier.width(8.dp))
//                Text(if (imageUrl.isNotBlank()) "Editar imagen" else "Añadir imagen")
//            }

//            if (imageUrl.isNotBlank()) {
//                Image(
//                    painter = rememberAsyncImagePainter(imageUrl),
//                    contentDescription = null,
//                    modifier = Modifier.fillMaxWidth().height(200.dp)
//                )
//            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val nuevaCantidad = cantidad.toDoubleOrNull() ?: 0.0
                    if (movimiento != null) {
                        val actualizado = movimiento.copy(
                            titulo = titulo,
                            subtitulo = subtitulo,
                            cantidad = nuevaCantidad,
                            ingreso = tipoIngreso,
                          //  imagenUrl = imageUrl
                        )
                        viewModel.updateMovimiento(actualizado)
                    } else {
                        val nuevo = Movimiento(
                            id = UUID.randomUUID().toString(),
                            titulo = titulo,
                            subtitulo = subtitulo,
                            cantidad = nuevaCantidad,
                            ingreso = tipoIngreso,
                            fecha = com.google.firebase.Timestamp.now(),
                            //imagenUrl = imageUrl
                        )
                        viewModel.addMovimiento(nuevo)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar", fontFamily = Roboto)
            }
        }
    }

}
//// Función para subir imagen a Firebase Storage
//fun uploadImageToFirebase(context: Context, uri: Uri, onUploaded: (String) -> Unit) {
//    val storageRef = FirebaseStorage.getInstance().reference.child("imagenes/${UUID.randomUUID()}.jpg")
//    storageRef.putFile(uri)
//        .addOnSuccessListener {
//            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
//                onUploaded(downloadUrl.toString())
//            }
//        }
//        .addOnFailureListener {
//            Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
//        }
//}

