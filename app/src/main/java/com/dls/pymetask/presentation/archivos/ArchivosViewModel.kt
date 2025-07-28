package com.dls.pymetask.presentation.archivos

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.mappers.toUiModel
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.domain.usecase.archivo.CrearCarpetaUseCase
import com.dls.pymetask.domain.usecase.archivo.EliminarCarpetaUseCase
import com.dls.pymetask.domain.usecase.archivo.GuardarArchivoUseCase
import com.dls.pymetask.domain.usecase.archivo.ObtenerArchivosFirestoreUseCase
import com.dls.pymetask.domain.usecase.archivo.SubirArchivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ArchivosViewModel @Inject constructor(
    private val subirArchivoUseCase: SubirArchivoUseCase,
    private val guardarArchivoUseCase: GuardarArchivoUseCase,
    private val obtenerArchivosFirestoreUseCase: ObtenerArchivosFirestoreUseCase,
    private val crearCarpetaUseCase: CrearCarpetaUseCase

) : ViewModel() {


    private val _archivos = MutableStateFlow<List<ArchivoUiModel>>(emptyList())
    val archivos: StateFlow<List<ArchivoUiModel>> = _archivos

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent




    fun cargarArchivos() {
        viewModelScope.launch {
            try {
                val lista = obtenerArchivosFirestoreUseCase()
                    .filter { it.tipo == "carpeta" } // ✅ Solo mostrar carpetas
                    .map { it.toUiModel() }

                _archivos.value = lista
            } catch (e: Exception) {
                _uiEvent.emit("Error al cargar archivos: ${e.localizedMessage}")
            }
        }
    }


    fun subirArchivo(uri: Uri, nombre: String) {
        viewModelScope.launch {
            try {
                val archivo = subirArchivoUseCase(uri, nombre)
                guardarArchivoUseCase(archivo) // << nuevo paso
                _archivos.value = _archivos.value + archivo.toUiModel()
            } catch (e: Exception) {
                _uiEvent.emit("Error al subir: ${e.localizedMessage}")

            }
        }
    }

    fun crearCarpeta(nombre: String) {
        viewModelScope.launch {
            try {
                crearCarpetaUseCase(nombre)
                cargarArchivos() // recarga tras crear
                _uiEvent.emit("Carpeta creada")
            } catch (e: Exception) {
                _uiEvent.emit("Error al crear carpeta: ${e.localizedMessage}")
            }
        }
    }

    @Inject lateinit var eliminarCarpetaUseCase: EliminarCarpetaUseCase

    fun eliminarCarpeta(carpetaId: String) {
        viewModelScope.launch {
            try {
                eliminarCarpetaUseCase(carpetaId)
                cargarArchivos()
                _uiEvent.emit("Carpeta eliminada")
            } catch (e: Exception) {
                _uiEvent.emit("Error al eliminar carpeta: ${e.localizedMessage}")
            }
        }
    }

}
