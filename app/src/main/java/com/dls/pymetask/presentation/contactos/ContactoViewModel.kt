

package com.dls.pymetask.presentation.contactos

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.utils.Constants.getUserIdSeguro
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactoViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    val contactos = mutableStateListOf<Contacto>()
    val contactoSeleccionado = mutableStateOf<Contacto?>(null)
    val isUploading = mutableStateOf(false)

    // Cargar contactos del usuario autenticado
    fun getContactos(context: Context) {
        val userId = getUserIdSeguro(context) ?: return
        firestore.collection("usuarios")
            .document(userId)
            .collection("contactos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Contactos", "Error al obtener contactos", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    contactos.clear()
                    for (doc in snapshot.documents) {
                        doc.toObject(Contacto::class.java)?.let { contactos.add(it) }
                    }
                } else {
                    contactos.clear()
                }
            }
    }

    // Añadir contacto a la ruta segura del usuario
    fun onAddContacto(context: Context, contacto: Contacto) = viewModelScope.launch {
        val userId = getUserIdSeguro(context) ?: return@launch
        firestore.collection("usuarios")
            .document(userId)
            .collection("contactos")
            .document(contacto.id)
            .set(contacto)
            .addOnFailureListener { Log.e("AddContacto", "Error al añadir", it) }
    }

    // Actualizar un contacto ya existente
    fun onUpdateContacto(context: Context, contacto: Contacto) = viewModelScope.launch {
        val userId = getUserIdSeguro(context) ?: return@launch
        firestore.collection("usuarios")
            .document(userId)
            .collection("contactos")
            .document(contacto.id)
            .set(contacto)
            .addOnFailureListener { Log.e("UpdateContacto", "Error al actualizar", it) }
    }

    // Eliminar contacto del usuario y su imagen si tiene
    fun onDeleteContacto(context: Context, contactoId: String, fotoUrl: String?) = viewModelScope.launch {
        val userId = getUserIdSeguro(context) ?: return@launch
        firestore.collection("usuarios")
            .document(userId)
            .collection("contactos")
            .document(contactoId)
            .delete()
            .addOnSuccessListener {
                if (!fotoUrl.isNullOrEmpty()) {
                    try {
                        val ref = storage.getReferenceFromUrl(fotoUrl)
                        ref.delete()
                    } catch (e: Exception) {
                        Log.e("EliminarFoto", "URL inválida o error al borrar", e)
                    }
                }
            }
            .addOnFailureListener { Log.e("DeleteContacto", "Error al eliminar", it) }
    }

    fun seleccionarContacto(contacto: Contacto) {
        contactoSeleccionado.value = contacto
    }

    fun limpiarSeleccion() {
        contactoSeleccionado.value = null
    }

    fun subirImagen(
        context: Context,
        uri: Uri,
        contactoId: String,
        onResult: (String?) -> Unit
    ) {
        val userId = getUserIdSeguro(context) ?: return
        val ref = storage.reference.child("usuarios/$userId/contactos/$contactoId.jpg")

        isUploading.value = true
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    isUploading.value = false
                    onResult(url.toString())
                }
            }
            .addOnFailureListener {
                isUploading.value = false
                Log.e("SubirImagen", "Error: ", it)
                onResult(null)
            }
    }
}

