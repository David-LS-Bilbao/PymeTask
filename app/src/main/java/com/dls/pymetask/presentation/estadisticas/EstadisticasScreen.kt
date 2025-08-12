package com.dls.pymetask.presentation.estadisticas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.NumberFormat
import java.time.format.TextStyle
import java.util.Locale

/**
 * Pantalla de Estadísticas.
 * - Muestra selector de modo (por mes / por categoría)
 * - Selector de año
 * - Gráfico de barras dibujado en Canvas (sin dependencias externas)
 * - Tarjetas con totales: ingresos, gastos y balance
 *
 * Todas las funciones y composables están comentados en español.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController,

) {
    // Estado expuesto por el ViewModel con los datos agregados para el gráfico y totales


    // Formateador de moneda en español
    val moneda = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // --- Filtros superiores: modo de agrupación y año ------------------------------------


            Spacer(Modifier.height(8.dp))

            // --- Tarjetas con totales (ingresos, gastos, balance) --------------------------------


            Spacer(Modifier.height(12.dp))

            // --- Gráfico -------------------------------------------------------------------------
//            when {
//                uiState.isLoading -> {
//                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        CircularProgressIndicator()
//                    }
//                }
//                uiState.barras.isEmpty() -> {
//                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text("No hay datos para mostrar en este período")
//                    }
//                }
//                else -> {
//                    // Scroll horizontal por si hay muchas barras (12 meses o muchas categorías)
//                    val scroll = rememberScrollState()
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .horizontalScroll(scroll)
//                            .padding(horizontal = 12.dp)
//                    ) {
//                        BarChart(
//                            data = uiState.barras,
//                            // Ancho dinámico: 80dp por par de barras (ingreso/gasto) + padding
//                            widthPerItem = 80.dp,
//                            chartHeight = 220.dp,
//                            mostrarEjes = true
//                        )
//                    }
//
//                    Spacer(Modifier.height(8.dp))
//
//                    // Leyenda simple: colores para Ingresos/Gastos
//                    LegendRow()
//                }
//            }
        }
    }
}

/**
 * FiltrosEstadisticas:
 * - Selector modo: PorMes / PorCategoria
 * - Selector de año (los años disponibles llegan del ViewModel)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosEstadisticas(
//    modo: EstadisticasModo,
//    onModoChange: (EstadisticasModo) -> Unit,
    year: Int,
    yearsDisponibles: List<Int>,
    onYearChange: (Int) -> Unit
) {
//    Column(Modifier.padding(horizontal = 12.dp)) {
//        // Selector de modo usando SegmentedButtons (Material3)
//        SingleChoiceSegmentedButtonRow {
//            SegmentButton(
//                selected = modo == EstadisticasModo.POR_MES,
//                text = "Por mes",
//                onClick = { onModoChange(EstadisticasModo.POR_MES) },
//                start = true
//            )
//            SegmentButton(
//                selected = modo == EstadisticasModo.POR_CATEGORIA,
//                text = "Por categoría",
//                onClick = { onModoChange(EstadisticasModo.POR_CATEGORIA) },
//                end = true
//            )
//        }
//
//        Spacer(Modifier.height(8.dp))
//
//        // Selector de año en un ExposedDropdown
//        var expanded by remember { mutableStateOf(false) }
//        ExposedDropdownMenuBox(
//            expanded = expanded,
//            onExpandedChange = { expanded = !expanded }
//        ) {
//            OutlinedTextField(
//                value = year.toString(),
//                onValueChange = {},
//                readOnly = true,
//                label = { Text("Año") },
//                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
//                modifier = Modifier
//                    .menuAnchor()
//                    .fillMaxWidth()
//            )
//            ExposedDropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false }
//            ) {
//                yearsDisponibles.forEach { y ->
//                    DropdownMenuItem(
//                        text = { Text(y.toString()) },
//                        onClick = {
//                            expanded = false
//                            onYearChange(y)
//                        }
//                    )
//                }
//            }
//        }
//    }
}

/** Botón de segmento reutilizable (segmented control) */
@Composable
private fun SegmentButton(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    start: Boolean = false,
    end: Boolean = false
) {
//    val shape = when {
//        start -> SegmentedButtonDefaults.itemShape(start = true, end = false)
//        end -> SegmentedButtonDefaults.itemShape(start = false, end = true)
//        else -> SegmentedButtonDefaults.itemShape()
//    }
//    SegmentedButton(
//        selected = selected,
//        onClick = onClick,
//        shape = shape,
//        label = { Text(text) }
//    )
}

/**
 * Muestra 3 tarjetas compactas con los totales:
 * - Total Ingresos
 * - Total Gastos
 * - Balance
 */
@Composable
private fun TotalesRow(
    totalIngresos: Double,
    totalGastos: Double,
    balance: Double,
    formatter: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TotalCard(
            title = "Ingresos",
            value = formatter.format(totalIngresos),
            bg = Color(0xFFE8F5E9) // verde suave
        )
        TotalCard(
            title = "Gastos",
            value = formatter.format(totalGastos),
            bg = Color(0xFFFFEBEE) // rojo suave
        )
        TotalCard(
            title = "Balance",
            value = formatter.format(balance),
            bg = Color(0xFFE3F2FD) // azul suave
        )
    }
}

/** Tarjeta simple para un total */
@Composable
private fun TotalCard(title: String, value: String, bg: Color) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        color = bg,
        modifier = Modifier
          //  .weight(1f)
            .height(72.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color.Black)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black
            )
        }
    }
}

/**
 * Leyenda del gráfico:
 * - Azul = Ingresos
 * - Rojo = Gastos
 */
@Composable
private fun LegendRow() {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(color = Color(0xFF1976D2)) // azul
        Text("Ingresos", modifier = Modifier.padding(end = 12.dp))
        LegendDot(color = Color(0xFFD32F2F)) // rojo
        Text("Gastos")
    }
}

/** Puntito de color usado en la leyenda */
@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(color, shape = MaterialTheme.shapes.extraSmall)
    )
}

/**
 * Componente del gráfico de barras dibujado con Canvas.
 * Cada elemento de data representa un "grupo" con 2 barras: ingresos y gastos.
 */
@Composable
private fun BarChart(
    data: List<BarGroup>,
    widthPerItem: Dp,
    chartHeight: Dp,
    mostrarEjes: Boolean
) {
    // Colores fijos para distinguir: ingresos (azul) y gastos (rojo)
    val ingresoColor = Color(0xFF1976D2)
    val gastoColor = Color(0xFFD32F2F)

    // Calculamos el valor máximo para escalar la altura de las barras
    val maxValor = (data.maxOfOrNull { maxOf(it.ingresos, it.gastos) } ?: 0.0).coerceAtLeast(1.0)

    // Medidas básicas del chart
    val axisPadding = 32.dp                 // espacio para el eje Y y etiquetas
    val itemSpacing = 16.dp                 // separación entre grupos
    val barWidth = 18.dp                    // ancho de cada barra (par por grupo)

    val totalWidth = axisPadding + // margen izquierdo para eje Y
            data.size * (widthPerItem) // cada grupo ocupa un ancho fijo

    Canvas(
        modifier = Modifier
            .height(chartHeight + 48.dp) // algo más para etiquetas X
            .width(totalWidth)
    ) {
        val h = size.height
        val w = size.width

        val left = axisPadding.toPx()
        val bottom = h - 28.dp.toPx() // deja espacio para etiquetas X
        val top = 12.dp.toPx()

        // Dibuja ejes
        if (mostrarEjes) {
            // Eje Y
            drawLine(
                color = Color.Gray,
                start = Offset(left, top),
                end = Offset(left, bottom),
                strokeWidth = 2f
            )
            // Eje X
            drawLine(
                color = Color.Gray,
                start = Offset(left, bottom),
                end = Offset(w - 8, bottom),
                strokeWidth = 2f
            )
        }

        // Alto útil del gráfico
        val chartUsableHeight = bottom - top

        // Recorremos grupos para dibujar pares de barras
        data.forEachIndexed { index, group ->
            // Centro del grupo
            val groupCenterX = left + (index * widthPerItem.toPx()) + widthPerItem.toPx() / 2f

            // Alturas normalizadas
            val ingresoHeight = (group.ingresos / maxValor) * chartUsableHeight
            val gastoHeight = (group.gastos / maxValor) * chartUsableHeight

            // X de cada barra (ingresos a la izquierda, gastos a la derecha del centro del grupo)
            val barHalfGap = 10.dp.toPx()
            val barW = barWidth.toPx()

            val ingresoLeft = groupCenterX - barHalfGap - barW
            val ingresoRight = groupCenterX - barHalfGap
            val gastoLeft = groupCenterX + barHalfGap
            val gastoRight = groupCenterX + barHalfGap + barW

            // Rectángulo barra Ingresos
            drawRect(
                color = ingresoColor,
                topLeft = Offset(ingresoLeft, bottom - ingresoHeight.toFloat()),
                size = androidx.compose.ui.geometry.Size(
                    width = (ingresoRight - ingresoLeft),
                    height = ingresoHeight.toFloat()
                )
            )

            // Rectángulo barra Gastos
            drawRect(
                color = gastoColor,
                topLeft = Offset(gastoLeft, bottom - gastoHeight.toFloat()),
                size = androidx.compose.ui.geometry.Size(
                    width = (gastoRight - gastoLeft),
                    height = gastoHeight.toFloat()
                )
            )

            // Etiqueta bajo el grupo (mes "Ene", "Feb" o nombre de categoría)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(group.label, groupCenterX, h - 6.dp.toPx(), paint)
            }
        }
    }
}

/** Datos para cada grupo del gráfico: dos barras + etiqueta */
data class BarGroup(
    val label: String,      // "Ene", "Feb" o "Clientes", "Compras", etc.
    val ingresos: Double,   // suma de ingresos del grupo
    val gastos: Double      // suma de gastos del grupo (valor positivo para dibujar)
)



