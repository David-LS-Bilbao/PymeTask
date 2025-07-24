
package com.dls.pymetask.presentation.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class PerfilUserViewModel @Inject constructor() : ViewModel() {

    private val _perfil = MutableStateFlow(PerfilUsuario())
    val perfil: StateFlow<PerfilUsuario> get() = _perfil

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        obtenerPerfil()
    }

    private fun obtenerPerfil() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: ""
                    val email = auth.currentUser?.email ?: ""
                    val foto = doc.getString("fotoUrl")
                    val telefono = doc.getString("telefono") ?: ""
                    val direccion = doc.getString("direccion") ?: ""
                    _perfil.value = PerfilUsuario(nombre, email, foto, telefono, direccion)
                }
        }
    }
}

data class PerfilUsuario(
    val nombre: String = "",
    val email: String = "",
    val fotoUrl: String? = null,
    val telefono: String = "",
    val direccion: String = ""

)