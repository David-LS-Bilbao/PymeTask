
package com.dls.pymetask.presentation.movimientos

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <-- i18n
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.R
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

    val context = LocalContext.current

    // DatePickerDialog inicial (se mantiene tu lógica)
    remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val calendar = java.util.Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                fechaSeleccionada = calendar.time
            },
            java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
            java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
            java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    // Selector reusable
    FechaSelector(
        context = context,
        onFechaSeleccionada = { fechaSeleccionada = it }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.movement_new_title),
                        fontFamily = Poppins,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.common_back)
                        )
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
            // 🗓️ Fecha
            FechaSelector(
                context = context,
                fechaInicial = fechaSeleccionada,
                onFechaSeleccionada = { fechaSeleccionada = it }
            )

            // Título
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text(stringResource(R.string.movement_title_label)) },
                maxLines = 1,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            // Descripción
            OutlinedTextField(
                value = subtitulo,
                onValueChange = { subtitulo = it },
                maxLines = 1,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                label = { Text(stringResource(R.string.movement_description_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Cantidad
            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text(stringResource(R.string.movement_amount_label)) },
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

            // Ingreso / Gasto
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { tipoIngreso = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (tipoIngreso) Color(0xFF1976D2) else Color.LightGray
                    )
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.movement_income))
                }

                Button(
                    onClick = { tipoIngreso = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!tipoIngreso) Color(0xFF1976D2) else Color.LightGray
                    )
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.movement_expense))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guardar
            Button(
                onClick = {
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
                    viewModel.addMovimiento(nuevo)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_save), fontFamily = Roboto)
            }
        }
    }
}
