package com.dls.pymetask.presentation.archivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.mappers.toUiModel
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.domain.usecase.archivo.ArchivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ArchivosViewModel @Inject constructor(
    private val archivoUseCase: ArchivoUseCase
) : ViewModel() {

    private val _archivos = MutableStateFlow<List<ArchivoUiModel>>(emptyList())
    val archivos: StateFlow<List<ArchivoUiModel>> = _archivos

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando

    fun cargarArchivos() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val lista = archivoUseCase.obtenerArchivos()
                    .filter { it.tipo == "carpeta" }
                    .map { it.toUiModel() }
                _archivos.value = lista
            } catch (e: Exception) {
                _uiEvent.emit("Error al cargar archivos: ${e.localizedMessage}")
            } finally {
                _cargando.value = false
            }
        }
    }

    fun crearCarpeta(nombre: String) {
        viewModelScope.launch {
            try {
                archivoUseCase.crearCarpeta(nombre)
                cargarArchivos()
                _uiEvent.emit("Carpeta creada")
            } catch (e: Exception) {
                _uiEvent.emit("Error al crear carpeta: ${e.localizedMessage}")
            }
        }
    }
}


