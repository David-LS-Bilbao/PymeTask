package com.dls.pymetask.presentation.estadisticas



import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.dls.pymetask.domain.model.Movimiento

// ===== Modelos de cálculo (UI-agnósticos) =====
data class Totales(val ingresos: Double, val gastosAbs: Double, val saldo: Double)

data class SerieDiaria(
    val dias: Int,
    val ingresosPorDia: List<Double>,
    val gastosPorDia: List<Double>,
    val saldoAcumulado: List<Double>
)

data class MonthAgg(
    val year: Int,
    val month0: Int,            // 0..11
    val ingresos: Double,
    val gastos: Double
) { val saldo: Double get() = ingresos - gastos }

// ===== Helpers de fechas =====
internal fun Long.inMonth(year: Int, month0: Int): Boolean {
    val c = Calendar.getInstance().apply { timeInMillis = this@inMonth }
    return c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month0
}

internal fun Long.isInDayRange(startMillis: Long, endMillis: Long): Boolean =
    this in startMillis..endMillis

internal fun Calendar.startOfDay(): Long {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    return timeInMillis
}
internal fun Calendar.endOfDay(): Long {
    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    return timeInMillis
}

fun daysInMonth(year: Int, month0: Int): Int =
    Calendar.getInstance().apply {
        set(Calendar.YEAR, year); set(Calendar.MONTH, month0); set(Calendar.DAY_OF_MONTH, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

fun monthYearTitle(year: Int, month0: Int): String {
    val c = Calendar.getInstance().apply { set(year, month0, 1) }
    val f = SimpleDateFormat("LLLL yyyy", Locale("es", "ES"))
    val raw = f.format(c.time)
    return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es","ES")) else it.toString() }
}

fun monthShortEs(year: Int, month0: Int): String {
    val c = Calendar.getInstance().apply { set(year, month0, 1) }
    val s = SimpleDateFormat("LLL", Locale("es","ES")).format(c.time)
    return s.replace(".", "").uppercase()
}

// ===== Cálculos principales =====
fun totalesMes(lista: List<Movimiento>): Totales {
    val ing = lista.filter { it.ingreso }.sumOf { it.cantidad }
    val gas = lista.filter { !it.ingreso }.sumOf { it.cantidad }
    return Totales(ing, gas, ing - gas)
}

fun seriesDiarias(lista: List<Movimiento>, year: Int, month0: Int): SerieDiaria {
    val dias = daysInMonth(year, month0)
    if (dias <= 0) return SerieDiaria(0, emptyList(), emptyList(), emptyList())

    val ingresos = MutableList(dias) { 0.0 }
    val gastos = MutableList(dias) { 0.0 }
    val cal = Calendar.getInstance()

    lista.forEach { m ->
        cal.timeInMillis = m.fecha
        if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month0) {
            val d = cal.get(Calendar.DAY_OF_MONTH) - 1
            if (d in 0 until dias) {
                if (m.ingreso) ingresos[d] += m.cantidad else gastos[d] += m.cantidad
            }
        }
    }

    val saldo = MutableList(dias) { 0.0 }
    var acc = 0.0
    for (i in 0 until dias) {
        acc += ingresos[i]; acc -= gastos[i]; saldo[i] = acc
    }
    return SerieDiaria(dias, ingresos, gastos, saldo)
}

sealed class Periodo {
    data object HOY : Periodo()
    data object SEMANA : Periodo()
    data object MES : Periodo()
    data object PERSONALIZADO : Periodo()
    companion object {
        fun values(): Array<Periodo> {
            return arrayOf(HOY, SEMANA, MES, PERSONALIZADO)
        }

        fun valueOf(value: String): Periodo {
            return when (value) {
                "HOY" -> HOY
                "SEMANA" -> SEMANA
                "MES" -> MES
                "PERSONALIZADO" -> PERSONALIZADO
                else -> throw IllegalArgumentException("No object com.dls.pymetask.presentation.estadisticas.Periodo.$value")
            }
        }
    }
}
sealed class FiltroTipo {
    data object TODO : FiltroTipo()
    data object INGRESO : FiltroTipo()
    data object GASTO : FiltroTipo()
    companion object {
        fun values(): Array<FiltroTipo> {
            return arrayOf(TODO, INGRESO, GASTO)
        }

        fun valueOf(value: String): FiltroTipo {
            return when (value) {
                "TODO" -> TODO
                "INGRESO" -> INGRESO
                "GASTO" -> GASTO
                else -> throw IllegalArgumentException("No object com.dls.pymetask.presentation.estadisticas.FiltroTipo.$value")
            }
        }
    }
}

fun List<Movimiento>.filtrarPorPeriodo(
    periodo: Periodo,
    custom: Pair<Long, Long>?,
    year: Int,
    month0: Int
): List<Movimiento> = when (periodo) {
    Periodo.MES -> filter { it.fecha.inMonth(year, month0) }
    Periodo.HOY -> {
        val hoy = Calendar.getInstance()
        val start = (hoy.clone() as Calendar).startOfDay()
        val end = (hoy.clone() as Calendar).endOfDay()
        filter { it.fecha.isInDayRange(start, end) }
    }
    Periodo.SEMANA -> {
        val now = Calendar.getInstance()
        val startCal = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }
        val start = startCal.startOfDay()
        val end = (now.clone() as Calendar).endOfDay()
        filter { it.fecha.isInDayRange(start, end) }
    }
    Periodo.PERSONALIZADO -> {
        val (s, e) = custom ?: return emptyList()
        filter { it.fecha.isInDayRange(s, e) }
    }
}

fun List<Movimiento>.filtrarPorTipo(filtro: FiltroTipo): List<Movimiento> = when (filtro) {
    FiltroTipo.TODO -> this
    FiltroTipo.INGRESO -> filter { it.ingreso }
    FiltroTipo.GASTO -> filter { !it.ingreso }
}

fun aggregateLastMonths(
    movimientos: List<Movimiento>,
    endYear: Int,
    endMonth0: Int,
    count: Int = 12
): List<MonthAgg> {
    val cal = Calendar.getInstance()
    val keys = ArrayList<Pair<Int, Int>>(count)
    cal.set(endYear, endMonth0, 1)
    repeat(count) {
        keys.add(cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH))
        cal.add(Calendar.MONTH, -1)
    }
    keys.reverse()

    val acc = keys.associateWith { 0.0 to 0.0 }.toMutableMap()
    val tmp = Calendar.getInstance()
    movimientos.forEach { m ->
        tmp.timeInMillis = m.fecha
        val k = tmp.get(Calendar.YEAR) to tmp.get(Calendar.MONTH)
        if (k in acc) {
            val (ing, gas) = acc[k]!!
            acc[k] = if (m.ingreso) (ing + m.cantidad) to gas else ing to (gas + m.cantidad)
        }
    }

    return keys.map { (y, m) ->
        val (ing, gas) = acc[y to m] ?: (0.0 to 0.0)
        MonthAgg(y, m, ingresos = ing, gastos = gas)
    }
}
