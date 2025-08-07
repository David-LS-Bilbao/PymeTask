package com.dls.pymetask.presentation.notas

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.domain.useCase.nota.NotaUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotaViewModel @Inject constructor(
    private val useCases: NotaUseCases
) : ViewModel() {

    private val _notas = MutableStateFlow<List<Nota>>(emptyList())
    val notas: StateFlow<List<Nota>> = _notas


    var notaActual: Nota? by mutableStateOf(null)
    private set


    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun cargarNotas() {
        viewModelScope.launch {
            _isLoading.value = true
            _notas.value = useCases.getNotas()
            _isLoading.value = false
        }
    }



    fun seleccionarNota(id: String) {
        viewModelScope.launch {
            notaActual = useCases.getNota(id)
        }
    }

    fun guardarNota(nota: Nota) {
        viewModelScope.launch {
            useCases.addNota(nota)
            cargarNotas()
        }
    }

    fun limpiarNotaActual() {
        notaActual = null
    }
    fun eliminarNotaPorId(id: String) {
        viewModelScope.launch {
            useCases.deleteNota(id)
            cargarNotas()
        }
    }

}
