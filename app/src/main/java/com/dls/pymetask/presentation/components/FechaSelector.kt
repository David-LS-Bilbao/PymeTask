package com.dls.pymetask.presentation.components


import android.app.DatePickerDialog
import android.content.Context
import android.text.format.DateFormat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import java.util.*

@Composable
fun FechaSelector(
    context: Context,
    fechaInicial: Date? = null,
    onFechaSeleccionada: (Date) -> Unit
) {
    var fecha by remember { mutableStateOf(fechaInicial) }

    val datePickerDialog = remember {
        val calendar = Calendar.getInstance().apply {
            time = fechaInicial ?: Date()
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                val nuevaFecha = calendar.time
                fecha = nuevaFecha
                onFechaSeleccionada(nuevaFecha)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Button(
        onClick = { datePickerDialog.show() },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
    ) {
        Text(
            text = if (fecha != null)
                "Fecha: ${DateFormat.format("dd/MM/yyyy", fecha)}"
            else "Seleccionar fecha"
        )
    }
}
