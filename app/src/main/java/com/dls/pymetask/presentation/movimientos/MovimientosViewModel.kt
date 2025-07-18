package com.dls.pymetask.presentation.movimientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.model.Movimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class MovimientosViewModel : ViewModel() {

    private val _movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
    val movimientos: StateFlow<List<Movimiento>> = _movimientos.asStateFlow()

    private val _tipoSeleccionado = MutableStateFlow("Todos") // "Ingresos", "Gastos" o "Todos"
    val tipoSeleccionado: StateFlow<String> = _tipoSeleccionado.asStateFlow()

    fun setTipo(tipo: String) {
        _tipoSeleccionado.value = tipo
    }

    fun loadMovimientos() {
        // Aquí simulamos datos. Luego conectarás con Firebase Firestore
        val lista = listOf(
            Movimiento("1", "Pago recibido", "Cliente Juan", 800.0, true, Date()),
            Movimiento("2", "Compra material", "Proveedor X", 50.0, false, Date())
        )
        _movimientos.value = lista
    }

    fun addMovimiento(mov: Movimiento) {
        _movimientos.value = _movimientos.value + mov
    }

    fun updateMovimiento(updated: Movimiento) {
        _movimientos.value = _movimientos.value.map {
            if (it.id == updated.id) updated else it
        }
    }

    fun getMovimientoById(id: String): Movimiento? {
        return _movimientos.value.find { it.id == id }
    }
}
