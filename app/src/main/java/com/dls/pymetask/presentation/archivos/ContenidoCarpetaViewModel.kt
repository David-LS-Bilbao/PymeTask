
package com.dls.pymetask.presentation.archivos

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.mappers.toUiModel
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.domain.usecase.archivo.*
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
    private val obtenerArchivosPorCarpetaUseCase: ObtenerArchivosPorCarpetaUseCase,
    private val subirArchivoUseCase: SubirArchivoUseCase,
    private val guardarArchivoUseCase: GuardarArchivoUseCase,
    private val eliminarArchivoUseCase: EliminarArchivoUseCase,
    private val renombrarArchivoUseCase: RenombrarArchivoUseCase,
    private val eliminarArchivosPorCarpetaUseCase: EliminarCarpetaUseCase
) : ViewModel() {

    private val _archivos = MutableStateFlow<List<ArchivoUiModel>>(emptyList())
    val archivos: StateFlow<List<ArchivoUiModel>> = _archivos

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent

    // Cargar archivos de una carpeta concreta
    fun cargarArchivosDeCarpeta(carpetaId: String) {
        viewModelScope.launch {
            try {
                val lista = obtenerArchivosPorCarpetaUseCase(carpetaId)
                    .map { it.toUiModel() }
                _archivos.value = lista
            } catch (e: Exception) {
                Log.e("ContenidoCarpetaViewModel", "Error al cargar archivos: ${e.localizedMessage}")
            }
        }
    }

    // Subir archivo dentro de una carpeta
    fun subirArchivo(uri: Uri, nombre: String, carpetaId: String) {
        viewModelScope.launch {
            try {
                val archivo = subirArchivoUseCase(uri, nombre).copy(carpetaId = carpetaId)
                guardarArchivoUseCase(archivo)
                cargarArchivosDeCarpeta(carpetaId)
                Log.d("Archivos", "Archivo guardado en Firestore: ${archivo.nombre}")
                _uiEvent.emit("Archivo añadido correctamente")
            } catch (e: Exception) {
                Log.e("Archivos", "Error al subir o guardar archivo: ${e.localizedMessage}")
                _uiEvent.emit("Error al subir archivo")
            }
        }
    }

    // Eliminar un archivo individual
    fun eliminarArchivo(archivoId: String, carpetaId: String) {
        viewModelScope.launch {
            eliminarArchivoUseCase(archivoId)
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
    fun renombrarCarpeta(id: String, nuevoNombre: String) {
        viewModelScope.launch {
            try {
                renombrarArchivoUseCase(id, nuevoNombre)
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
                eliminarArchivosPorCarpetaUseCase(id)       // eliminar hijos
                eliminarArchivoUseCase(id)
                _uiEvent.emit("Carpeta eliminada")
            } catch (e: Exception) {
                _uiEvent.emit("Error al eliminar: ${e.localizedMessage}")
            }
        }
    }
}



