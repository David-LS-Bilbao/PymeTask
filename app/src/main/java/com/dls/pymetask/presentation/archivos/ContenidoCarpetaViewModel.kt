
package com.dls.pymetask.presentation.archivos

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.mappers.toUiModel
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.domain.useCase.archivo.ArchivoUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



@HiltViewModel
class ContenidoCarpetaViewModel @Inject constructor(

    private val archivoUseCase: ArchivoUseCase
) : ViewModel() {

    private val _archivos = MutableStateFlow<List<ArchivoUiModel>>(emptyList())
    val archivos: StateFlow<List<ArchivoUiModel>> = _archivos

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando




    // Cargar archivos de una carpeta concreta
    fun cargarArchivosDeCarpeta(carpetaId: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val lista = archivoUseCase.obtenerPorCarpeta(carpetaId)
                    .map { it.toUiModel() }
                _archivos.value = lista
            } catch (e: Exception) {
                Log.e("ContenidoCarpetaViewModel", "Error al cargar archivos: ${e.localizedMessage}")
            }
            finally {
                _cargando.value = false
            }
        }
    }

    // Subir archivo dentro de una carpeta

    fun subirArchivo(
        context: Context,
        uri: Uri,
        nombreOriginal: String,
        carpetaId: String,
        onFinalizado: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Obtener MIME y extensión real
                val mimeType = context.contentResolver.getType(uri) ?: "*/*"
                val extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(mimeType) ?: "bin"

                val nombreFinal = if (nombreOriginal.contains(".")) {
                    nombreOriginal
                } else {
                    "${System.currentTimeMillis()}.$extension"
                }

                val archivo = archivoUseCase.subirArchivo(uri, nombreFinal).copy(carpetaId = carpetaId)
                archivoUseCase.guardarArchivo(archivo)
                cargarArchivosDeCarpeta(carpetaId)
                Log.d("Archivos", "Archivo guardado en Firestore: ${archivo.nombre}")
                _uiEvent.emit("Archivo añadido correctamente")
            } catch (e: Exception) {
                Log.e("Archivos", "Error al subir o guardar archivo: ${e.localizedMessage}")
                _uiEvent.emit("Error al subir archivo")
            }finally {
                onFinalizado()
            }
        }
    }

    // Eliminar un archivo individual
    fun eliminarArchivo(archivoId: String, carpetaId: String) {
        viewModelScope.launch {
            archivoUseCase.eliminarArchivo(archivoId)
            cargarArchivosDeCarpeta(carpetaId)
        }
    }

    // Obtener el nombre actual de la carpeta
    fun obtenerNombreCarpeta(carpetaId: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val doc = firestore.collection("archivos").document(carpetaId).get().await()
                val nombre = doc.getString("nombre") ?: "Carpeta"
                onResult(nombre)
            } catch (e: Exception) {
                onResult("Carpeta")
                Log.e("ContenidoCarpetaViewModel", "Error al obtener nombre de carpeta: ${e.localizedMessage}")
            }
        }
    }

    // Renombrar la carpeta actual
    fun renombrarArchivo(id: String, nuevoNombre: String, carpetaId: String) {
        viewModelScope.launch {
            try {
                archivoUseCase.renombrarArchivo(id, nuevoNombre)
                cargarArchivosDeCarpeta(carpetaId) // 🔁 recarga tras renombrar

                _uiEvent.emit("Carpeta renombrada")
            } catch (e: Exception) {
                _uiEvent.emit("Error al renombrar: ${e.localizedMessage}")
            }
        }
    }

    // Eliminar la carpeta actual
    fun eliminarCarpeta(id: String) {
        viewModelScope.launch {
            try {
                archivoUseCase.eliminarArchivosPorCarpeta(id)       // eliminar hijos
                archivoUseCase.eliminarArchivo(id)
                _uiEvent.emit("Carpeta eliminada")
            } catch (e: Exception) {
                _uiEvent.emit("Error al eliminar: ${e.localizedMessage}")
            }
        }
    }
}



