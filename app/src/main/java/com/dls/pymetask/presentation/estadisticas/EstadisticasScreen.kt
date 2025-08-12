// NUEVA VERSION






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
