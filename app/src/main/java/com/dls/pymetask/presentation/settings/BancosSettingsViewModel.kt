package com.dls.pymetask.presentation.settings


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.local.AccountPrefs
import com.dls.pymetask.data.remote.bank.AccountDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.dls.pymetask.data.remote.bank.auth.TokenStore
import com.dls.pymetask.domain.repository.BankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * VM sencillo que expone si hay token guardado y permite desconectar.
 * Mantiene los nombres y patrón del proyecto.
 */
@HiltViewModel
class BancosSettingsViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val bankRepo: BankRepository,
    private val prefs: AccountPrefs
) : ViewModel() {

    private val _conectado = MutableStateFlow(tokenStore.load() != null)
    val conectado = _conectado.asStateFlow()

    val selectedAccountId: StateFlow<String?> =
        prefs.selectedAccountId().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _accounts = MutableStateFlow<List<AccountDto>>(emptyList())
    val accounts = _accounts.asStateFlow()




    /** Carga cuentas si hay token */
    fun cargarCuentas() {
        if (!_conectado.value) {
            _accounts.value = emptyList(); return
        }
        viewModelScope.launch {
            runCatching { bankRepo.fetchAccounts() }
                .onSuccess { _accounts.value = it }
                .onFailure { _accounts.value = emptyList() }
        }
    }

    fun seleccionarCuenta(id: String) {
        viewModelScope.launch { prefs.setSelectedAccountId(id) }
    }

    fun desconectar() {
        tokenStore.clear()
        _conectado.value = false
        _accounts.value = emptyList()
    }
}
