package com.dls.pymetask.presentation.archivos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.R
import com.dls.pymetask.data.mappers.toUiModel
import com.dls.pymetask.domain.model.ArchivoUiModel
import com.dls.pymetask.domain.useCase.archivo.ArchivoUseCase
import com.dls.pymetask.presentation.commons.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject // <-- usa javax para Hilt
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

    private val _uiEvent = MutableSharedFlow<UiText>()
    val uiEvent: SharedFlow<UiText> = _uiEvent

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
                val reason = e.localizedMessage ?: "—"
                _uiEvent.emit(UiText.res(R.string.files_error_loading_with_reason, reason))
                _uiEvent.emit(UiText.res(R.string.files_folder_created))
                _uiEvent.emit(UiText.res(R.string.files_error_creating_with_reason, reason))

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
                _uiEvent.emit(UiText.StringResource(R.string.files_folder_created))
            } catch (e: Exception) {
                val reason = e.localizedMessage ?: "—"
                _uiEvent.emit(UiText.res(R.string.files_error_loading_with_reason, reason))
                _uiEvent.emit(UiText.res(R.string.files_folder_created))
                _uiEvent.emit(UiText.res(R.string.files_error_creating_with_reason, reason))

            }
        }
    }


}



