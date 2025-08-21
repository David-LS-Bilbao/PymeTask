package com.dls.pymetask.domain.repository

import android.net.Uri
import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.model.ArchivoUiModel

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
    suspend fun renombrarArchivo(id: String, nuevoNombre: String)
    suspend fun eliminarArchivosPorCarpeta(carpetaId: String)
    suspend fun obtenerNombreCarpeta(carpetaId: String): String?
    suspend fun renombrarCarpetaFirestore(archivo: ArchivoUiModel, nuevoNombre: String): ArchivoUiModel
    suspend fun renombrarArchivoStorageYFirestore(archivo: ArchivoUiModel, nuevoNombre: String): ArchivoUiModel











}