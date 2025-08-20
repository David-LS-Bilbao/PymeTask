
package com.dls.pymetask.presentation.archivos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.dls.pymetask.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection

/**
 * Descarga (si hace falta) y abre un archivo en almacenamiento privado de "Downloads".
 * - Localiza los mensajes de usuario (Toasts).
 * - Detecta el MIME de forma robusta para elegir la app de apertura.
 *
 * @param context Contexto Android
 * @param archivoNombre Nombre del archivo (con o sin extensión)
 * @param archivoUrl URL directa de descarga
 */
suspend fun abrirArchivoLocal(context: Context, archivoNombre: String, archivoUrl: String) {
    val tag = "AbrirArchivo"
    val carpetaDescargas = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    // 1) Determinar la extensión final (si no viene en el nombre, inférela desde la URL)
    val extension = archivoNombre.substringAfterLast('.', missingDelimiterValue = "")
        .ifEmpty {
            archivoUrl.toUri().lastPathSegment
                ?.substringAfterLast(".", "bin")
                ?.substringBefore("?") ?: "bin"
        }

    // 2) Asegurar nombre con extensión
    val nombreConExtension = if (archivoNombre.contains(".")) archivoNombre else "$archivoNombre.$extension"

    // 3) Archivo destino y crear carpeta si no existe
    val archivoLocal = File(carpetaDescargas, nombreConExtension)
    archivoLocal.parentFile?.mkdirs()

    // 4) Descargar si no existe
    if (!archivoLocal.exists()) {
        try {
            withContext(Dispatchers.IO) {
                URL(archivoUrl).openStream().use { input ->
                    FileOutputStream(archivoLocal).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            Toast.makeText(context, context.getString(R.string.files_toast_downloaded), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(tag, "Error al descargar ($nombreConExtension)", e)
            Toast.makeText(context, context.getString(R.string.files_toast_download_error), Toast.LENGTH_SHORT).show()
            return
        }
    } else {
        // Ya lo tenemos en local
        Toast.makeText(context, context.getString(R.string.files_toast_opened), Toast.LENGTH_SHORT).show()
    }

    // 5) Intent de apertura con FileProvider + MIME correcto
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivoLocal
        )

        // Detección de MIME: contentResolver (si el proveedor lo conoce) > nombre de archivo > comodín
        val mime: String = context.contentResolver.getType(uri)
            ?: URLConnection.guessContentTypeFromName(nombreConExtension)
            ?: "*/*"

        Log.d(tag, "MIME detectado: $mime para archivo: $nombreConExtension")

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // No hay app registrada para ese MIME
        Toast.makeText(context, context.getString(R.string.files_toast_no_app), Toast.LENGTH_SHORT).show()
    }
}



