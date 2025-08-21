
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

@HiltViewModel
class ContactoViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    // Estado de UI (se mantiene tu enfoque simple con Compose)
    val contactos = mutableStateListOf<Contacto>()
    val contactoSeleccionado = mutableStateOf<Contacto?>(null)
    val isUploading = mutableStateOf(false)

    // 🔔 Eventos de UI traducibles (toasts/snackbars)
    private val _uiEvent = MutableSharedFlow<UiText>()
    val uiEvent: SharedFlow<UiText> = _uiEvent

    /** Carga contactos del usuario autenticado y escucha cambios en tiempo real. */
    fun getContactos(context: Context) {
        val userId = getUserIdSeguro(context) ?: return
        firestore.collection("usuarios")
            .document(userId)
            .collection("contactos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Contactos", "Error al obtener contactos", error) // <-- logs intactos
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

    /** Añade un contacto bajo la ruta segura del usuario. */
    fun onAddContacto(context: Context, contacto: Contacto) = viewModelScope.launch {
        val userId = getUserIdSeguro(context) ?: return@launch
        firestore.collection("usuarios")
            .document(userId)
            .collection("contactos")
            .document(contacto.id)
            .set(contacto)
            .addOnSuccessListener {
                viewModelScope.launch { _uiEvent.emit(UiText.res(R.string.contacts_saved)) }
            }
            .addOnFailureListener {
                Log.e("AddContacto", "Error al añadir", it) // <-- logs intactos
                viewModelScope.launch {
                    _uiEvent.emit(
                        UiText.res(
                            R.string.contacts_error_saving_with_reason,
                            it.localizedMessage ?: "—"
                        )
                    )
                }
            }
    }

    /** Actualiza un contacto existente. */
    fun onUpdateContacto(context: Context, contacto: Contacto) = viewModelScope.launch {
        val userId = getUserIdSeguro(context) ?: return@launch
        firestore.collection("usuarios")
            .document(userId)
            .collection("contactos")
            .document(contacto.id)
            .set(contacto)
            .addOnSuccessListener {
                viewModelScope.launch { _uiEvent.emit(UiText.res(R.string.contacts_updated)) }
            }
            .addOnFailureListener {
                Log.e("UpdateContacto", "Error al actualizar", it) // <-- logs intactos
                viewModelScope.launch {
                    _uiEvent.emit(
                        UiText.res(
                            R.string.contacts_error_updating_with_reason,
                            it.localizedMessage ?: "—"
                        )
                    )
                }
            }
    }

    /** Elimina un contacto y, si tiene foto, intenta borrar su imagen. */
    fun onDeleteContacto(context: Context, contactoId: String, fotoUrl: String?) = viewModelScope.launch {
        val userId = getUserIdSeguro(context) ?: return@launch
        firestore.collection("usuarios")
            .document(userId)
            .collection("contactos")
            .document(contactoId)
            .delete()
            .addOnSuccessListener {
                // Borrado de la foto (si hay) con logs en caso de fallo
                if (!fotoUrl.isNullOrEmpty()) {
                    try {
                        val ref = storage.getReferenceFromUrl(fotoUrl)
                        ref.delete()
                    } catch (e: Exception) {
                        Log.e("EliminarFoto", "URL inválida o error al borrar", e) // <-- logs intactos
                    }
                }
                viewModelScope.launch { _uiEvent.emit(UiText.res(R.string.contacts_deleted)) }
            }
            .addOnFailureListener {
                Log.e("DeleteContacto", "Error al eliminar", it) // <-- logs intactos
                viewModelScope.launch {
                    _uiEvent.emit(
                        UiText.res(
                            R.string.contacts_error_deleting_with_reason,
                            it.localizedMessage ?: "—"
                        )
                    )
                }
            }
    }

    /** Selecciona un contacto para edición o detalle. */
    fun seleccionarContacto(contacto: Contacto) { contactoSeleccionado.value = contacto }

    /** Limpia la selección de contacto. */
    fun limpiarSeleccion() { contactoSeleccionado.value = null }

    /**
     * Sube la imagen del contacto y devuelve su URL pública.
     * - Emite eventos i18n para éxito/fracaso.
     */
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
                    viewModelScope.launch { _uiEvent.emit(UiText.res(R.string.contacts_photo_uploaded)) }
                }
            }
            .addOnFailureListener {
                isUploading.value = false
                Log.e("SubirImagen", "Error: ", it) // <-- logs intactos
                onResult(null)
                viewModelScope.launch {
                    _uiEvent.emit(
                        UiText.res(
                            R.string.contacts_error_uploading_photo_with_reason,
                            it.localizedMessage ?: "—"
                        )
                    )
                }
            }
    }
}

