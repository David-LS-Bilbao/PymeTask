package com.dls.pymetask.data.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.dls.pymetask.domain.model.Archivo
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.domain.repository.ArchivoRepository
import com.dls.pymetask.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLConnection

class ArchivoRepositoryImpl(
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore,
    private val context: android.content.Context
) : ArchivoRepository {

    private fun userCollection() = Constants.getUserIdSeguro(context)?.let { userId ->
        firestore.collection("usuarios").document(userId).collection("archivos")
    } ?: run {
        Log.e("ArchivoRepo", "❌ userId no disponible, abortando operación")
        null
    }

    override suspend fun listarArchivos(carpeta: String): List<Archivo> {
        val listaRef = storage.reference.child(carpeta)
        val archivos = mutableListOf<Archivo>()

        val items = listaRef.listAll().await().items

        for (item in items) {
            val metadata = item.metadata.await()
            val tipoMime = metadata.contentType ?: URLConnection.guessContentTypeFromName(item.name)
            val url = item.downloadUrl.await().toString()

            val archivo = Archivo(
                id = item.name.hashCode().toString(),
                nombre = item.name,
                url = url,
                tipo = tipoMime?.substringAfter("/") ?: "desconocido",
                fecha = metadata.updatedTimeMillis
            )
            archivos.add(archivo)
        }

        return archivos
    }

    override suspend fun subirArchivo(uri: Uri, nombre: String, carpeta: String): Archivo {
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
        userCollection()?.document(archivo.id)?.set(archivo)?.await()
    }

    override suspend fun obtenerArchivosDesdeFirestore(): List<Archivo> {
        val snapshot = userCollection()?.get()?.await() ?: return emptyList()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Archivo::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun crearCarpeta(nombre: String) {
        val id = nombre.hashCode().toString()
        val carpeta = Archivo(
            id = id,
            nombre = nombre,
            url = "",
            tipo = "carpeta",
            fecha = System.currentTimeMillis()
        )
        userCollection()?.document(id)?.set(carpeta)?.await()
    }

    override suspend fun obtenerArchivosPorCarpeta(carpetaId: String): List<Archivo> {
        val snapshot = userCollection()
            ?.whereEqualTo("carpetaId", carpetaId)
            ?.get()?.await() ?: return emptyList()

        return snapshot.documents.mapNotNull { it.toObject(Archivo::class.java)?.copy(id = it.id) }
    }

    override suspend fun eliminarArchivo(archivoId: String) {
        userCollection()?.document(archivoId)?.delete()?.await()
    }

    override suspend fun eliminarCarpeta(carpetaId: String) {
        userCollection()?.document(carpetaId)?.delete()?.await()

        val snapshot = userCollection()
            ?.whereEqualTo("carpetaId", carpetaId)?.get()?.await() ?: return

        snapshot.documents.forEach { it.reference.delete() }
    }

    override suspend fun renombrarArchivo(id: String, nuevoNombre: String) {
        userCollection()?.document(id)?.update("nombre", nuevoNombre)?.await()
    }

    override suspend fun eliminarArchivosPorCarpeta(carpetaId: String) {
        val archivos = userCollection()
            ?.whereEqualTo("carpetaId", carpetaId)
            ?.get()?.await() ?: return

        for (doc in archivos.documents) {
            doc.reference.delete().await()
        }
    }

    override suspend fun renombrarCarpetaFirestore(archivo: ArchivoUiModel, nuevoNombre: String): ArchivoUiModel {
        userCollection()?.document(archivo.id)
            ?.update("nombre", nuevoNombre)
            ?.await()
        return archivo.copy(nombre = nuevoNombre)
    }

    override suspend fun renombrarArchivoStorageYFirestore(archivo: ArchivoUiModel, nuevoNombre: String): ArchivoUiModel {
        val nuevaRuta = "archivos/$nuevoNombre"
        val originalRef = storage.reference.child("archivos/${archivo.nombre}")
        val nuevoRef = storage.reference.child(nuevaRuta)

        val tempFile = File.createTempFile("archivo_temp", null)
        originalRef.getFile(tempFile).await()

        nuevoRef.putFile(tempFile.toUri()).await()
        val nuevoUrl = nuevoRef.downloadUrl.await().toString()

        userCollection()?.document(archivo.id)
            ?.update(mapOf("nombre" to nuevoNombre, "url" to nuevoUrl))
            ?.await()

        originalRef.delete().await()

        return archivo.copy(nombre = nuevoNombre, url = nuevoUrl)
    }

    /**
     * ✅ Lee el documento de la carpeta y devuelve su campo "nombre".
     * Ajusta "carpetas" si tu colección usa otro nombre.
     */
    override suspend fun obtenerNombreCarpeta(carpetaId: String): String? = withContext(Dispatchers.IO) {
        // 1) Asegura sesión
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext null

        // 2) Rutas candidatas (ajusta nombres si usas otros)
        val refs = listOf(
            firestore.collection("usuarios").document(uid).collection("carpetas").document(carpetaId),
            firestore.collection("users").document(uid).collection("carpetas").document(carpetaId),
            firestore.collection("carpetas").document(carpetaId) // fallback global
        )

        // 3) Intenta cada ruta hasta encontrar un doc con algún campo de nombre
        for (docRef in refs) {
            val snap = runCatching { docRef.get().await() }.getOrNull() ?: continue
            if (!snap.exists()) continue

            // Campos posibles según cómo lo guardaras
            val nombre = snap.getString("nombre")
                ?: snap.getString("name")
                ?: snap.getString("titulo")
                ?: snap.getString("folderName")

            if (!nombre.isNullOrBlank()) return@withContext nombre
        }
        null
    }

}













// antigua


//import android.net.Uri
//import android.util.Log
//import androidx.core.net.toUri
//import com.dls.pymetask.domain.model.Archivo
//import com.dls.pymetask.domain.model.ArchivoUiModel
//import com.dls.pymetask.domain.repository.ArchivoRepository
//import com.dls.pymetask.utils.Constants
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.storage.FirebaseStorage
//import kotlinx.coroutines.tasks.await
//import java.io.File
//import java.net.URLConnection
//
//class ArchivoRepositoryImpl(
//    private val storage: FirebaseStorage,
//    private val firestore: FirebaseFirestore
//) : ArchivoRepository {
//
//    override suspend fun listarArchivos(carpeta: String): List<Archivo> {
//        val listaRef = storage.reference.child(carpeta)
//        val archivos = mutableListOf<Archivo>()
//
//        val items = listaRef.listAll().await().items
//
//        for (item in items) {
//            val metadata = item.metadata.await()
//            val tipoMime = metadata.contentType ?: URLConnection.guessContentTypeFromName(item.name)
//            val url = item.downloadUrl.await().toString()
//
//            val archivo = Archivo(id = item.name.hashCode().toString(), nombre = item.name, url = url, tipo = tipoMime?.substringAfter("/") ?: "desconocido", fecha = metadata.updatedTimeMillis)
//            archivos.add(archivo)
//        }
//
//        return archivos
//    }
//    override suspend fun subirArchivo(
//        uri: Uri,
//        nombre: String,
//        carpeta: String,
//    ): Archivo {
//        val ref = storage.reference.child("$carpeta/$nombre")
//        ref.putFile(uri).await()
//
//        val metadata = ref.metadata.await()
//        val tipoMime = metadata.contentType ?: URLConnection.guessContentTypeFromName(nombre)
//        val url = ref.downloadUrl.await().toString()
//
//        return Archivo(
//            id = nombre.hashCode().toString(),
//            nombre = nombre,
//            url = url,
//            tipo = tipoMime?.substringAfter("/") ?: "desconocido",
//            carpetaId = carpeta,
//            fecha = metadata.updatedTimeMillis
//        )
//    }
//
//    override suspend fun guardarArchivoEnFirestore(archivo: Archivo) {
//        val firestore = FirebaseFirestore.getInstance()
//        firestore.collection("archivos")
//            .document(archivo.id)
//            .set(archivo)
//            .await()
//    }
//    override suspend fun obtenerArchivosDesdeFirestore(): List<Archivo> {
//        val firestore = FirebaseFirestore.getInstance()
//        val snapshot = firestore.collection("archivos").get().await()
//
//        return snapshot.documents.mapNotNull { doc ->
//            doc.toObject(Archivo::class.java)?.copy(id = doc.id)
//        }
//    }
//    override suspend fun crearCarpeta(nombre: String) {
//        val firestore = FirebaseFirestore.getInstance()
//        val id = nombre.hashCode().toString()
//
//        val carpeta = Archivo(
//            id = id,
//            nombre = nombre,
//            url = "", // no hay url
//            tipo = "carpeta",
//            fecha = System.currentTimeMillis()
//        )
//
//        firestore.collection("archivos")
//            .document(id)
//            .set(carpeta)
//            .await()
//    }
//    override suspend fun obtenerArchivosPorCarpeta(carpetaId: String): List<Archivo> {
//        val firestore = FirebaseFirestore.getInstance()
//        val snapshot = firestore.collection("archivos")
//            .whereEqualTo("carpetaId", carpetaId)
//            .get()
//            .await()
//
//        return snapshot.documents.mapNotNull { it.toObject(Archivo::class.java)?.copy(id = it.id) }
//    }
//    override suspend fun eliminarArchivo(archivoId: String) {
//        val firestore = FirebaseFirestore.getInstance()
//        firestore.collection("archivos").document(archivoId).delete().await()
//    }
//    override suspend fun eliminarCarpeta(carpetaId: String) {
//        val firestore = FirebaseFirestore.getInstance()
//        firestore.collection("archivos").document(carpetaId).delete().await()
//
//        // También puedes borrar sus archivos si lo deseas:
//        val snapshot = firestore.collection("archivos")
//            .whereEqualTo("carpetaId", carpetaId).get().await()
//
//        snapshot.documents.forEach { it.reference.delete() }
//    }
//    override suspend fun renombrarArchivo(id: String, nuevoNombre: String) {
//        val firestore = FirebaseFirestore.getInstance()
//        firestore.collection("archivos")
//            .document(id)
//            .update("nombre", nuevoNombre)
//            .await()
//    }
//    override suspend fun eliminarArchivosPorCarpeta(carpetaId: String) {
//        val firestore = FirebaseFirestore.getInstance()
//        val archivos = firestore.collection("archivos")
//            .whereEqualTo("carpetaId", carpetaId)
//            .get()
//            .await()
//
//        for (doc in archivos.documents) {
//            doc.reference.delete().await()
//        }
//    }
//    override suspend fun renombrarCarpetaFirestore(archivo: ArchivoUiModel, nuevoNombre: String): ArchivoUiModel {
//        val documentoRef = firestore.collection("archivos").document(archivo.id)
//
//        // Actualiza el campo 'nombre' en Firestore
//        documentoRef.update("nombre", nuevoNombre).await()
//
//        // Devuelve una copia del archivo con el nuevo nombre
//        return archivo.copy(nombre = nuevoNombre)
//    }
//    override suspend fun renombrarArchivoStorageYFirestore(archivo: ArchivoUiModel, nuevoNombre: String): ArchivoUiModel {
//        val nuevaRuta = "archivos/$nuevoNombre"
//        val originalRef = storage.reference.child("archivos/${archivo.nombre}")
//        val nuevoRef = storage.reference.child(nuevaRuta)
//
//        // 1. Descargar archivo original a archivo temporal
//        val tempFile = File.createTempFile("archivo_temp", null)
//        originalRef.getFile(tempFile).await()
//
//        // 2. Subir archivo con nuevo nombre
//        nuevoRef.putFile(tempFile.toUri()).await()
//        val nuevoUrl = nuevoRef.downloadUrl.await().toString()
//
//        // 3. Actualiza Firestore con nuevo nombre y URL
//        val documentoRef = firestore.collection("archivos").document(archivo.id)
//        documentoRef.update(
//            mapOf(
//                "nombre" to nuevoNombre,
//                "url" to nuevoUrl
//            )
//        ).await()
//
//        // 4. Elimina el archivo antiguo del Storage
//        originalRef.delete().await()
//
//        // 5. Devuelve el archivo actualizado
//        return archivo.copy(nombre = nuevoNombre, url = nuevoUrl)
//    }
//}
