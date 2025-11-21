package com.dls.pymetask.presentation.contactos
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Contacto
import com.dls.pymetask.presentation.commons.UiText
import com.dls.pymetask.utils.Constants.getUserIdSeguro
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
@HiltViewModel
class ContactoViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {
    val contactos = mutableStateListOf<Contacto>()
    val contactoSeleccionado = mutableStateOf<Contacto?>(null)
    val isUploading = mutableStateOf(false)
    private val _uiEvent = MutableSharedFlow<UiText>()
    val uiEvent: SharedFlow<UiText> = _uiEvent
    fun getContactos(context: Context) {
        val userId = getUserIdSeguro(context) ?: return
        firestore.collection("usuarios")
            .document(userId)
            .collection("contactos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Contactos", "Error al obtener contactos", error)
                    viewModelScope.launch {
                        _uiEvent.emit(
                            UiText.res(
                                R.string.contacts_error_loading_with_reason,
                                error.localizedMessage ?: "—"
                            )
                        )
                    }
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
    fun onAddContacto(context: Context, contacto: Contacto) = viewModelScope.launch {
        try {
            val userId = getUserIdSeguro(context) ?: return@launch
            firestore.collection("usuarios")
                .document(userId)
                .collection("contactos")
                .document(contacto.id)
                .set(contacto)
                .await()
            _uiEvent.emit(UiText.res(R.string.contacts_saved))
        } catch (e: Exception) {
            Log.e("AddContacto", "Error al añadir", e)
            _uiEvent.emit(
                UiText.res(
                    R.string.contacts_error_saving_with_reason,
                    e.localizedMessage ?: "—"
                )
            )
        }
    }
    fun onUpdateContacto(context: Context, contacto: Contacto) = viewModelScope.launch {
        try {
            val userId = getUserIdSeguro(context) ?: return@launch
            firestore.collection("usuarios")
                .document(userId)
                .collection("contactos")
                .document(contacto.id)
                .set(contacto)
                .await()
            _uiEvent.emit(UiText.res(R.string.contacts_updated))
        } catch (e: Exception) {
            Log.e("UpdateContacto", "Error al actualizar", e)
            _uiEvent.emit(
                UiText.res(
                    R.string.contacts_error_updating_with_reason,
                    e.localizedMessage ?: "—"
                )
            )
        }
    }
    fun onDeleteContacto(context: Context, contactoId: String, fotoUrl: String?) = viewModelScope.launch {
        try {
            val userId = getUserIdSeguro(context) ?: return@launch
            firestore.collection("usuarios")
                .document(userId)
                .collection("contactos")
                .document(contactoId)
                .delete()
                .await()
            if (!fotoUrl.isNullOrEmpty()) {
                try {
                    val ref = storage.getReferenceFromUrl(fotoUrl)
                    ref.delete().await()
                } catch (e: Exception) {
                    Log.e("EliminarFoto", "URL inválida o error al borrar", e)
                }
            }
            _uiEvent.emit(UiText.res(R.string.contacts_deleted))
        } catch (e: Exception) {
            Log.e("DeleteContacto", "Error al eliminar", e)
            _uiEvent.emit(
                UiText.res(
                    R.string.contacts_error_deleting_with_reason,
                    e.localizedMessage ?: "—"
                )
            )
        }
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
    ) = viewModelScope.launch {
        try {
            val userId = getUserIdSeguro(context) ?: return@launch
            val ref = storage.reference.child("usuarios/$userId/contactos/$contactoId.jpg")
            isUploading.value = true
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await()
            isUploading.value = false
            onResult(url.toString())
            _uiEvent.emit(UiText.res(R.string.contacts_photo_uploaded))
        } catch (e: Exception) {
            isUploading.value = false
            Log.e("SubirImagen", "Error: ", e)
            onResult(null)
            _uiEvent.emit(
                UiText.res(
                    R.string.contacts_error_uploading_photo_with_reason,
                    e.localizedMessage ?: "—"
                )
            )
        }
    }
}
