

@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("DEPRECATION")

package com.dls.pymetask.presentation.estadisticas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.presentation.movimientos.MovimientosViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant.ofEpochMilli
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EstadisticasScreen(
    navController: NavController,
    movimientosVm: MovimientosViewModel = hiltViewModel(),
    statsVm: EstadisticasViewModel = hiltViewModel()
) {
    // Fuente de movimientos desde tu VM existente
    val movimientos by movimientosVm.movimientos.collectAsState(initial = emptyList())
    LaunchedEffect(movimientos) { statsVm.setMovimientos(movimientos) }

    val currency = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = java.util.Currency.getInstance("EUR")   // 👈 €
            maximumFractionDigits = 2
        }
    }


    // Estado de estadísticas
    val ui by statsVm.ui.collectAsState()

    // UI local
    var showRangePicker by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Formatos (se deja como estaba para no tocar lógica ajena al idioma)
  //  val currency = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val sdfFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")) }

    val listaParaLista = remember(ui.listaA, ui.filtro, selectedCategory) {
        val base = ui.listaA.filtrarPorTipo(ui.filtro)
        selectedCategory?.let { label ->
            base.filter { !it.ingreso && movimientoCategoryLabel(it) == label }
        } ?: base
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.stats_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { statsVm.toggleModo() }) {
                        Icon(
                            imageVector = if (ui.modo == Modo.MES)
                                Icons.AutoMirrored.Filled.CompareArrows
                            else
                                Icons.Filled.DateRange,
                            contentDescription = stringResource(R.string.stats_toggle_mode)
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

            // ---------- Chips periodo + selector mes (modo Mes) ----------
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    PeriodChip(stringResource(R.string.stats_period_today),   ui.periodo == Periodo.HOY)       { statsVm.setPeriodo(Periodo.HOY) }
                    PeriodChip(stringResource(R.string.stats_period_week),    ui.periodo == Periodo.SEMANA)    { statsVm.setPeriodo(Periodo.SEMANA) }
                    PeriodChip(stringResource(R.string.stats_period_month),   ui.periodo == Periodo.MES)       { statsVm.setPeriodo(Periodo.MES) }
                    PeriodChip(stringResource(R.string.stats_period_custom),  ui.periodo == Periodo.PERSONALIZADO) {
                        statsVm.setPeriodo(Periodo.PERSONALIZADO)
                        showRangePicker = true
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (ui.periodo == Periodo.MES && ui.modo == Modo.MES) {
                    SelectorMes(
                        title = stringResource(R.string.stats_month),
                        year = ui.year,
                        monthZero = ui.month0,
                        onPrev = { statsVm.mesPrevA() },
                        onNext = { statsVm.mesNextA() }
                    )
                }
            }

            // ---------- Resumen ----------
            item {
                if (ui.movimientos.isEmpty()) {
                    Tarjeta(stringResource(R.string.stats_summary)) {
                        TextoVacio(stringResource(R.string.stats_empty_movements))
                    }
                } else if (ui.modo == Modo.MES) {
                    ResumenGlobal(
                        saldo = currency.format(ui.totA.saldo),
                        ingresos = currency.format(ui.totA.ingresos),
                        gastos = currency.format(ui.totA.gastosAbs)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectorMes(
                            title = stringResource(R.string.stats_month_a),
                            year = ui.year,
                            monthZero = ui.month0,
                            onPrev = { statsVm.mesPrevA() },
                            onNext = { statsVm.mesNextA() }
                        )
                        SelectorMes(
                            title = stringResource(R.string.stats_month_b),
                            year = ui.yearB,
                            monthZero = ui.month0B,
                            onPrev = { statsVm.mesPrevB() },
                            onNext = { statsVm.mesNextB() }
                        )
                        ResumenComparativa(
                            tituloA = ui.tituloMesA, tituloB = ui.tituloMesB,
                            ingresosA = currency.format(ui.totA.ingresos),
                            gastosA = currency.format(ui.totA.gastosAbs),
                            saldoA = currency.format(ui.totA.saldo),
                            ingresosB = currency.format(ui.totB.ingresos),
                            gastosB = currency.format(ui.totB.gastosAbs),
                            saldoB = currency.format(ui.totB.saldo)
                        )
                    }
                }
            }

            // ---------- Gastos por categorías (modo Mes) ----------
            if (ui.modo == Modo.MES) {
                item {
                    val allSlices = remember(ui.listaA) { buildExpenseSlicesAll(ui.listaA) }
                    CategoryBreakdownCard(
                        title = stringResource(R.string.stats_expenses_by_category, ui.tituloMesA),
                        allSlices = allSlices,
                        selectedLabel = selectedCategory,
                        onSelect = { label -> selectedCategory = if (selectedCategory == label) null else label },
                        maxVisible = 8
                    )
                }
            }

            // ---------- Gráficos ----------
            if (ui.modo == Modo.MES && ui.periodo == Periodo.MES) {
                item {
                    Tarjeta(stringResource(R.string.stats_chart_income_vs_expenses_day, ui.tituloMesA)) {
                        if (ui.diarioA.dias == 0) TextoVacio(stringResource(R.string.stats_chart_no_data_month))
                        else BarChartIngresosGastosPorDia(
                            ingresosPorDia = ui.diarioA.ingresosPorDia,
                            gastosPorDia = ui.diarioA.gastosPorDia,
                            height = 180.dp
                        )
                    }
                    if (selectedCategory != null) {
                        Spacer(Modifier.height(4.dp))
                        AssistChip(
                            onClick = { selectedCategory = null },
                            label = { Text("Filtro: $selectedCategory  ✕") } // (opcional) si lo quieres también en recursos, avísame
                        )
                    }
                }
                item {
                    Tarjeta(stringResource(R.string.stats_chart_accumulated_balance, ui.tituloMesA)) {
                        if (ui.diarioA.dias == 0) TextoVacio(stringResource(R.string.stats_chart_no_data_month))
                        else LineChartSaldoAcumulado(ui.diarioA.saldoAcumulado, 180.dp)
                    }
                }
            } else if (ui.modo == Modo.COMPARAR) {
                item {
                    Tarjeta(stringResource(R.string.stats_compare_totals)) {
                        if (ui.listaA.isEmpty() && ui.listaB.isEmpty()) {
                            TextoVacio(stringResource(R.string.stats_compare_no_data))
                        } else {
                            BarChartComparativaTotales(
                                tituloA = ui.tituloMesA, tituloB = ui.tituloMesB,
                                ingresosA = ui.totA.ingresos, gastosA = ui.totA.gastosAbs, saldoA = ui.totA.saldo,
                                ingresosB = ui.totB.ingresos, gastosB = ui.totB.gastosAbs, saldoB = ui.totB.saldo,
                                height = 220.dp
                            )
                        }
                    }
                }
            }

            // Tendencia 12 meses
            if (ui.modo == Modo.MES) {
                item {
                    Tarjeta(stringResource(R.string.stats_trend_12m)) {
                        if (ui.tendencia12m.isEmpty()) TextoVacio(stringResource(R.string.stats_no_data))
                        else MonthlyTrendChart(ui.tendencia12m, 200.dp)
                    }
                }
            }
            // ---------- Lista ----------
            if (ui.modo == Modo.MES && listaParaLista.isNotEmpty()) {
                item {
                    val tituloLista = when (ui.periodo) {
                        Periodo.MES           -> stringResource(R.string.stats_list_month, ui.tituloMesA)
                        Periodo.HOY           -> stringResource(R.string.stats_list_today)
                        Periodo.SEMANA        -> stringResource(R.string.stats_list_week)
                        Periodo.PERSONALIZADO -> stringResource(R.string.stats_list_custom)
                    }
                    Text(tituloLista, style = MaterialTheme.typography.titleMedium)
                }
                items(listaParaLista.sortedByDescending { it.fecha }, key = { it.id }) { mov ->
                    MovimientoRow(mov, sdfFecha, currency)
                    HorizontalDivider()
                }
            }
        }
    }

    // ---------- DateRangePicker ----------
    if (showRangePicker) {
        DateRangePickerDialog(
            onDismiss = { showRangePicker = false },
            onConfirm = { start, end ->
                statsVm.setCustomRange(start.toEpochMillisAtStart() to end.toEpochMillisAtEnd())
                showRangePicker = false
            }
        )
    }
}

/* ======================== UI Helpers ======================== */

@Composable
private fun PeriodChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
            selectedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun Tarjeta(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.large
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
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onPrev) { Text("←") }
            Text(
                monthYearTitle(year, monthZero),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onNext) { Text("→") }
        }
    }
}

@Composable
private fun ResumenGlobal(saldo: String, ingresos: String, gastos: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.stats_balance))
                Text(saldo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = MaterialTheme.shapes.large
            ) { Column(Modifier.padding(16.dp)) { Text(stringResource(R.string.stats_income));  Text(ingresos, fontWeight = FontWeight.SemiBold) } }
            Card(
                Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = MaterialTheme.shapes.large
            ) { Column(Modifier.padding(16.dp)) { Text(stringResource(R.string.stats_expenses)); Text(gastos,  fontWeight = FontWeight.SemiBold) } }
        }
    }
}

@Composable
private fun ResumenComparativa(
    tituloA: String, tituloB: String,
    ingresosA: String, gastosA: String, saldoA: String,
    ingresosB: String, gastosB: String, saldoB: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Card(
            Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(tituloA, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text("${stringResource(R.string.stats_balance)}: $saldoA")
                Text("${stringResource(R.string.stats_income)}: $ingresosA")
                Text("${stringResource(R.string.stats_expenses)}: $gastosA")
            }
        }
        Card(
            Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(tituloB, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text("${stringResource(R.string.stats_balance)}: $saldoB")
                Text("${stringResource(R.string.stats_income)}: $ingresosB")
                Text("${stringResource(R.string.stats_expenses)}: $gastosB")
            }
        }
    }
}

@Composable private fun TextoVacio(text: String) {
    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MovimientoRow(
    mov: Movimiento,
    sdfFecha: SimpleDateFormat,
    currency: NumberFormat
) {
    val color = if (mov.ingreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    ListItem(
        headlineContent = { Text(mov.titulo) },
        supportingContent = { Text(sdfFecha.format(Date(mov.fecha))) },
        trailingContent = {
            val signed = if (mov.ingreso) mov.cantidad else -mov.cantidad
            Text(currency.format(signed), color = color, fontWeight = FontWeight.SemiBold)
        }
    )
}

/* ======================== Charts ======================== */

@Composable
private fun BarChartIngresosGastosPorDia(
    ingresosPorDia: List<Double>,
    gastosPorDia: List<Double>,
    height: Dp
) {
    val colorA = MaterialTheme.colorScheme.primary
    val colorB = MaterialTheme.colorScheme.error
    val dias = ingresosPorDia.size
    if (dias == 0) return
    val maxValor = maxOf(
        ingresosPorDia.maxOrNull() ?: 0.0,
        gastosPorDia.maxOrNull() ?: 0.0,
        1e-9
    )

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

            drawRect(colorA, topLeft = Offset(xStart, baseY - hIng), size = androidx.compose.ui.geometry.Size(barW, hIng))
            drawRect(colorB, topLeft = Offset(xStart + barW + gap, baseY - hGas), size = androidx.compose.ui.geometry.Size(barW, hGas))
        }
    }
}

@Composable
private fun LineChartSaldoAcumulado(saldoAcumulado: List<Double>, height: Dp) {
    val colorLine = MaterialTheme.colorScheme.error
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
            drawLine(colorLine, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = 4f)
        }
    }
}

@Composable
private fun BarChartComparativaTotales(
    tituloA: String, tituloB: String,
    ingresosA: Double, gastosA: Double, saldoA: Double,
    ingresosB: Double, gastosB: Double, saldoB: Double,
    height: Dp
) {
    val colorA = MaterialTheme.colorScheme.primary
    val colorB = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    val labels = listOf(
        stringResource(R.string.stats_income),
        stringResource(R.string.stats_expenses),
        stringResource(R.string.stats_balance)
    )
    val valsA = listOf(ingresosA, gastosA, saldoA)
    val valsB = listOf(ingresosB, gastosB, saldoB)

    val maxV = (valsA + valsB).maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val currency = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = java.util.Currency.getInstance("EUR")   // 👈 €
            maximumFractionDigits = 2
        }
    }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val textSizePx = with(LocalDensity.current) { 11.sp.toPx() }
    val valuePaint = remember(onSurface, textSizePx) {
        android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            color = onSurface.toArgb()
            textSize = textSizePx
        }
    }

    Column {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val w = size.width; val h = size.height
            val left = 12f; val right = 12f; val top = 12f; val bottom = 32f
            val contentW = w - left - right
            val contentH = h - top - bottom
            val baseY = top + contentH
            val usableH = contentH * 0.92f

            // Guías
            for (i in 1..3) {
                val y = baseY - (contentH * i / 4f)
                drawLine(gridColor, Offset(left, y), Offset(left + contentW, y), 1f)
            }

            val groups = 3
            val cellW = contentW / groups
            val groupW = cellW * 0.7f
            val gap = 8f
            val barW = (groupW - gap) / 2f

            for (i in 0 until groups) {
                val xStart = left + i * cellW + (cellW - groupW) / 2f
                val hA = ((valsA[i] / maxV).toFloat() * usableH)
                val hB = ((valsB[i] / maxV).toFloat() * usableH)

                val xA = xStart
                drawRect(colorA, topLeft = Offset(xA, baseY - hA), size = androidx.compose.ui.geometry.Size(barW, hA))
                drawIntoCanvas {
                    val yText = (baseY - hA - 6f).coerceAtLeast(top + 12f)
                    it.nativeCanvas.drawText(currency.format(valsA[i]), xA + barW / 2f, yText, valuePaint)
                }

                val xB = xStart + barW + gap
                drawRect(colorB, topLeft = Offset(xB, baseY - hB), size = androidx.compose.ui.geometry.Size(barW, hB))
                drawIntoCanvas {
                    val yText = (baseY - hB - 6f).coerceAtLeast(top + 12f)
                    it.nativeCanvas.drawText(currency.format(valsB[i]), xB + barW / 2f, yText, valuePaint)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendChip(MaterialTheme.colorScheme.primary, stringResource(R.string.stats_legend_a, tituloA))
            LegendChip(MaterialTheme.colorScheme.error,   stringResource(R.string.stats_legend_b, tituloB))
        }
    }
}

@Composable
private fun LegendChip(color: Color, label: String) {
    Surface(tonalElevation = 1.dp, shape = RoundedCornerShape(50)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(color, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MonthlyTrendChart(data: List<MonthAgg>, height: Dp) {
    val colorIngresos = MaterialTheme.colorScheme.primary
    val colorGastos = MaterialTheme.colorScheme.error
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    val maxV = (data.flatMap { listOf(it.ingresos, it.gastos) }.maxOrNull() ?: 0.0).coerceAtLeast(1e-6)
    val labels = data.map { monthShortEs(it.year, it.month0) }

    Column {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val w = size.width; val h = size.height
            val left = 12f; val right = 12f; val top = 12f; val bottom = 28f
            val contentW = w - left - right
            val contentH = h - top - bottom
            val baseY = top + contentH
            val usableH = contentH * 0.92f

            for (i in 1..3) {
                val y = baseY - (contentH * i / 4f)
                drawLine(gridColor, Offset(left, y), Offset(left + contentW, y), 1f)
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

                drawRect(colorIngresos, topLeft = Offset(xStart, baseY - hIng), size = androidx.compose.ui.geometry.Size(barW, hIng))
                drawRect(colorGastos,   topLeft = Offset(xStart + barW + gap, baseY - hGas), size = androidx.compose.ui.geometry.Size(barW, hGas))
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
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
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendChip(MaterialTheme.colorScheme.primary, stringResource(R.string.stats_income))
            LegendChip(MaterialTheme.colorScheme.error,   stringResource(R.string.stats_expenses))
        }
    }
}

/* ======================== DateRangePicker ======================== */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (start: LocalDate, end: LocalDate) -> Unit
) {
    val now = LocalDate.now()
    val startInit = now.withDayOfMonth(1)
    val endInit = now

    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startInit.toEpochMillisAtStart(),
        initialSelectedEndDateMillis = endInit.toEpochMillisAtStart()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                onClick = {
                    val startMillis = state.selectedStartDateMillis
                    val endMillis = state.selectedEndDateMillis

                    if (startMillis != null && endMillis != null) {
                        val start = startMillis.toLocalDate()
                        val end = endMillis.toLocalDate()
                        onConfirm(start, end)
                    }
                }
            ) { Text(stringResource(R.string.common_apply)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    ) {
        DateRangePicker(
            state = state,
            title = { Text(stringResource(R.string.stats_daterange_title)) },
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onPrimary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/* ======================== Utils fecha UI ======================== */
@RequiresApi(Build.VERSION_CODES.O)
private fun Long.toLocalDate(): LocalDate =
    ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalDate.toEpochMillisAtStart(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalDate.toEpochMillisAtEnd(): Long =
    plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

/* ======================== Modelo/agrupación categorías ======================== */

private data class CategorySlice(val label: String, val value: Double)

private fun buildExpenseSlicesAll(movimientos: List<Movimiento>): List<CategorySlice> {
    if (movimientos.isEmpty()) return emptyList()
    val byLabel = movimientos
        .asSequence()
        .filter { !it.ingreso }
        .groupBy { m ->
            val base = (m.subtitulo.ifBlank { m.titulo }).ifBlank { "Otros" }.trim()
            base.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        .mapValues { (_, list) -> list.sumOf { it.cantidad } }
        .filterValues { it > 0.0 }

    return byLabel.entries
        .sortedByDescending { it.value }
        .map { CategorySlice(it.key, it.value) }
}

private fun collapseSlices(all: List<CategorySlice>, maxVisible: Int): List<CategorySlice> {
    if (all.size <= maxVisible) return all
    val top = all.take(maxVisible - 1)
    val rest = all.drop(maxVisible - 1).sumOf { it.value }
    return top + CategorySlice("Otros", rest)
}

/* ======================== Donut & leyenda ======================== */


@Composable
private fun CategoryBreakdownCard(
    title: String,
    allSlices: List<CategorySlice>,
    selectedLabel: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxVisible: Int = 8,
    height: Dp = 180.dp,
    ringThickness: Dp = 22.dp
) {
    var expanded by remember { mutableStateOf(false) }
    val display = remember(allSlices, expanded, maxVisible) {
        if (expanded) allSlices else collapseSlices(allSlices, maxVisible)
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,

            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)}
            Row(
                Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,

            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        if (expanded)
                            stringResource(R.string.stats_see_top_n, maxVisible)
                        else
                            stringResource(R.string.stats_see_all),
                                    maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (display.isEmpty() || display.sumOf { it.value } <= 0.0) {
                TextoVacio(stringResource(R.string.stats_no_expenses_period))
                return@Column
            }

            BoxWithConstraints {
                val chartSize = if (maxWidth < 360.dp) 130.dp else height

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DonutChart(
                        slices = display.mapIndexed { i, s -> s.value to donutPalette[i % donutPalette.size] },
                        modifier = Modifier.size(chartSize),
                        thickness = ringThickness
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = height)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        display.forEachIndexed { i, s ->
                            val pct = if (display.sumOf { it.value } == 0.0) 0 else ((s.value / display.sumOf { it.value }) * 100).toInt()
                            val isSel = s.label == selectedLabel
                            val shownLabel = if (s.label == "Otros") stringResource(R.string.stats_others) else s.label
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                        else Color.Transparent
                                    )
                                    .clickable { onSelect(s.label) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .background(donutPalette[i % donutPalette.size], CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    shownLabel,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val currency = remember {
                                    NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                                        currency = java.util.Currency.getInstance("EUR")
                                        maximumFractionDigits = 2
                                    }
                                }
                                Text(
                                    "${currency.format(s.value)}  ·  $pct%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}




//@Composable
//private fun CategoryBreakdownCard(
//    title: String,
//    allSlices: List<CategorySlice>,
//    selectedLabel: String?,
//    onSelect: (String) -> Unit,
//    modifier: Modifier = Modifier,
//    maxVisible: Int = 8,
//    height: Dp = 180.dp,
//    ringThickness: Dp = 22.dp
//) {
//    var expanded by remember { mutableStateOf(false) }
//    val display = remember(allSlices, expanded, maxVisible) {
//        if (expanded) allSlices else collapseSlices(allSlices, maxVisible)
//    }
//
//    Card(
//        modifier = modifier,
//        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        elevation = CardDefaults.cardElevation(2.dp),
//        shape = MaterialTheme.shapes.large
//    ) {
//        Column(Modifier.fillMaxWidth().padding(16.dp)) {
//            Row(
//                Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(title, style = MaterialTheme.typography.titleMedium)
//                TextButton(onClick = { expanded = !expanded }) {
//                    Text(
//                        if (expanded)
//                            stringResource(R.string.stats_see_top_n, maxVisible)
//                        else
//                            stringResource(R.string.stats_see_all)
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(12.dp))
//
//            if (display.isEmpty() || display.sumOf { it.value } <= 0.0) {
//                TextoVacio(stringResource(R.string.stats_no_expenses_period))
//                return@Column
//            }
//
//            Row(
//                Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                DonutChart(
//                    slices = display.mapIndexed { i, s -> s.value to donutPalette[i % donutPalette.size] },
//                    modifier = Modifier.size(height),
//                    thickness = ringThickness
//                )
//                Spacer(Modifier.width(16.dp))
//                LegendList(
//                    slices = display,
//                    selectedLabel = selectedLabel,
//                    onSelect = onSelect,
//                    modifier = Modifier
//                        .weight(1f)
//                        .heightIn(max = height)
//                        .verticalScroll(rememberScrollState())
//                )
//            }
//        }
//    }
//}

@Composable
private fun LegendList(
    slices: List<CategorySlice>,
    selectedLabel: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value }
    val currency = remember {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = java.util.Currency.getInstance("EUR")   // 👈 €
            maximumFractionDigits = 2
        }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        slices.forEachIndexed { i, s ->
            val pct = if (total == 0.0) 0 else ((s.value / total) * 100).toInt()
            val isSel = s.label == selectedLabel
            val shownLabel = if (s.label == "Otros") stringResource(R.string.stats_others) else s.label

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        else Color.Transparent
                    )
                    .clickable { onSelect(s.label) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(donutPalette[i % donutPalette.size], CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    shownLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${currency.format(s.value)}  ·  $pct%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<Pair<Double, Color>>,
    modifier: Modifier = Modifier,
    thickness: Dp = 20.dp
) {
    val total = slices.sumOf { it.first }.takeIf { it > 0 } ?: return
    Canvas(modifier) {
        val diameter = size.minDimension
        val strokeWidth = thickness.toPx()
        val radius = diameter / 2f
        val rect = androidx.compose.ui.geometry.Rect(
            center = Offset(size.width / 2f, size.height / 2f),
            radius = radius - strokeWidth / 2f
        )
        var start = -90f
        slices.forEach { (value, color) ->
            val sweep = (value / total).toFloat() * 360f
            if (sweep > 0f) {
                drawArc(
                    color = color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
                start += sweep
            }
        }
    }
}

// Etiqueta de categoría por movimiento (se deja sin cambios funcionales)
private fun movimientoCategoryLabel(m: Movimiento): String {
    val base = (m.subtitulo.ifBlank { m.titulo }).ifBlank { "Otros" }.trim()
    return base.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

// Paleta usada en el donut
private val donutPalette = listOf(
    Color(0xFF3B82F6), Color(0xFF22C55E), Color(0xFFF59E0B), Color(0xFFEF4444),
    Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFFE11D48),
)



