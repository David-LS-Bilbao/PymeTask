// NUEVA VERSION
@file:Suppress("DEPRECATION")

package com.dls.pymetask.presentation.estadisticas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.presentation.movimientos.MovimientosViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme

// ---------- Tipos/top-level (evita enums/clases locales) ----------
private enum class Modo { MES, COMPARAR }

private data class Totales(val ingresos: Double, val gastosAbs: Double, val saldo: Double)

private data class SerieDiaria(
    val dias: Int,
    val ingresosPorDia: List<Double>,
    val gastosPorDia: List<Double>,
    val saldoAcumulado: List<Double>
)

// ---------- Helpers de lógica (no composables) ----------
private fun Long.inMonth(year: Int, month0: Int): Boolean {
    val c = Calendar.getInstance().apply { timeInMillis = this@inMonth }
    return c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month0
}

private fun daysInMonth(year: Int, month0: Int): Int {
    val c = Calendar.getInstance()
    c.set(Calendar.YEAR, year)
    c.set(Calendar.MONTH, month0)
    c.set(Calendar.DAY_OF_MONTH, 1)
    return c.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun totalesMes(lista: List<Movimiento>): Totales {
    val ing = lista.filter { it.ingreso }.sumOf { it.cantidad }
    val gas = lista.filter { !it.ingreso }.sumOf { it.cantidad }
    return Totales(ing, gas, ing - gas)
}

private fun seriesDiarias(lista: List<Movimiento>, year: Int, month0: Int): SerieDiaria {
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
        acc += ingresos[i]
        acc -= gastos[i]
        saldo[i] = acc
    }
    return SerieDiaria(dias, ingresos, gastos, saldo)
}

private fun monthYearTitle(year: Int, month0: Int): String {
    val c = Calendar.getInstance().apply { set(year, month0, 1) }
    val f = SimpleDateFormat("LLLL yyyy", Locale("es", "ES"))
    val raw = f.format(c.time)
    return raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
}

// ===============================
//  PANTALLA
// ===============================
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController,
    viewModel: MovimientosViewModel = hiltViewModel()
) {
    val movimientos by viewModel.movimientos.collectAsState(initial = emptyList())

    val currency = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val sdfFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")) }

    var modo by remember { mutableStateOf(Modo.MES) }

    val hoy = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(hoy.get(Calendar.YEAR)) }
    var month0 by remember { mutableIntStateOf(hoy.get(Calendar.MONTH)) }

    var yearB by remember { mutableIntStateOf(year) }
    var month0B by remember { mutableIntStateOf((month0 - 1).coerceAtLeast(0)) }

    val listaMesA = remember(movimientos, year, month0) { movimientos.filter { it.fecha.inMonth(year, month0) } }
    val listaMesB = remember(movimientos, yearB, month0B, modo) {
        if (modo == Modo.COMPARAR) movimientos.filter { it.fecha.inMonth(yearB, month0B) } else emptyList()
    }

    val totA = remember(listaMesA) { totalesMes(listaMesA) }
    val totB = remember(listaMesB) { totalesMes(listaMesB) }

    val diarioA = remember(listaMesA, year, month0) { seriesDiarias(listaMesA, year, month0) }

    val tituloMesA = remember(year, month0) { monthYearTitle(year, month0) }
    val tituloMesB = remember(yearB, month0B) { monthYearTitle(yearB, month0B) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        modo = if (modo == Modo.MES) Modo.COMPARAR else Modo.MES
                    }) {
                        Icon(
                            imageVector = if (modo == Modo.MES)
                                Icons.AutoMirrored.Filled.CompareArrows
                            else
                                Icons.Filled.DateRange,
                            contentDescription = "Cambiar modo"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selector de periodo
            item {
                if (modo == Modo.MES) {
                    SelectorMes(
                        title = "Mes",
                        year = year,
                        monthZero = month0,
                        onPrev = {
                            val c = Calendar.getInstance().apply { set(year, month0, 1); add(Calendar.MONTH, -1) }
                            year = c.get(Calendar.YEAR); month0 = c.get(Calendar.MONTH)
                        },
                        onNext = {
                            val c = Calendar.getInstance().apply { set(year, month0, 1); add(Calendar.MONTH, 1) }
                            year = c.get(Calendar.YEAR); month0 = c.get(Calendar.MONTH)
                        }
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectorMes(
                            title = "Mes A",
                            year = year,
                            monthZero = month0,
                            onPrev = {
                                val c = Calendar.getInstance().apply { set(year, month0, 1); add(Calendar.MONTH, -1) }
                                year = c.get(Calendar.YEAR); month0 = c.get(Calendar.MONTH)
                            },
                            onNext = {
                                val c = Calendar.getInstance().apply { set(year, month0, 1); add(Calendar.MONTH, 1) }
                                year = c.get(Calendar.YEAR); month0 = c.get(Calendar.MONTH)
                            }
                        )
                        SelectorMes(
                            title = "Mes B",
                            year = yearB,
                            monthZero = month0B,
                            onPrev = {
                                val c = Calendar.getInstance().apply { set(yearB, month0B, 1); add(Calendar.MONTH, -1) }
                                yearB = c.get(Calendar.YEAR); month0B = c.get(Calendar.MONTH)
                            },
                            onNext = {
                                val c = Calendar.getInstance().apply { set(yearB, month0B, 1); add(Calendar.MONTH, 1) }
                                yearB = c.get(Calendar.YEAR); month0B = c.get(Calendar.MONTH)
                            }
                        )
                    }
                }
            }

            // Resumen superior
            item {
                if (movimientos.isEmpty()) {
                    Tarjeta(title = "Resumen") { TextoVacio("Aún no hay movimientos") }
                } else if (modo == Modo.MES) {
                    ResumenGlobal(
                        saldo = currency.format(totA.saldo),
                        ingresos = currency.format(totA.ingresos),
                        gastos = currency.format(totA.gastosAbs)
                    )
                } else {
                    ResumenComparativa(
                        tituloA = tituloMesA,
                        tituloB = tituloMesB,
                        ingresosA = currency.format(totA.ingresos),
                        gastosA = currency.format(totA.gastosAbs),
                        saldoA = currency.format(totA.saldo),
                        ingresosB = currency.format(totB.ingresos),
                        gastosB = currency.format(totB.gastosAbs),
                        saldoB = currency.format(totB.saldo)
                    )
                }
            }

            // Gráficos
            if (modo == Modo.MES) {
                item {
                    Tarjeta(title = "Ingresos vs Gastos por día — $tituloMesA") {
                        if (diarioA.dias == 0) TextoVacio("No hay datos en este mes.")
                        else BarChartIngresosGastosPorDia(
                            ingresosPorDia = diarioA.ingresosPorDia,
                            gastosPorDia = diarioA.gastosPorDia,
                            height = 180.dp
                        )
                    }
                }
                item {
                    Tarjeta(title = "Saldo acumulado — $tituloMesA") {
                        if (diarioA.dias == 0) TextoVacio("No hay datos en este mes.")
                        else LineChartSaldoAcumulado(
                            saldoAcumulado = diarioA.saldoAcumulado,
                            height = 180.dp
                        )
                    }
                }
            } else {
                item {
                    Tarjeta(title = "Comparativa de totales") {
                        if (listaMesA.isEmpty() && listaMesB.isEmpty()) {
                            TextoVacio("No hay datos para comparar.")
                        } else {
                            BarChartComparativaTotales(
                                tituloA = tituloMesA,
                                tituloB = tituloMesB,
                                ingresosA = totA.ingresos,
                                gastosA = totA.gastosAbs,
                                saldoA = totA.saldo,
                                ingresosB = totB.ingresos,
                                gastosB = totB.gastosAbs,
                                saldoB = totB.saldo,
                                height = 200.dp
                            )
                        }
                    }
                }
            }

            // Lista ligera del mes A
            if (modo == Modo.MES && listaMesA.isNotEmpty()) {
                item { Text("Movimientos de $tituloMesA", style = MaterialTheme.typography.titleMedium) }
                items(
                    items = listaMesA.sortedByDescending { it.fecha },
                    key = { m: Movimiento -> m.id }
                ) { mov: Movimiento ->
                    ListItem(
                        headlineContent = { Text(mov.titulo) },
                        supportingContent = {
                            Text(sdfFecha.format(java.util.Date(mov.fecha)))
                        },
                        trailingContent = {
                            val signed = if (mov.ingreso) mov.cantidad else -mov.cantidad
                            Text(
                                text = currency.format(signed),
                                color = if (mov.ingreso) colorScheme.primary else colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

// ===============================
//  Composables auxiliares
// ===============================
@Composable
private fun Tarjeta(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SelectorMes(
    title: String,
    year: Int,
    monthZero: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val titulo = remember(year, monthZero) { monthYearTitle(year, monthZero) }
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 8.dp))
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onPrev) { Text("←") }
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onNext) { Text("→") }
        }
    }
}

@Composable
private fun ResumenGlobal(saldo: String, ingresos: String, gastos: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card { Column(Modifier.padding(16.dp)) { Text("Saldo"); Text(saldo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) } }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Card(Modifier.weight(1f)) { Column(Modifier.padding(16.dp)) { Text("Ingresos"); Text(ingresos, fontWeight = FontWeight.SemiBold) } }
            Card(Modifier.weight(1f)) { Column(Modifier.padding(16.dp)) { Text("Gastos");   Text(gastos,   fontWeight = FontWeight.SemiBold) } }
        }
    }
}

@Composable
private fun ResumenComparativa(
    tituloA: String, tituloB: String,
    ingresosA: String, gastosA: String, saldoA: String,
    ingresosB: String, gastosB: String, saldoB: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Card(Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp)) {
                    Text(tituloA, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text("Saldo: $saldoA"); Text("Ingresos: $ingresosA"); Text("Gastos: $gastosA")
                }
            }
            Card(Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp)) {
                    Text(tituloB, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text("Saldo: $saldoB"); Text("Ingresos: $ingresosB"); Text("Gastos: $gastosB")
                }
            }
        }
    }
}

@Composable private fun TextoVacio(text: String) {
    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        Text(text, color = colorScheme.onSurfaceVariant)
    }
}

// ===============================
//  Gráficos (Canvas) — sin composables dentro
// ===============================
@Composable
private fun BarChartIngresosGastosPorDia(
    ingresosPorDia: List<Double>,
    gastosPorDia: List<Double>,
    height: Dp
) {
    // ✅ colores capturados fuera del Canvas
    val colorA = colorScheme.primary
    val colorB = colorScheme.error
    val dias = ingresosPorDia.size
    if (dias == 0) return
    val maxValor = max(ingresosPorDia.maxOrNull() ?: 0.0, gastosPorDia.maxOrNull() ?: 0.0).coerceAtLeast(1e-9)

    Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
        val w = size.width
        val h = size.height
        val cellW = w / dias
        val groupW = cellW * 0.85f
        val gap = 4f
        val barW = (groupW - gap) / 2f
        val baseY = h * 0.95f
        val usableH = h * 0.9f

        for (i in 0 until dias) {
            val xStart = i * cellW + (cellW - groupW) / 2f

            val hIng = ((ingresosPorDia[i] / maxValor).toFloat() * usableH)
            val hGas = ((gastosPorDia[i] / maxValor).toFloat() * usableH)

            // Ingresos
            drawRect(
                color = colorA,
                topLeft = Offset(xStart, baseY - hIng),
                size = androidx.compose.ui.geometry.Size(barW, hIng)
            )
            // Gastos
            drawRect(
                color = colorB,
                topLeft = Offset(xStart + barW + gap, baseY - hGas),
                size = androidx.compose.ui.geometry.Size(barW, hGas)
            )
        }
    }
}

@Composable
private fun LineChartSaldoAcumulado(
    saldoAcumulado: List<Double>,
    height: Dp
) {

    // ✅ colores capturados fuera del Canvas
    val colorB = colorScheme.error

    val n = saldoAcumulado.size
    if (n == 0) return
    val maxV = saldoAcumulado.maxOrNull() ?: 0.0
    val minV = saldoAcumulado.minOrNull() ?: 0.0
    val rango = (maxV - minV).coerceAtLeast(1e-9)

    Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
        val w = size.width
        val h = size.height
        val contentW = w * 0.96f
        val contentH = h * 0.9f
        val left = (w - contentW) / 2f
        val bottom = h * 0.95f

        for (i in 0 until n - 1) {
            val x1 = left + contentW * (i.toFloat() / (n - 1))
            val x2 = left + contentW * ((i + 1).toFloat() / (n - 1))
            val y1 = bottom - ((saldoAcumulado[i] - minV) / rango).toFloat() * contentH
            val y2 = bottom - ((saldoAcumulado[i + 1] - minV) / rango).toFloat() * contentH

            drawLine(
                color = colorB,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 4f
            )
        }
    }
}

@Composable
private fun BarChartComparativaTotales(
    tituloA: String,
    tituloB: String,
    ingresosA: Double, gastosA: Double, saldoA: Double,
    ingresosB: Double, gastosB: Double, saldoB: Double,
    height: Dp
) {
    // ✅ colores capturados fuera del Canvas
    val colorA = colorScheme.primary
    val colorB = colorScheme.error


    val maxV = listOf(ingresosA, gastosA, saldoA, ingresosB, gastosB, saldoB).maxOrNull() ?: 0.0
    val norm = max(maxV, 1e-9)

    Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
        val w = size.width
        val h = size.height
        val grupos = 3
        val cellW = w / grupos
        val groupW = cellW * 0.7f
        val gap = 6f
        val barW = (groupW - gap) / 2f
        val baseY = h * 0.92f
        val usableH = h * 0.8f

        val valsA = listOf(ingresosA, gastosA, saldoA)
        val valsB = listOf(ingresosB, gastosB, saldoB)

        for (i in 0 until grupos) {
            val xStart = i * cellW + (cellW - groupW) / 2f
            val hA = ((valsA[i] / norm).toFloat() * usableH)
            val hB = ((valsB[i] / norm).toFloat() * usableH)

            drawRect(
                color = colorA,
                topLeft = Offset(xStart, baseY - hA),
                size = androidx.compose.ui.geometry.Size(barW, hA)
            )
            drawRect(
                color = colorB,
                topLeft = Offset(xStart + barW + gap, baseY - hB),
                size = androidx.compose.ui.geometry.Size(barW, hB)
            )
        }
    }

    // Leyenda sencilla para usar los títulos y evitar warnings de "unused"
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text("A: $tituloA", style = MaterialTheme.typography.labelMedium)
        Text("B: $tituloB", style = MaterialTheme.typography.labelMedium)
    }
}







//package com.dls.pymetask.presentation.estadisticas
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import com.dls.pymetask.domain.model.Movimiento // <-- usa SIEMPRE el modelo de dominio
//import com.dls.pymetask.presentation.movimientos.MovimientosViewModel
//import java.text.NumberFormat
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Locale
//
///**
// * Pantalla de Estadísticas:
// * - Lee la lista de movimientos desde el ViewModel (dominio: fecha: Long, ingreso: Boolean, cantidad: Double)
// * - Calcula resumen global y por MES/AÑO
// * - No usa stickyHeader ni java.time (máxima compatibilidad)
// */
//@RequiresApi(Build.VERSION_CODES.O)
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EstadisticasScreen(
//    navController: NavController,
//    viewModel: MovimientosViewModel = hiltViewModel()
//) {
//    // 1) Recoge los movimientos (StateFlow<List<Movimiento>>) y conviértelo en State para Compose
//    val movimientos: List<Movimiento> by viewModel.movimientos.collectAsState(initial = emptyList())
//
//    // 2) Formateadores de moneda y fechas (ES/EUR)
//    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
//    val mesFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "ES")) }
//    val fechaFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")) }
//
//    // 3) Totales globales usando 'ingreso' (no el signo)
//    val totalIngresos = remember(movimientos) { movimientos.filter { it.ingreso }.sumOf { it.cantidad } }
//    val totalGastosAbs = remember(movimientos) { movimientos.filter { !it.ingreso }.sumOf { it.cantidad } }
//    val saldo = remember(totalIngresos, totalGastosAbs) { totalIngresos - totalGastosAbs }
//
//    // 4) Agrupar por mes/año a partir de fecha (Long) con Calendar (0..11 para meses)
//    data class MonthKey(val year: Int, val month0: Int) // month0 en [0..11]
//
//    val porMesOrdenado: List<Pair<MonthKey, List<Movimiento>>> = remember(movimientos) {
//        // groupBy -> Map<MonthKey, List<Movimiento>>, luego a List<Pair<..>> para ordenar y destructurar
//        val agrupado: Map<MonthKey, List<Movimiento>> = movimientos.groupBy { mov ->
//            val cal = Calendar.getInstance().apply { timeInMillis = mov.fecha }
//            MonthKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
//        }
//        // Orden descendente: año, mes
//        agrupado.toList().sortedWith(
//            compareByDescending<Pair<MonthKey, List<Movimiento>>> { it.first.year }
//                .thenByDescending { it.first.month0 }
//        )
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Estadísticas") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        if (movimientos.isEmpty()) {
//            // Estado vacío simple
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Aún no hay movimientos para mostrar")
//            }
//            return@Scaffold
//        }
//
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            // ---- Resumen global ----
//            item {
//                ResumenGlobal(
//                    saldo = currencyFormat.format(saldo),
//                    ingresos = currencyFormat.format(totalIngresos),
//                    gastos = currencyFormat.format(totalGastosAbs)
//                )
//            }
//
//            // ---- Secciones por mes (encabezado + totales + lista breve) ----
//            porMesOrdenado.forEach { par: Pair<MonthKey, List<Movimiento>> ->
//                val key: MonthKey = par.first
//                val lista: List<Movimiento> = par.second
//
//                // Encabezado del mes
//                item(key = "header-${key.year}-${key.month0}") {
//                    val cal = Calendar.getInstance().apply {
//                        set(Calendar.YEAR, key.year)
//                        set(Calendar.MONTH, key.month0)
//                        set(Calendar.DAY_OF_MONTH, 1)
//                    }
//                    val headerText = mesFormatter.format(cal.time).replaceFirstChar { it.uppercase() }
//
//                    Surface(tonalElevation = 3.dp) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = headerText,
//                                style = MaterialTheme.typography.titleMedium,
//                                modifier = Modifier.padding(horizontal = 12.dp),
//                                fontWeight = FontWeight.SemiBold
//                            )
//                            Spacer(Modifier.weight(1f))
//
//                            val ingMes = lista.filter { it.ingreso }.sumOf { it.cantidad }
//                            val gasMesAbs = lista.filter { !it.ingreso }.sumOf { it.cantidad }
//                            val saldoMes = ingMes - gasMesAbs
//
//                            Text(
//                                text = currencyFormat.format(saldoMes),
//                                style = MaterialTheme.typography.titleMedium,
//                                modifier = Modifier.padding(horizontal = 12.dp),
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//
//                // Tarjeta de totales del mes
//                item(key = "totales-${key.year}-${key.month0}") {
//                    val ingMes = lista.filter { it.ingreso }.sumOf { it.cantidad }
//                    val gasMesAbs = lista.filter { !it.ingreso }.sumOf { it.cantidad }
//                    val saldoMes = ingMes - gasMesAbs
//
//                    TotalesMesCard(
//                        ingresos = currencyFormat.format(ingMes),
//                        gastos = currencyFormat.format(gasMesAbs),
//                        saldo = currencyFormat.format(saldoMes)
//                    )
//                }
//
//                // Lista ligera del mes (título, fecha, importe con color)
//                items(
//                    items = lista,
//                    key = { m: Movimiento -> m.id }
//                ) { mov: Movimiento ->
//                    val fechaTxt = fechaFormatter.format(java.util.Date(mov.fecha))
//                    MovimientoLineaLigera(
//                        titulo = mov.titulo,
//                        fecha = fechaTxt,
//                        importeFormateado = currencyFormat.format(
//                            if (mov.ingreso) mov.cantidad else -mov.cantidad
//                        ),
//                        esIngreso = mov.ingreso
//                    )
//                }
//            }
//        }
//    }
//}
//
///** Tarjeta del resumen global superior (saldo, ingresos, gastos) */
//@Composable
//private fun ResumenGlobal(
//    saldo: String,
//    ingresos: String,
//    gastos: String
//) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        Card {
//            Column(Modifier.padding(16.dp)) {
//                Text("Saldo total", style = MaterialTheme.typography.titleMedium)
//                Text(saldo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
//            }
//        }
//        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
//            Card(modifier = Modifier.weight(1f)) {
//                Column(Modifier.padding(16.dp)) {
//                    Text("Ingresos", style = MaterialTheme.typography.titleSmall)
//                    Text(ingresos, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
//                }
//            }
//            Card(modifier = Modifier.weight(1f)) {
//                Column(Modifier.padding(16.dp)) {
//                    Text("Gastos", style = MaterialTheme.typography.titleSmall)
//                    Text(gastos, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
//                }
//            }
//        }
//    }
//}
//
///** Tarjeta de totales del mes (ingresos, gastos, saldo) */
//@Composable
//private fun TotalesMesCard(
//    ingresos: String,
//    gastos: String,
//    saldo: String
//) {
//    Card {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column {
//                Text("Ingresos", style = MaterialTheme.typography.labelMedium)
//                Text(ingresos, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
//            }
//            Column {
//                Text("Gastos", style = MaterialTheme.typography.labelMedium)
//                Text(gastos, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
//            }
//            Column(horizontalAlignment = Alignment.End) {
//                Text("Saldo", style = MaterialTheme.typography.labelMedium)
//                Text(saldo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
//            }
//        }
//    }
//}
//
///** Fila ligera para cada movimiento en la sección por mes */
//@Composable
//private fun MovimientoLineaLigera(
//    titulo: String,
//    fecha: String,
//    importeFormateado: String,
//    esIngreso: Boolean
//) {
//    ListItem(
//        headlineContent = { Text(titulo) },
//        supportingContent = { Text(fecha) },
//        trailingContent = {
//            Text(
//                text = importeFormateado,
//                color = if (esIngreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//    )
//    Divider()
//}
