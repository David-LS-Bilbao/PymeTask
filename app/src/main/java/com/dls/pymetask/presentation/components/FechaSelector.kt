package com.dls.pymetask.presentation.components


import android.app.DatePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateFormat.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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



    ElevatedButton(

        onClick = { datePickerDialog.show() },
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = Color(0xFFC4CCD9),
            contentColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium

    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = "Seleccionar fecha",
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = fecha?.let { "Fecha: ${format("dd/MM/yyyy", it)}"
            } ?: "Fecha: ${format("dd/MM/yyyy", Date())}",// Muestra la fecha actual si 'fecha' es null
            style = MaterialTheme.typography.bodyMedium,
            // color del texto
            color = Color.Black
        )
    }
}
