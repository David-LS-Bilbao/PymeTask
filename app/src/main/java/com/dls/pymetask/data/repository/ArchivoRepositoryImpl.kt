package com.dls.pymetask.data.repository

import android.net.Uri
import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.repository.ArchivoRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.net.URLConnection

class ArchivoRepositoryImpl(
    private val storage: FirebaseStorage
) : ArchivoRepository {

    override suspend fun listarArchivos(carpeta: String): List<Archivo> {
        val listaRef = storage.reference.child(carpeta)
        val archivos = mutableListOf<Archivo>()

        val items = listaRef.listAll().await().items

        for (item in items) {
            val metadata = item.metadata.await()
            val tipoMime = metadata.contentType ?: URLConnection.guessContentTypeFromName(item.name)
            val url = item.downloadUrl.await().toString()

            val archivo = Archivo(id = item.name.hashCode().toString(), nombre = item.name, url = url, tipo = tipoMime?.substringAfter("/") ?: "desconocido", fecha = metadata.updatedTimeMillis)
            archivos.add(archivo)
        }

        return archivos
    }

    override suspend fun subirArchivo(
        uri: Uri,
        nombre: String,
        carpeta: String,
    ): Archivo {
        val ref = storage.reference.child("$carpeta/$nombre")
        ref.putFile(uri).await()

        val metadata = ref.metadata.await()
        val tipoMime = metadata.contentType ?: URLConnection.guessContentTypeFromName(nombre)
        val url = ref.downloadUrl.await().toString()

        return Archivo(
            id = nombre.hashCode().toString(),
            nombre = nombre,
            url = url,
            tipo = tipoMime?.substringAfter("/") ?: "desconocido",
            carpetaId = carpeta,
            fecha = metadata.updatedTimeMillis
        )
    }

    override suspend fun guardarArchivoEnFirestore(archivo: Archivo) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("archivos")
            .document(archivo.id)
            .set(archivo)
            .await()
    }

    override suspend fun obtenerArchivosDesdeFirestore(): List<Archivo> {
        val firestore = FirebaseFirestore.getInstance()
        val snapshot = firestore.collection("archivos").get().await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Archivo::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun crearCarpeta(nombre: String) {
        val firestore = FirebaseFirestore.getInstance()
        val id = nombre.hashCode().toString()

        val carpeta = Archivo(
            id = id,
            nombre = nombre,
            url = "", // no hay url
            tipo = "carpeta",
            fecha = System.currentTimeMillis()
        )

        firestore.collection("archivos")
            .document(id)
            .set(carpeta)
            .await()
    }


    override suspend fun obtenerArchivosPorCarpeta(carpetaId: String): List<Archivo> {
        val firestore = FirebaseFirestore.getInstance()
        val snapshot = firestore.collection("archivos")
            .whereEqualTo("carpetaId", carpetaId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.toObject(Archivo::class.java)?.copy(id = it.id) }
    }

    override suspend fun eliminarArchivo(archivoId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("archivos").document(archivoId).delete().await()
    }

    override suspend fun eliminarCarpeta(carpetaId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("archivos").document(carpetaId).delete().await()

        // También puedes borrar sus archivos si lo deseas:
        val snapshot = firestore.collection("archivos")
            .whereEqualTo("carpetaId", carpetaId).get().await()

        snapshot.documents.forEach { it.reference.delete() }
    }
    override suspend fun renombrarArchivo(id: String, nuevoNombre: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("archivos")
            .document(id)
            .update("nombre", nuevoNombre)
            .await()
    }

    override suspend fun eliminarArchivosPorCarpeta(carpetaId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val archivos = firestore.collection("archivos")
            .whereEqualTo("carpetaId", carpetaId)
            .get()
            .await()

        for (doc in archivos.documents) {
            doc.reference.delete().await()
        }
    }



}
