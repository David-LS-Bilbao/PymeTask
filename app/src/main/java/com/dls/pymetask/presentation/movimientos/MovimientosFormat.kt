package com.dls.pymetask.presentation.movimientos

import com.dls.pymetask.domain.model.Movimiento
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/** Mapper dominio -> UI para esta pantalla. */
internal fun Movimiento.toUi(): MovimientoUi {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaFormateada = sdf.format(Date(this.fecha))
    val importeUi = if (this.ingreso) this.cantidad else -this.cantidad
    return MovimientoUi(
        id = this.id,
        titulo = this.titulo,
        fechaTexto = fechaFormateada,
        importe = importeUi,
        fechaMillis = this.fecha
    )
}

/** Calcula ingresos, gastos (positivo) y saldo. */
internal fun calcularTotales(movimientos: List<MovimientoUi>): Triple<Double, Double, Double> {
    val ingresos = movimientos.filter { it.importe >= 0 }.sumOf { it.importe }
    val gastosAbs = movimientos.filter { it.importe < 0 }.sumOf { -it.importe }
    val saldo = ingresos - gastosAbs
    return Triple(ingresos, gastosAbs, saldo)
}

/** Formatea en EUR/ES; withSign añade +/− en pantalla. */
internal fun Double.toCurrency(withSign: Boolean = false): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
        currency = Currency.getInstance("EUR")
        maximumFractionDigits = 2
    }
    val base = nf.format(this)
    if (!withSign) return base
    val absText = nf.format(kotlin.math.abs(this))
    return if (this >= 0) "+$absText" else "-$absText"
}
