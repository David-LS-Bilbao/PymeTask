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
import java.net.URLConnection

suspend fun abrirArchivoLocal(context: Context, archivoNombre: String, archivoUrl: String) {
    val carpetaDescargas = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
    val nombreConExtension = if (archivoNombre.contains(".")) {
        archivoNombre
    } else {
        "$archivoNombre.${archivoTipoSinPunto(archivoUrl)}"
    }
    val archivoLocal = File(carpetaDescargas, nombreConExtension)


    if (!archivoLocal.exists()) {
        try {
            withContext(Dispatchers.IO) {
                val input = java.net.URL(archivoUrl).openStream()
                val output = FileOutputStream(archivoLocal)
                input.use { inputStream ->
                    output.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            Toast.makeText(context, "Archivo descargado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("AbrirArchivo", "Error al descargar", e)
            Toast.makeText(context, "Error al descargar archivo", Toast.LENGTH_SHORT).show()
            return
        }
    }

    try {
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            archivoLocal
        )
        val mime = getMimeTypeFromFile(archivoLocal)
        Log.d("AbrirArchivo", "MIME detectado: $mime para archivo: ${archivoLocal.name}")


        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val prefs = DefaultAppPreferences(context)
        val appPreferida = prefs.obtenerApp(mime)

        if (appPreferida != null) {
            intent.setPackage(appPreferida)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "La app predeterminada ya no está disponible",
                    Toast.LENGTH_SHORT
                ).show()
                prefs.eliminarApp(mime)
            }
        } else {
            val apps = context.packageManager.queryIntentActivities(intent, 0)

            if (apps.isNotEmpty()) {
                val nombres = apps.map { it.loadLabel(context.packageManager).toString() }
                val paquetes = apps.map { it.activityInfo.packageName }

                android.app.AlertDialog.Builder(context)
                    .setTitle("Selecciona una app para abrir este tipo de archivo")
                    .setItems(nombres.toTypedArray()) { _, index ->
                        val paqueteSeleccionado = paquetes[index]

                        if (!mime.contains("*") && !mime.contains("octet-stream")) {
                            prefs.guardarApp(mime, paqueteSeleccionado)
                        }


                        intent.setPackage(paqueteSeleccionado)
                        context.startActivity(intent)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                Toast.makeText(
                    context,
                    "No hay apps compatibles para abrir este archivo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    } catch (e: ActivityNotFoundException) {
        Log.e("AbrirArchivo", "Error al abrir el archivo", e)
        Toast.makeText(context, "Error al abrir el archivo", Toast.LENGTH_SHORT).show()
    }
}

fun archivoTipoSinPunto(url: String): String {
    return Uri.parse(url).lastPathSegment?.substringAfterLast(".")?.substringBefore("?") ?: "bin"
}

