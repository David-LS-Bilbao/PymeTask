
// NUEVA VERSION (con Periodo y FiltroTipo)
@file:Suppress("DEPRECATION")

package com.dls.pymetask.presentation.estadisticas

import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import java.util.Date


// ============== Tipos existentes + nuevos ==============
private enum class Modo { MES, COMPARAR } // (se mantiene)

private enum class Periodo { HOY, SEMANA, MES, PERSONALIZADO } // nuevo
private enum class FiltroTipo { TODO, INGRESO, GASTO }          // nuevo

private data class Totales(val ingresos: Double, val gastosAbs: Double, val saldo: Double)

private data class SerieDiaria(
    val dias: Int,
    val ingresosPorDia: List<Double>,
    val gastosPorDia: List<Double>,
    val saldoAcumulado: List<Double>
)

// ============== Helpers de lógica (se mantienen / amplían) ==============
private fun Long.inMonth(year: Int, month0: Int): Boolean {
    val c = Calendar.getInstance().apply { timeInMillis = this@inMonth }
    return c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month0
}

private fun Long.isInDayRange(startMillis: Long, endMillis: Long): Boolean {
    val t = this
    return t >= startMillis && t <= endMillis
}

private fun Calendar.startOfDay(): Long {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return timeInMillis
}

private fun Calendar.endOfDay(): Long {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
    return timeInMillis
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

// ============== NUEVOS filtros de periodo y tipo ==============
private fun List<Movimiento>.filtrarPorPeriodo(
    periodo: Periodo,
    custom: Pair<Long, Long>?,
    year: Int,
    month0: Int
): List<Movimiento> {
    val cal = Calendar.getInstance()
    return when (periodo) {
        Periodo.MES -> filter { it.fecha.inMonth(year, month0) }
        Periodo.HOY -> {
            val hoy = Calendar.getInstance()
            val start = hoy.clone() as Calendar
            val end = hoy.clone() as Calendar
            filter { it.fecha.isInDayRange(start.startOfDay(), end.endOfDay()) }
        }
        Periodo.SEMANA -> {
            val now = Calendar.getInstance()
            val start = now.clone() as Calendar
            start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)
            val end = now.clone() as Calendar
            end.timeInMillis = now.timeInMillis
            filter { it.fecha.isInDayRange(start.startOfDay(), end.endOfDay()) }
        }
        Periodo.PERSONALIZADO -> {
            val (s, e) = custom ?: return emptyList()
            filter { it.fecha.isInDayRange(s, e) }
        }
    }
}

private fun List<Movimiento>.filtrarPorTipo(filtro: FiltroTipo): List<Movimiento> = when (filtro) {
    FiltroTipo.TODO -> this
    FiltroTipo.INGRESO -> filter { it.ingreso }
    FiltroTipo.GASTO -> filter { !it.ingreso }
}

// ===============================
//  PANTALLA (se mantiene nombre/firma y toggle comparar)
// ===============================
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController,
    viewModel: MovimientosViewModel = hiltViewModel()
) {
    // -> Se mantiene tu lectura de movimientos del ViewModel
    val movimientos by viewModel.movimientos.collectAsState(initial = emptyList()) // :contentReference[oaicite:3]{index=3}

    val currency = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val sdfFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")) }

    var modo by remember { mutableStateOf(Modo.MES) } // se mantiene :contentReference[oaicite:4]{index=4}

    // ---- Estados nuevos de filtros ----
    var periodo by remember { mutableStateOf(Periodo.MES) }
    var filtroTipo by remember { mutableStateOf(FiltroTipo.TODO) }
    var customRange by remember { mutableStateOf<Pair<Long, Long>?>(null) }
    var showRangePicker by remember { mutableStateOf(false) }

    // ---- Estados de navegación mensual (se mantienen) ----
    val hoy = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(hoy.get(Calendar.YEAR)) }
    var month0 by remember { mutableIntStateOf(hoy.get(Calendar.MONTH)) }

    var yearB by remember { mutableIntStateOf(year) }
    var month0B by remember { mutableIntStateOf((month0 - 1).coerceAtLeast(0)) }

    // ---- Datos A y B (manteniendo tu lógica) ----
    val listaA = remember(movimientos, periodo, customRange, year, month0) {
        movimientos.filtrarPorPeriodo(periodo, customRange, year, month0)
    }
    val listaB = remember(movimientos, yearB, month0B, modo) {
        if (modo == Modo.COMPARAR) movimientos.filter { it.fecha.inMonth(yearB, month0B) } else emptyList()
    }

    val listaFiltradaA = remember(listaA, filtroTipo) { listaA.filtrarPorTipo(filtroTipo) }

    val totA = remember(listaFiltradaA) { totalesMes(listaFiltradaA) }
    val totB = remember(listaB) { totalesMes(listaB) }

    val diarioA = remember(periodo, listaA, year, month0) {
        // Gráficos diarios solo tienen sentido en MES
        if (periodo == Periodo.MES) seriesDiarias(listaA, year, month0) else SerieDiaria(0, emptyList(), emptyList(), emptyList())
    }

    val tituloMesA = remember(year, month0) { monthYearTitle(year, month0) }
    val tituloMesB = remember(yearB, month0B) { monthYearTitle(yearB, month0B) }


    // Dentro de EstadisticasScreen, cerca de otros remember/derivados:
    val tendencia12m = remember(movimientos, year, month0) {
        aggregateLastMonths(movimientos, endYear = year, endMonth0 = month0, count = 12)
    }


    Scaffold(
        containerColor = colorScheme.background,
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
            // ---------------- Selector de periodo + mes ----------------
            if (modo == Modo.MES) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),

                    ) {
//                        FilterChip(selected = periodo == Periodo.HOY, onClick = { periodo = Periodo.HOY }, label = { Text("Hoy") })
//                        FilterChip(selected = periodo == Periodo.SEMANA, onClick = { periodo = Periodo.SEMANA }, label = { Text("Esta semana") })
//                        FilterChip(selected = periodo == Periodo.MES, onClick = { periodo = Periodo.MES }, label = { Text("Este mes") })
//                        FilterChip(
//                            selected = periodo == Periodo.MES,
//                            onClick = { periodo = Periodo.MES },
//                            label = { Text("Este mes") },
//                            colors = FilterChipDefaults.filterChipColors(
//                                selectedContainerColor = colorScheme.primary.copy(alpha = 0.15f),
//                                selectedLabelColor = colorScheme.primary
//                            )
//                        )

                        FilterChip(
                            selected = periodo == Periodo.HOY,
                            onClick = { periodo = Periodo.HOY },
                            label = { Text("Hoy") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = colorScheme.primary
                            )
                        )
                        FilterChip(
                            selected = periodo == Periodo.SEMANA,
                            onClick = { periodo = Periodo.SEMANA },
                            label = { Text("Esta semana") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = colorScheme.primary
                            )
                        )
                        FilterChip(
                            selected = periodo == Periodo.MES,
                            onClick = { periodo = Periodo.MES },
                            label = { Text("Este mes") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = colorScheme.primary
                            )
                        )
                        FilterChip(
                            selected = periodo == Periodo.PERSONALIZADO,
                            onClick = { showRangePicker = true; periodo = Periodo.PERSONALIZADO },
                            label = { Text("Personalizado") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = colorScheme.primary
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    if (periodo == Periodo.MES) {
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
                    }
                }
            } else {
                // Comparativa: mantenemos tus dos selectores A/B
                item {
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

            // ---------------- Resumen superior ----------------
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

            // ---------------- Filtro por tipo (solo Modo MES) ----------------
            if (modo == Modo.MES) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = filtroTipo == FiltroTipo.TODO, onClick = { filtroTipo = FiltroTipo.TODO }, label = { Text("Todo") })
                        FilterChip(selected = filtroTipo == FiltroTipo.INGRESO, onClick = { filtroTipo = FiltroTipo.INGRESO }, label = { Text("Ingresos") })
                        FilterChip(selected = filtroTipo == FiltroTipo.GASTO, onClick = { filtroTipo = FiltroTipo.GASTO }, label = { Text("Gastos") })
                    }
                }
            }

            // ---------------- Gráficos ----------------
            if (modo == Modo.MES && periodo == Periodo.MES) {
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

                item {
                    Tarjeta(title = "Tendencia últimos 12 meses") {
                        if (tendencia12m.isEmpty()) {
                            TextoVacio("Sin datos")
                        } else {
                            MonthlyTrendChart(data = tendencia12m, height = 200.dp)
                        }
                    }
                }





            } else if (modo == Modo.COMPARAR) {
                item {
                    Tarjeta(title = "Comparativa de totales") {
                        if (listaA.isEmpty() && listaB.isEmpty()) {
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

            // ---------------- Lista de movimientos (según periodo) ----------------
            if (modo == Modo.MES && listaFiltradaA.isNotEmpty()) {
                val tituloLista = when (periodo) {
                    Periodo.MES -> "Movimientos de $tituloMesA"
                    Periodo.HOY -> "Movimientos de hoy"
                    Periodo.SEMANA -> "Movimientos de esta semana"
                    Periodo.PERSONALIZADO -> "Movimientos (rango personalizado)"
                }
                item { Text(tituloLista, style = MaterialTheme.typography.titleMedium) }
                items(
                    items = listaFiltradaA.sortedByDescending { it.fecha },
                    key = { m: Movimiento -> m.id }
                ) { mov: Movimiento ->
                    ListItem(
                        headlineContent = { Text(mov.titulo) },
                        supportingContent = {
                            Text(sdfFecha.format(Date(mov.fecha)))
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

    // ---------- DateRangePicker para "Personalizado" ----------
    if (showRangePicker) {
        val now = Calendar.getInstance()
        val startInit = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.startOfDay()
        val endInit = (now.clone() as Calendar).endOfDay()

        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startInit,
            initialSelectedEndDateMillis = endInit
        )

        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedStartDateMillis != null && pickerState.selectedEndDateMillis != null,
                    onClick = {
                        customRange = pickerState.selectedStartDateMillis!! to pickerState.selectedEndDateMillis!!
                        showRangePicker = false
                    }
                ) { Text("Aplicar") }
            },
            dismissButton = { TextButton(onClick = { showRangePicker = false }) { Text("Cancelar") } }
        ) {
            DateRangePicker(state = pickerState, title = { Text("Rango personalizado") })
        }
    }
}

// ===============================
//  Composables auxiliares (sin cambios salvo pequeñas mejoras)
// ===============================
@Composable
private fun Tarjeta(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White, // <- fuerza blanco
            contentColor = colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
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
//  Gráficos Canvas (se mantienen tal cual)
// ===============================
@Composable
private fun BarChartIngresosGastosPorDia(
    ingresosPorDia: List<Double>,
    gastosPorDia: List<Double>,
    height: Dp
) {
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

            drawRect(
                color = colorA,
                topLeft = Offset(xStart, baseY - hIng),
                size = Size(barW, hIng)
            )
            drawRect(
                color = colorB,
                topLeft = Offset(xStart + barW + gap, baseY - hGas),
                size = Size(barW, hGas)
            )
        }
    }
}

@Composable
private fun LineChartSaldoAcumulado(
    saldoAcumulado: List<Double>,
    height: Dp
) {
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
    val colorA = colorScheme.primary
    val colorB = colorScheme.error
    val gridColor = colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    val labels = listOf("Ingresos", "Gastos", "Saldo")
    val valsA = listOf(ingresosA, gastosA, saldoA)
    val valsB = listOf(ingresosB, gastosB, saldoB)

    val maxV = (valsA + valsB).maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }

    // --- Capturamos recursos de tema/densidad en contexto composable ---
    val onSurface = colorScheme.onSurface
    val density = LocalDensity.current
    val textSizePx = with(density) { 11.sp.toPx() }

    // Paint memorizado (sin invocar composables dentro del remember)
    val valuePaint = remember(onSurface, textSizePx) {
        Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            color = onSurface.toArgb()
            textSize = textSizePx
        }
    }

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val w = size.width
            val h = size.height

            // Márgenes y área útil
            val left = 12f
            val right = 12f
            val top = 12f
            val bottom = 32f
            val contentW = w - left - right
            val contentH = h - top - bottom
            val baseY = top + contentH

            // Guías horizontales (25/50/75%)
            val steps = 3
            for (i in 1..steps) {
                val y = baseY - (contentH * i / (steps + 1))
                drawLine(
                    color = gridColor,
                    start = Offset(left, y),
                    end = Offset(left + contentW, y),
                    strokeWidth = 1f
                )
            }

            // Geometría de grupos y barras
            val groups = 3
            val cellW = contentW / groups
            val groupW = cellW * 0.7f
            val gap = 8f
            val barW = (groupW - gap) / 2f
            val usableH = contentH * 0.92f

            for (i in 0 until groups) {
                val xStart = left + i * cellW + (cellW - groupW) / 2f

                val hA = ((valsA[i] / maxV).toFloat() * usableH)
                val hB = ((valsB[i] / maxV).toFloat() * usableH)

                // Barra A
                val xA = xStart
                drawRect(
                    color = colorA,
                    topLeft = Offset(xA, baseY - hA),
                    size = Size(barW, hA)
                )
                // Valor A
                drawIntoCanvas {
                    val yText = (baseY - hA - 6f).coerceAtLeast(top + 12f)
                    it.nativeCanvas.drawText(
                        currency.format(valsA[i]),
                        xA + barW / 2f,
                        yText,
                        valuePaint
                    )
                }

                // Barra B
                val xB = xStart + barW + gap
                drawRect(
                    color = colorB,
                    topLeft = Offset(xB, baseY - hB),
                    size = Size(barW, hB)
                )
                // Valor B
                drawIntoCanvas {
                    val yText = (baseY - hB - 6f).coerceAtLeast(top + 12f)
                    it.nativeCanvas.drawText(
                        currency.format(valsB[i]),
                        xB + barW / 2f,
                        yText,
                        valuePaint
                    )
                }
            }
        }

        // Etiquetas del eje X
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        // Leyenda Mes A / Mes B (usa tu LegendChip existente)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendChip(color = colorScheme.primary, label = "A: $tituloA")
            LegendChip(color = colorScheme.error, label = "B: $tituloB")
        }
    }
}




@Composable
private fun LegendChip(color: Color, label: String) {
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

// --- Agregado: agregación por mes (últimos N meses) ---
private data class MonthAgg(
    val year: Int,
    val month0: Int,            // 0..11
    val ingresos: Double,
    val gastos: Double
) {
    val saldo: Double get() = ingresos - gastos
}

private fun aggregateLastMonths(
    movimientos: List<Movimiento>,
    endYear: Int,
    endMonth0: Int,
    count: Int = 12
): List<MonthAgg> {
    // Construye ventana de meses [end - (count-1) ... end]
    val cal = Calendar.getInstance()
    val keys = ArrayList<Pair<Int, Int>>(count)
    cal.set(endYear, endMonth0, 1)
    repeat(count) {
        keys.add(cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH))
        cal.add(Calendar.MONTH, -1)
    }
    keys.reverse() // de antiguo -> reciente

    // Inicializa acumuladores
    val acc = keys.associateWith { 0.0 to 0.0 }.toMutableMap() // Pair(ingresos, gastos)

    val tmp = Calendar.getInstance()
    movimientos.forEach { m ->
        tmp.timeInMillis = m.fecha
        val k = tmp.get(Calendar.YEAR) to tmp.get(Calendar.MONTH)
        if (k in acc.keys) {
            val (ing, gas) = acc[k]!!
            if (m.ingreso) acc[k] = (ing + m.cantidad) to gas
            else acc[k] = ing to (gas + m.cantidad)
        }
    }

    return keys.map { (y, m) ->
        val (ing, gas) = acc[y to m] ?: 0.0 to 0.0
        MonthAgg(y, m, ingresos = ing, gastos = gas)
    }
}

private fun monthShortEs(year: Int, month0: Int): String {
    val c = Calendar.getInstance().apply { set(year, month0, 1) }
    val s = java.text.SimpleDateFormat("LLL", java.util.Locale("es", "ES")).format(c.time)
    return s.replace(".", "").uppercase() // "ene." -> "ENE"
}



// --- Agregado: gráfico de tendencia 12 meses ---
@Composable
private fun MonthlyTrendChart(
    data: List<MonthAgg>,
    height: Dp
) {
    val colorIngresos = MaterialTheme.colorScheme.primary
    val colorGastos = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    val maxV = (data.flatMap { listOf(it.ingresos, it.gastos) }.maxOrNull() ?: 0.0).coerceAtLeast(1e-6)
    val labels = data.map { monthShortEs(it.year, it.month0) }

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val w = size.width
            val h = size.height

            val left = 12f
            val right = 12f
            val top = 12f
            val bottom = 28f
            val contentW = w - left - right
            val contentH = h - top - bottom
            val baseY = top + contentH
            val usableH = contentH * 0.92f

            // Guías 25/50/75%
            val steps = 3
            for (i in 1..steps) {
                val y = baseY - (contentH * i / (steps + 1))
                drawLine(
                    color = gridColor,
                    start = Offset(left, y),
                    end = Offset(left + contentW, y),
                    strokeWidth = 1f
                )
            }

            val n = data.size
            if (n == 0) return@Canvas
            val cellW = contentW / n
            val groupW = cellW * 0.68f
            val gap = 6f
            val barW = (groupW - gap) / 2f

            data.forEachIndexed { i, m ->
                val xStart = left + i * cellW + (cellW - groupW) / 2f

                val hIng = ((m.ingresos / maxV).toFloat() * usableH)
                val hGas = ((m.gastos / maxV).toFloat() * usableH)

                // Ingresos
                drawRect(
                    color = colorIngresos,
                    topLeft = Offset(xStart, baseY - hIng),
                    size = androidx.compose.ui.geometry.Size(barW, hIng)
                )
                // Gastos
                drawRect(
                    color = colorGastos,
                    topLeft = Offset(xStart + barW + gap, baseY - hGas),
                    size = androidx.compose.ui.geometry.Size(barW, hGas)
                )
            }
        }

        // Etiquetas de mes (cada 2 para no saturar, si quieres)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { index, s ->
                Text(
                    if (labels.size > 8 && index % 2 == 1) " " else s,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Leyenda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendChip(color = MaterialTheme.colorScheme.primary, label = "Ingresos")
            LegendChip(color = MaterialTheme.colorScheme.error, label = "Gastos")
        }
    }
}


