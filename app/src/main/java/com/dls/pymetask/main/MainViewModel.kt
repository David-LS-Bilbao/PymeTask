package com.dls.pymetask.main


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de autenticación mostrado en la capa UI.
 */
sealed class AuthState {
    data object Checking : AuthState()           // aún verificando
    data object LoggedIn : AuthState()           // usuario autenticado o sesión marcada
    data object LoggedOut : AuthState()          // sin sesión
}

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository   // Debe resolver internamente preferencias/Firebase
) : ViewModel() {

    // Flujo observable con el estado tipado
    private val _authState = MutableStateFlow<AuthState>(AuthState.Checking)
    val authState = _authState.asStateFlow()

    /**
     * Verifica el estado de login sin requerir Context:
     * - Si Firebase tiene usuario o hay "sesión recordada" en preferencias -> LoggedIn
     * - En caso contrario -> LoggedOut
     */
    fun checkLoginStatus() {
        viewModelScope.launch {
            // Repositorio debe proveer ambas comprobaciones
            val user = authRepository.isUserLoggedIn()  // encapsula FirebaseAuth.getInstance().currentUser
            val marcada = authRepository.sesionMarcada(context)   // encapsula preferencias/almacenamiento

            Log.d("AuthCheck", "hayUsuario=$user, marcada=$marcada")

            _authState.value = if (user || marcada) {
                AuthState.LoggedIn
            } else {
                AuthState.LoggedOut
            }
        }
    }
}



















//
//import android.content.Context
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.dls.pymetask.data.preferences.DefaultAppPreferences
//import com.dls.pymetask.domain.repository.AuthRepository
//import com.google.firebase.auth.FirebaseAuth
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class MainViewModel @Inject constructor(
//    private val authRepository: AuthRepository
//) : ViewModel() {
//
//    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
//    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()
//
//
//    fun checkLoginStatus(context: Context) {
//        val user = FirebaseAuth.getInstance().currentUser
//        val marcada = authRepository.sesionMarcada(context)
//
//        Log.d("AuthCheck", "UID: ${user?.uid}, Email: ${user?.email}, Marcada=$marcada")
//
//
//        // nueva funcion para mantener login en firebase con dispositivos nuevos
//        if (user != null || marcada) {
//            _isUserLoggedIn.value = true
//        } else {
//            _isUserLoggedIn.value = false
//        }
//    }
//
//}
