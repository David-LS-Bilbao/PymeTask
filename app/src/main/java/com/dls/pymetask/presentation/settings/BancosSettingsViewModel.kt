package com.dls.pymetask.presentation.settings


import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.dls.pymetask.data.remote.bank.auth.TokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * VM sencillo que expone si hay token guardado y permite desconectar.
 * Mantiene los nombres y patrón del proyecto.
 */
@HiltViewModel
class BancosSettingsViewModel @Inject constructor(
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _conectado = MutableStateFlow(tokenStore.load() != null)
    val conectado = _conectado.asStateFlow()

    /** Borra tokens (logout bancario local). */
    fun desconectar() {
        tokenStore.clear()
        _conectado.value = false
    }
}
