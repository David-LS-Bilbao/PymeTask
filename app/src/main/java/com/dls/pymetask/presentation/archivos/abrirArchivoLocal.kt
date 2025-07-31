package com.dls.pymetask.presentation.archivos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.dls.pymetask.data.preferences.DefaultAppPreferences
import com.dls.pymetask.presentation.components.getMimeTypeFromFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection
import androidx.core.net.toUri

suspend fun abrirArchivoLocal(context: Context, archivoNombre: String, archivoUrl: String) {
    val carpetaDescargas = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    // Detectar extensión si no está incluida en el nombre
    val extension = archivoNombre.substringAfterLast('.', missingDelimiterValue = "")
        .ifEmpty {
            archivoUrl.toUri().lastPathSegment
                ?.substringAfterLast(".", "bin")
                ?.substringBefore("?") ?: "bin"
        }

    val nombreConExtension = if (archivoNombre.contains(".")) {
        archivoNombre
    } else {
        "$archivoNombre.$extension"
    }

    val archivoLocal = File(carpetaDescargas, nombreConExtension)

    // Crear carpeta si no existe
    archivoLocal.parentFile?.mkdirs()

    // Descargar si no existe
    if (!archivoLocal.exists()) {
        try {
            withContext(Dispatchers.IO) {
                val input = URL(archivoUrl).openStream()
                val output = FileOutputStream(archivoLocal)
                input.use { inputStream ->
                    output.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            Toast.makeText(context, "Archivo descargado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("AbrirArchivo", "Error al descargar ($nombreConExtension)", e)
            Toast.makeText(context, "Error al descargar archivo", Toast.LENGTH_SHORT).show()
            return
        }
    } else {
        Toast.makeText(context, "Archivo abierto", Toast.LENGTH_SHORT).show()
    }

    // Abrir archivo con tipo correcto
    try {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            archivoLocal
        )

        val mime = URLConnection.guessContentTypeFromName(nombreConExtension) ?: "*/*"
        Log.d("AbrirArchivo", "MIME detectado: $mime para archivo: $nombreConExtension")

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay app para abrir este tipo de archivo", Toast.LENGTH_SHORT).show()
    }
}

