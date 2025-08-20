package com.dls.pymetask.presentation.components

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource // <-- i18n en Compose
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.presentation.archivos.abrirArchivoLocal
import com.dls.pymetask.utils.esAudio
import com.dls.pymetask.utils.esVideo
import com.dls.pymetask.utils.iconoPorTipo
import java.io.File
import java.net.URLConnection
import kotlinx.coroutines.launch

/**
 * Card “extendida” para un archivo/carpeta:
 * - Pulsación: intenta abrir/descargar y abrir el archivo (abrirArchivoLocal).
 * - Menú contextual: Renombrar, Enviar por WhatsApp/Email, Eliminar.
 *
 * @param archivo datos a pintar
 * @param onEliminar callback cuando el usuario elige eliminar
 * @param onRenombrar callback cuando el usuario elige renombrar
 */
@Composable
fun ArchivoCardExtendido(
    archivo: ArchivoUiModel,
    onEliminar: () -> Unit,
    onRenombrar: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var cargando by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            // 🔹 Al tocar: descarga/abre el archivo (deshabilitamos mientras carga)
            .clickable(enabled = !cargando) {
                scope.launch {
                    cargando = true
                    abrirArchivoLocal(context, archivo.nombre, archivo.url)
                    cargando = false
                }
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.blueCard))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // === Icono/miniatura según tipo ===
            when {
                esImagen(archivo.tipo) -> {
                    Image(
                        painter = rememberAsyncImagePainter(archivo.url),
                        contentDescription = archivo.nombre, // nombre del archivo como descripción
                        modifier = Modifier
                            .size(40.dp)
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                }
                esVideo(archivo.tipo) -> {
                    Icon(
                        Icons.Default.Videocam,
                        contentDescription = stringResource(R.string.files_cd_video),
                        modifier = Modifier.size(40.dp)
                    )
                }
                esAudio(archivo.tipo) -> {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = stringResource(R.string.files_cd_audio),
                        modifier = Modifier.size(40.dp)
                    )
                }
                archivo.tipo == "carpeta" -> {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = stringResource(R.string.files_cd_folder),
                        modifier = Modifier.size(40.dp)
                    )
                }
                else -> {
                    Icon(
                        iconoPorTipo(archivo.tipo),
                        contentDescription = stringResource(R.string.files_cd_file),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // === Nombre + fecha ===
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

            // === Menú contextual ===
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.files_cd_menu)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.common_rename)) },
                        onClick = {
                            onRenombrar()
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.common_send_whatsapp)) },
                        onClick = {
                            compartirPorWhatsApp(context, archivo.url)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.common_send_email)) },
                        onClick = {
                            compartirPorEmail(context, archivo.url)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.common_delete)) },
                        onClick = {
                            onEliminar()
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                    )
                }
            }
        }
    }
}

/** Abre una URL con un intent genérico (no se usa en esta card, se mantiene por si lo llamas desde fuera). */
private fun abrirArchivo(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}

/** Compartir enlace por WhatsApp (si está instalado). */
private fun compartirPorWhatsApp(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
        setPackage("com.whatsapp")
    }
    context.startActivity(intent)
}

/** Compartir enlace por Email con asunto localizado. */
private fun compartirPorEmail(context: Context, url: String) {
    val subject = context.getString(
        R.string.files_email_subject_from_app,
        context.getString(R.string.app_name)
    )
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(intent)
}

/** Devuelve true si el tipo (extensión) pertenece a imagen. */
fun esImagen(tipo: String): Boolean {
    val extensionesImagen = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
    return tipo.lowercase() in extensionesImagen
}

/** Deducción básica de MIME por extensión, con último intento por nombre. */
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
            val guess = URLConnection.guessContentTypeFromName(file.name)
            if (!guess.isNullOrEmpty() && !guess.contains("octet-stream")) guess else "*/*"
        }
    }
}
