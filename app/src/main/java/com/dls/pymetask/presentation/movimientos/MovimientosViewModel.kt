package com.dls.pymetask.presentation.movimientos

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Movimiento
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
    private val repository: MovimientoRepository
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



    init { loadMovimientos()}
    fun onMonthSelected(month: Int) {
        _selectedMonth.value = month
    }
    fun onYearSelected(year: Int) {
        _selectedYear.value = year
    }
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
