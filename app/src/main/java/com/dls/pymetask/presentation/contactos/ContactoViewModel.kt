package com.dls.pymetask.presentation.contactos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.domain.usecase.DeleteContactoUseCase
import com.dls.pymetask.domain.usecase.GetContactosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactoViewModel(
    private val getContactosUseCase: GetContactosUseCase,
    private val deleteContactoUseCase: DeleteContactoUseCase
) : ViewModel() {

    private val _contactos = MutableStateFlow<List<Contacto>>(emptyList())
    val contactos: StateFlow<List<Contacto>> = _contactos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        cargarContactos()
    }

    fun cargarContactos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
               // _contactos.value = getContactosUseCase()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al cargar contactos"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onDeleteContacto(id: String) {
        viewModelScope.launch {
            try {
              //  deleteContactoUseCase(id)
                cargarContactos()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al eliminar contacto"
            }
        }
    }

    fun onEditContacto(contacto: Contacto) {
        // Lógica de navegación se maneja en la pantalla, aquí no es necesario implementar nada.
    }
}
