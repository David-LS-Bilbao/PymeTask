package com.dls.pymetask.presentation.ajustes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun OpcionesDialog(
    titulo: String,
    opciones: List<String>,
    seleccionada: Int,
    onSeleccionar: (Int) -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = {
            Column {
                opciones.forEachIndexed { index, opcion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = index == seleccionada,
                            onClick = { onSeleccionar(index) }
                        )
                        Text(opcion)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
