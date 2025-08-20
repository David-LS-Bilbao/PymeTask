package com.dls.pymetask.presentation.movimientos


import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.presentation.components.FechaSelector
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto
import java.util.Date
import java.util.UUID


@SuppressLint("DefaultLocale")
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
    var cantidadFocus by remember { mutableStateOf(false) }

    var fechaSeleccionada by remember { mutableStateOf<Date?>(null) }



    // 👉 Mostrar DatePickerDialog al pulsar el botón
    val context = LocalContext.current
    remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val calendar = java.util.Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                fechaSeleccionada = calendar.time
            },
            java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
            java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
            java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
        )
    }


    FechaSelector(
        context = context,
        onFechaSeleccionada = { fechaSeleccionada = it }
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nuevo movimiento",
                        fontFamily = Poppins,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Atrás")
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
        ) {     // 🗓️ BOTÓN DE SELECCIÓN DE FECHA
            FechaSelector(
                context = context,
                fechaInicial = fechaSeleccionada,
                onFechaSeleccionada = { fechaSeleccionada = it }
            )
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                maxLines = 1,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = subtitulo,
                onValueChange = { subtitulo = it },
                maxLines = 1,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cantidad,
                onValueChange = {
                    cantidad = it
                },
                label = { Text("Cantidad (€)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        cantidadFocus = focusState.isFocused
                        if (!cantidadFocus) {
                            val valor = cantidad.replace(",", ".").toDoubleOrNull()
                            if (valor != null) {
                                cantidad = String.format("%.2f", valor)
                            }
                        }
                    },
                maxLines = 1,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                )
            )



                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {


                    // Botones de ingreso / gasto
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

                // 🧾 GUARDAR MOVIMIENTO
                Button(
                    onClick = {
//                        val fechaFinal = fechaSeleccionada ?: Date()
//                        val timestamp = com.google.firebase.Timestamp(fechaFinal)

                        val userIdSeguro = com.dls.pymetask.utils.Constants.getUserIdSeguro(context) ?: ""
                        val nuevo = Movimiento(
                            id = UUID.randomUUID().toString(),
                            titulo = titulo,
                            subtitulo = subtitulo,
                            cantidad = cantidad.toDoubleOrNull() ?: 0.0,
                            ingreso = tipoIngreso,
                            fecha = com.google.firebase.Timestamp(fechaSeleccionada ?: Date()).toDate().time,
                            userId = userIdSeguro
                        )




//                        val nuevo = Movimiento(
//                            id = UUID.randomUUID().toString(),
//                            titulo = titulo,
//                            subtitulo = subtitulo,
//                            cantidad = cantidad.toDoubleOrNull() ?: 0.0,
//                            ingreso = tipoIngreso,
//                            fecha = com.google.firebase.Timestamp(fechaSeleccionada ?: Date())
//                                .toDate().time,
//                            userId = userId
//                        )
                        viewModel.addMovimiento(nuevo)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar", fontFamily = Roboto)
                }
            }
        }
    }
