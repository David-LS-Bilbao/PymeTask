package com.dls.pymetask.presentation.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EditarPerfilViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre

    private val _fotoUrl = MutableStateFlow<String?>(null)
    val fotoUrl: StateFlow<String?> = _fotoUrl
    private val _telefono = MutableStateFlow("")
    val telefono: StateFlow<String> = _telefono
    private val _direccion = MutableStateFlow("")
    val direccion: StateFlow<String> = _direccion

    fun cargarDatosPerfil() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                _nombre.value = doc.getString("nombre") ?: ""
                _fotoUrl.value = doc.getString("fotoUrl")
                // Agrega más campos según sea necesario
                _telefono.value = doc.getString("telefono") ?: ""
                _direccion.value = doc.getString("direccion") ?: ""
            }
    }

    fun actualizarPerfil(nuevoNombre: String, nuevaFotoUri: Uri?,
                         nuevoTelefono: String, nuevoDireccion: String,
                         onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            if (nuevaFotoUri != null) {
                val ref = storage.reference.child("fotos_perfil/$uid-${UUID.randomUUID()}.jpg")
                ref.putFile(nuevaFotoUri).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        guardarEnFirestore(uid,
                            nuevoNombre,
                            nuevoTelefono,
                            nuevoDireccion,
                            uri.toString(), onSuccess, onError)
                    }
                }.addOnFailureListener { onError(it.message ?: "Error subiendo imagen") }
            } else {
                guardarEnFirestore(uid, nuevoNombre, _fotoUrl.value, nuevoTelefono, nuevoDireccion, onSuccess, onError)
            }
        }
    }

    private fun guardarEnFirestore(uid: String,
                                   nombre: String,
                                   fotoUrl: String?,
                                   telefono: String,
                                   direccion: String,
                                   onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userMap = mapOf(
            "nombre" to nombre,
            "fotoUrl" to fotoUrl,
            // Agrega más campos según sea necesario
             "telefono" to telefono,
             "direccion" to direccion
        )
        firestore.collection("usuarios").document(uid)
            .set(userMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error actualizando perfil") }
    }
}
