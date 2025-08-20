package com.dls.pymetask.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.preferences.DefaultAppPreferences
import com.dls.pymetask.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()


    fun checkLoginStatus(context: Context) {
        val user = FirebaseAuth.getInstance().currentUser
        val marcada = authRepository.sesionMarcada(context)

        Log.d("AuthCheck", "UID: ${user?.uid}, Email: ${user?.email}, Marcada=$marcada")


        // nueva funcion para mantener login en firebase con dispositivos nuevos
        if (user != null || marcada) {
            _isUserLoggedIn.value = true
        } else {
            _isUserLoggedIn.value = false
        }
    }

}
