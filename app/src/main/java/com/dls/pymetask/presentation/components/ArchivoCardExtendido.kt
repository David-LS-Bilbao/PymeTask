package com.dls.pymetask.presentation.components


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.utils.esAudio
import com.dls.pymetask.utils.esVideo
import com.dls.pymetask.utils.iconoPorTipo


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

            Log.d("ArchivoCard", "tipo = ${archivo.tipo}, url = ${archivo.url}")


            if (esImagen(archivo.tipo)) {
                androidx.compose.foundation.Image(
                    painter = rememberAsyncImagePainter(archivo.url),
                    contentDescription = archivo.nombre,
                    modifier = Modifier
                        .size(40.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            } else {
                when {
                    esImagen(archivo.tipo) -> {
                        Image(
                            painter = rememberAsyncImagePainter(archivo.url),
                            contentDescription = archivo.nombre,
                            modifier = Modifier
                                .size(40.dp)
                                .aspectRatio(1f),
                            contentScale = ContentScale.Crop
                        )
                    }

                    esVideo(archivo.tipo) -> {
                        Icon(Icons.Default.Videocam, contentDescription = "Video", modifier = Modifier.size(40.dp))
                    }

                    esAudio(archivo.tipo) -> {
                        Icon(Icons.Default.MusicNote, contentDescription = "Audio", modifier = Modifier.size(40.dp))
                    }

                    archivo.tipo == "carpeta" -> {
                        Icon(Icons.Default.Folder, contentDescription = "Carpeta", modifier = Modifier.size(40.dp))
                    }

                    else -> {
                        Icon(iconoPorTipo(archivo.tipo), contentDescription = "Archivo", modifier = Modifier.size(40.dp))
                    }
                }

            }


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


fun esImagen(tipo: String): Boolean {
    val extensionesImagen = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
    return tipo.lowercase() in extensionesImagen
}