package com.dls.pymetask.presentation.movimientos


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearMovimientoScreen(
    navController: NavController,
    viewModel: MovimientosViewModel = hiltViewModel()
) {
    var titulo by remember { mutableStateOf("") }
    var subtitulo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var tipoIngreso by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nuevo movimiento",
                        fontFamily = Poppins,
                        fontSize = 20.sp
                    )
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {

                        val nuevo = Movimiento(
                            id = UUID.randomUUID().toString(),
                            titulo = titulo,
                            subtitulo = subtitulo,
                            cantidad = cantidad.toDoubleOrNull() ?: 0.0,
                            ingreso = tipoIngreso,
                            fecha = com.google.firebase.Timestamp.now()
                        )
                        viewModel.addMovimiento(nuevo)
                        navController.popBackStack() // volver atrás tras guardar


                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar", fontFamily = Roboto)
            }
        }
    }
}
