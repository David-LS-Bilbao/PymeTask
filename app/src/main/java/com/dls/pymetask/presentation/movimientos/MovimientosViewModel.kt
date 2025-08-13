package com.dls.pymetask.presentation.movimientos

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.domain.repository.BankRepository
import com.dls.pymetask.domain.repository.MovimientoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@SuppressLint("AutoboxingStateCreation")
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MovimientosViewModel @Inject constructor(
    private val repository: MovimientoRepository,
    private val bankRepository: BankRepository // <-- inyecta


) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(LocalDate.now().monthValue)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()
    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // Flujo con todos los movimientos del usuario (todos los meses)
    private val _movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
    val movimientos: StateFlow<List<Movimiento>> = _movimientos.asStateFlow()
    private val _tipoSeleccionado = MutableStateFlow("Todos") // "Ingresos", "Gastos" o "Todos"
    val tipoSeleccionado: StateFlow<String> = _tipoSeleccionado.asStateFlow()
    fun setTipo(tipo: String) {
        _tipoSeleccionado.value = tipo
    }



    // Estado de sincronización para la UI
    var syncing by mutableStateOf(false)
        private set
    var lastSyncResult by mutableStateOf<String?>(null)
        private set

    /**
     * Sincroniza un MES concreto de una cuenta bancaria:
     * - accountId viene del proveedor (temporalmente, introduce un hardcode para probar).
     * - year/month0 definen el rango
     */
    fun syncBancoMes(accountId: String, year: Int, month0: Int) {
        viewModelScope.launch {
            syncing = true
            lastSyncResult = null
            val (fromMillis, toMillis) = monthBounds(year, month0)
            val result = bankRepository.syncAccount(accountId, fromMillis, toMillis)
            syncing = false
            lastSyncResult = result.fold(
                onSuccess = { count -> "Importadas/actualizadas: $count" },
                onFailure = { t -> "Error: ${t.message ?: t::class.java.simpleName}" }
            )
        }
    }

    /** Devuelve (inicio, fin) de mes en epoch millis */
    private fun monthBounds(year: Int, month0: Int): Pair<Long, Long> {
        val c1 = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month0)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val c2 = (c1.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.MONTH, 1)
            add(java.util.Calendar.MILLISECOND, -1)
        }
        return c1.timeInMillis to c2.timeInMillis
    }











    init { loadMovimientos()}

    private fun loadMovimientos() {
        viewModelScope.launch {
            repository.getMovimientos().collect { lista ->
                _movimientos.value = lista
            }
        }
    }
    fun addMovimiento(mov: Movimiento) {
        viewModelScope.launch {
            try {
                repository.insertMovimiento(mov)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun updateMovimiento(updated: Movimiento) {
        viewModelScope.launch {
            try {
                repository.updateMovimiento(updated) // persistencia
                _movimientos.value = _movimientos.value.map {
                    if (it.id == updated.id) updated else it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getMovimientoById(id: String): Movimiento? {
        return _movimientos.value.find { it.id == id }
    }
    fun deleteMovimiento(id: String) {
        viewModelScope.launch {
            repository.deleteMovimiento(id)
        }
    }
}
