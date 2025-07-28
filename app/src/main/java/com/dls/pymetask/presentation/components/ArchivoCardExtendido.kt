package com.dls.pymetask.presentation.components


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Whatsapp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dls.pymetask.domain.model.ArchivoUiModel

@Composable
fun ArchivoCardExtendido(
    archivo: ArchivoUiModel,
    onEliminar: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { abrirArchivo(context, archivo.url) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = archivo.icono,
                contentDescription = archivo.nombre,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = archivo.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = archivo.fechaFormateada,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Abrir") },
                        onClick = {
                            abrirArchivo(context, archivo.url)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Enviar por WhatsApp") },
                        onClick = {
                            compartirPorWhatsApp(context, archivo.url)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Enviar por Email") },
                        onClick = {
                            compartirPorEmail(context, archivo.url)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            onEliminar()
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                        }
                    )
                }
            }
        }
    }
}

private fun abrirArchivo(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

private fun compartirPorWhatsApp(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
        setPackage("com.whatsapp")
    }
    context.startActivity(intent)
}

private fun compartirPorEmail(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_SUBJECT, "Archivo desde PymeTask")
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(intent)
}
