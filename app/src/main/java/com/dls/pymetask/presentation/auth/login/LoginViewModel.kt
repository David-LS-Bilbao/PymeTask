package com.dls.pymetask.presentation.auth.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Estado que indica si se está procesando una operación
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Estado que guarda el mensaje de error si ocurre
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Estado que indica si el login fue exitoso
    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    // Función para iniciar sesión con email y contraseña
    fun login(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _isLoading.value = false

            result.onSuccess {
                _loginSuccess.value = true
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }

    // Función para registrarse (si decides incluirlo en la misma pantalla)
    fun register(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = authRepository.register(email, password)
            _isLoading.value = false

            result.onSuccess {
                _loginSuccess.value = true
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
        }
    }
    fun setError(message: String) {
        _errorMessage.value = message
    }

}
