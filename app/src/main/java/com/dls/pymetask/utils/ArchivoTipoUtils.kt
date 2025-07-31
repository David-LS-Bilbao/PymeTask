package com.dls.pymetask.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector


fun esVideo(extension: String): Boolean {
    val tipos = listOf("mp4", "avi", "mov", "mkv", "webm")
    return extension.lowercase() in tipos
}

fun esAudio(extension: String): Boolean {
    val tipos = listOf("mp3", "wav", "ogg", "m4a")
    return extension.lowercase() in tipos
}

fun iconoPorTipo(extension: String): ImageVector {
    return when (extension.lowercase()) {
        "pdf" -> Icons.Default.Description
        "doc", "docx" -> Icons.Default.Article
        "xls", "xlsx", "csv" -> Icons.Default.TableChart
        "mp3", "wav", "ogg" -> Icons.Default.MusicNote
        "mp4", "mov", "avi" -> Icons.Default.Videocam
        "folder", "carpeta" -> Icons.Default.Folder
        else -> Icons.Default.InsertDriveFile
    }
}
