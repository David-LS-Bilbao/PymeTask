
package com.dls.pymetask.presentation.archivos

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.R
import com.dls.pymetask.data.mappers.toUiModel
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.domain.useCase.archivo.ArchivoUseCase
import com.dls.pymetask.presentation.commons.UiText
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject // <-- usa javax para Hilt
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

    // Antes: SharedFlow<String>
    private val _uiEvent = MutableSharedFlow<UiText>()
    val uiEvent: SharedFlow<UiText> = _uiEvent

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
                val reason = e.localizedMessage ?: "—"
                _uiEvent.emit(UiText.res(R.string.files_error_loading_with_reason, reason))
            } finally {
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
                _uiEvent.emit(UiText.res(R.string.files_uploaded_success))
            } catch (e: Exception) {
                Log.e("Archivos", "Error al subir o guardar archivo: ${e.localizedMessage}")
                val reason = e.localizedMessage ?: "—"
                _uiEvent.emit(UiText.res(R.string.files_error_uploading_with_reason, reason))
            } finally {
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

    // Renombrar archivo (el nombre del método es correcto; el comentario anterior decía "carpeta")
    fun renombrarArchivo(id: String, nuevoNombre: String, carpetaId: String) {
        viewModelScope.launch {
            try {
                archivoUseCase.renombrarArchivo(id, nuevoNombre)
                cargarArchivosDeCarpeta(carpetaId)
                _uiEvent.emit(UiText.res(R.string.files_file_renamed))
            } catch (e: Exception) {
                val reason = e.localizedMessage ?: "—"
                _uiEvent.emit(UiText.res(R.string.files_error_renaming_with_reason, reason))
            }
        }
    }

    // Eliminar la carpeta actual
    fun eliminarCarpeta(id: String) {
        viewModelScope.launch {
            try {
                archivoUseCase.eliminarArchivosPorCarpeta(id) // eliminar hijos
                archivoUseCase.eliminarArchivo(id)
                _uiEvent.emit(UiText.res(R.string.files_folder_deleted))
            } catch (e: Exception) {
                val reason = e.localizedMessage ?: "—"
                _uiEvent.emit(UiText.res(R.string.files_error_deleting_with_reason, reason))
            }
        }
    }
}
