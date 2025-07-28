package com.dls.pymetask.presentation.notas

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.domain.usecase.nota.NotaUseCases
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

    fun cargarNotas() {
        viewModelScope.launch {
            _notas.value = useCases.getNotas()
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

    fun eliminarNota(nota: Nota) {
        viewModelScope.launch {
            useCases.deleteNota(nota.toString())
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
