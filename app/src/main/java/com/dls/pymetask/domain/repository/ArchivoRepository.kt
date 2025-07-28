package com.dls.pymetask.domain.repository

import android.net.Uri
import com.dls.pymetask.domain.model.Archivo

interface ArchivoRepository {
    suspend fun listarArchivos(carpeta: String = "archivos"): List<Archivo>

    suspend fun subirArchivo(
        uri: Uri,
        nombre: String,
        carpeta: String = "archivos"
    ): Archivo

    suspend fun guardarArchivoEnFirestore(archivo: Archivo)
    suspend fun obtenerArchivosDesdeFirestore(): List<Archivo>
    suspend fun crearCarpeta(nombre: String)
    suspend fun eliminarCarpeta(carpetaId: String)
    suspend fun obtenerArchivosPorCarpeta(carpetaId: String): List<Archivo>
    suspend fun eliminarArchivo(archivoId: String)








}