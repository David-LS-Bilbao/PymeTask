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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.presentation.archivos.abrirArchivoLocal
import com.dls.pymetask.utils.esAudio
import com.dls.pymetask.utils.esVideo
import com.dls.pymetask.utils.iconoPorTipo
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLConnection
import androidx.core.net.toUri
import com.dls.pymetask.R


@Composable
fun ArchivoCardExtendido(
    archivo: ArchivoUiModel,
    onEliminar: () -> Unit,
    onRenombrar:() -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var cargando by remember { mutableStateOf(false) }



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = !cargando) {
                scope.launch {
                    cargando = true
                    abrirArchivoLocal(context, archivo.nombre, archivo.url)
                    cargando = false
                }
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource( R.color.blueCard))
   ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {


            if (esImagen(archivo.tipo)) {
                Image(
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

                        text = { Text("Renombrar") },
                        onClick = {
                            onRenombrar() // 🔹 Llama al callback
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
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
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
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_SUBJECT, "Archivo desde PymeTask")
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(intent)
}


fun esImagen(tipo: String): Boolean {
    val extensionesImagen = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
    return tipo.lowercase() in extensionesImagen
}

fun getMimeTypeFromFile(file: File): String {
    val extension = file.extension.lowercase()

    return when (extension) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp", "bmp", "heic" -> "image/$extension"
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "avi" -> "video/x-msvideo"
        "mov" -> "video/quicktime"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        else -> {
            // Último intento con URLConnection (pero no confiar ciegamente)
            val guess = URLConnection.guessContentTypeFromName(file.name)
            if (!guess.isNullOrEmpty() && !guess.contains("octet-stream")) guess
            else "*/*" // último recurso
        }
    }
}


