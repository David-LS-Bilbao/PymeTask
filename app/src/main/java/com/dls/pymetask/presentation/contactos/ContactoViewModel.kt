package com.dls.pymetask.presentation.contactos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Contacto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactoViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _contactos = MutableStateFlow<List<Contacto>>(emptyList())
    val contactos: StateFlow<List<Contacto>> = _contactos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var listenerRegistration: ListenerRegistration? = null

    init {
        getContactos()
    }

    private fun getContactos() {
        _isLoading.value = true
        listenerRegistration = firestore.collection("contactos")
            .addSnapshotListener { snapshots, error ->
                _isLoading.value = false
                if (error != null) {
                    _errorMessage.value = "Error al cargar contactos: ${error.message}"
                    return@addSnapshotListener
                }

                val lista = snapshots?.documents?.mapNotNull { doc ->
                    doc.toObject(Contacto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                _contactos.value = lista
            }
    }

    fun onAddContacto(contacto: Contacto) {
        firestore.collection("contactos").document(contacto.id)
            .set(contacto)
            .addOnFailureListener {
                _errorMessage.value = "Error al guardar: ${it.message}"
            }
    }

    fun onUpdateContacto(contacto: Contacto) {
        firestore.collection("contactos").document(contacto.id)
            .set(contacto)
            .addOnFailureListener {
                _errorMessage.value = "Error al actualizar: ${it.message}"
            }
    }

    fun onDeleteContacto(id: String) {
        firestore.collection("contactos").document(id)
            .delete()
            .addOnFailureListener {
                _errorMessage.value = "Error al eliminar: ${it.message}"
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
