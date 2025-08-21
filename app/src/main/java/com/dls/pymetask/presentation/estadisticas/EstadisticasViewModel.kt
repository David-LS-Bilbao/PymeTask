package com.dls.pymetask.presentation.estadisticas

// Importa utilidades/calculadoras puras
import androidx.lifecycle.ViewModel
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.presentation.estadisticas.Modo.MES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

enum class Modo { MES, COMPARAR }

data class EstadisticasUiState(
    val modo: Modo = MES,
    val periodo: Periodo = Periodo.MES,
    val filtro: FiltroTipo = FiltroTipo.TODO,
    val customRange: Pair<Long, Long>? = null,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month0: Int = Calendar.getInstance().get(Calendar.MONTH),
    val yearB: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month0B: Int = (Calendar.getInstance().get(Calendar.MONTH) - 1).coerceAtLeast(0),
    val movimientos: List<Movimiento> = emptyList()
) {
    // Derivados (calculados on-demand con funciones puras)
    val listaA: List<Movimiento> get() = movimientos.filtrarPorPeriodo(periodo, customRange, year, month0)
    val listaB: List<Movimiento> get() = if (modo == Modo.COMPARAR) movimientos.filter { it.fecha.inMonth(yearB, month0B) } else emptyList()
    val listaFiltradaA: List<Movimiento> get() = listaA.filtrarPorTipo(filtro)

    val totA get() = totalesMes(listaFiltradaA)
    val totB get() = totalesMes(listaB)
    val diarioA: SerieDiaria get() = if (periodo == Periodo.MES) seriesDiarias(listaA, year, month0) else SerieDiaria(0, emptyList(), emptyList(), emptyList())
    val tendencia12m: List<MonthAgg> get() = aggregateLastMonths(movimientos, endYear = year, endMonth0 = month0, count = 12)

    val tituloMesA get() = monthYearTitle(year, month0)
    val tituloMesB get() = monthYearTitle(yearB, month0B)


}

@HiltViewModel
class EstadisticasViewModel @Inject constructor(

) : ViewModel() {

    private val _ui = MutableStateFlow(EstadisticasUiState())
    val ui: StateFlow<EstadisticasUiState> = _ui.asStateFlow()

    /** Puente: pásame la lista desde tu MovimientosViewModel */
    fun setMovimientos(list: List<Movimiento>) {
        _ui.update { it.copy(movimientos = list) }
    }

    // Intents de UI
    fun toggleModo() = _ui.update { it.copy(modo = if (it.modo == MES) Modo.COMPARAR else MES) }
    fun setPeriodo(p: Periodo) = _ui.update { it.copy(periodo = p) }
    fun setFiltro(f: FiltroTipo) = _ui.update { it.copy(filtro = f) }
    fun setCustomRange(range: Pair<Long, Long>?) = _ui.update { it.copy(customRange = range, periodo = Periodo.PERSONALIZADO) }

    fun mesPrevA() = _ui.update {
        val c = Calendar.getInstance().apply { set(it.year, it.month0, 1); add(Calendar.MONTH, -1) }
        it.copy(year = c.get(Calendar.YEAR), month0 = c.get(Calendar.MONTH))
    }
    fun mesNextA() = _ui.update {
        val c = Calendar.getInstance().apply { set(it.year, it.month0, 1); add(Calendar.MONTH, 1) }
        it.copy(year = c.get(Calendar.YEAR), month0 = c.get(Calendar.MONTH))
    }
    fun mesPrevB() = _ui.update {
        val c = Calendar.getInstance().apply { set(it.yearB, it.month0B, 1); add(Calendar.MONTH, -1) }
        it.copy(yearB = c.get(Calendar.YEAR), month0B = c.get(Calendar.MONTH))
    }
    fun mesNextB() = _ui.update {
        val c = Calendar.getInstance().apply { set(it.yearB, it.month0B, 1); add(Calendar.MONTH, 1) }
        it.copy(yearB = c.get(Calendar.YEAR), month0B = c.get(Calendar.MONTH))
    }
}
