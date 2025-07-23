package com.dls.pymetask.presentation.notas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.domain.useCase.nota.NotaUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class NotaViewModel @Inject constructor(private val useCases: NotaUseCases) : ViewModel() {
    var notas by mutableStateOf(listOf<Nota>())
        private set

    var notaActual by mutableStateOf<Nota?>(null)

    fun cargarNotas() {
        viewModelScope.launch {
            notas = useCases.getNotas()
        }
    }

    fun seleccionarNota(id: String) {
        viewModelScope.launch {
            notaActual = useCases.getNota(id)
        }
    }

    fun guardarNota(nota: Nota) {
        viewModelScope.launch {
            if (nota.id.isBlank()) {
                useCases.addNota(nota.copy(id = UUID.randomUUID().toString()))
            } else {
                useCases.updateNota(nota)
            }
            cargarNotas()
        }
    }

    fun eliminarNota(id: String) {
        viewModelScope.launch {
            useCases.deleteNota(id)
            cargarNotas()
        }
    }
}