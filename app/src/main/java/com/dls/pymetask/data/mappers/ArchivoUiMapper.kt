package com.dls.pymetask.data.mappers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.model.ArchivoUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Archivo.toUiModel(): ArchivoUiModel {
    val icono = when (tipo.lowercase()) {
        "pdf" -> Icons.Default.Description
        "jpg", "jpeg", "png" -> Icons.Default.Image
        "mp3", "wav" -> Icons.Default.MusicNote
        "mp4", "avi" -> Icons.Default.Videocam
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaFormateada = sdf.format(Date(fecha))

    return ArchivoUiModel(
        id = id,
        nombre = nombre,
        tipo = tipo,
        icono = icono,
        fechaFormateada = fechaFormateada,
        url = url
    )
}
