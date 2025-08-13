package com.dls.pymetask.presentation.movimientos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Movimiento
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ------------------------------------------------------------
// MODELO DE UI (exclusivo de la vista)
// ------------------------------------------------------------
data class MovimientoUi(
    val id: String,
    val titulo: String,
    val fechaTexto: String,   // fecha ya formateada para UI
    val importe: Double,       // positivo si ingreso, negativo si gasto
    val fechaMillis: Long     // <-- NUEVO: para filtros por mes

)

// ===== Enum para el tipo de filtro =====
private enum class FiltroTipo { TODOS, INGRESOS, GASTOS }



// ------------------------------------------------------------
// WRAPPER: conecta VM -> ordena por fecha -> mapea a UI -> Content
// ------------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreen(
    navController: NavController,
    viewModel: MovimientosViewModel = hiltViewModel()
) {

    val hoy = remember { Calendar.getInstance() }
    val year = hoy.get(Calendar.YEAR)
    val month0 = hoy.get(Calendar.MONTH)

    // 👇 Estos dos son compose state del VM (mutableStateOf), se pueden leer directamente
    val syncing = viewModel.syncing
    val lastMsg = viewModel.lastSyncResult


    // 1) Recogemos el estado del VM (StateFlow<List<Movimiento>>)
    val movimientosDomain by viewModel.movimientos.collectAsState()

    // 2) Ordenamos por fecha (Long) de forma segura (más recientes arriba)
    val ordenados = remember(movimientosDomain) {
        movimientosDomain.sortedWith(
            compareByDescending<Movimiento> { it.fecha }
                .thenByDescending { it.id } // desempate estable
        )
    }
    // 3) Mapeamos a UI (fecha -> "dd/MM/yyyy", signo del importe)
    val movimientosUi = remember(ordenados) { ordenados.map { it.toUi() } }
    // 4) Delegamos navegación por callbacks (el content no conoce NavController)
    MovimientosScreenContent(
        navController = navController,
        movimientos = movimientosUi,
        onAddClick = { navController.navigate("crear_movimiento") },
        onItemClick = { ui -> navController.navigate("editar_movimiento/${ui.id}") },
        onSyncBanco = { viewModel.syncBancoMes(accountId = "demo-account", year = year, month0 = month0) }
    )
// (Opcional) Snackbar de resultado:
    if (syncing) {
        Box(
            modifier = Modifier.fillMaxSize(), // O el tamaño que necesites para el área de alineación
            contentAlignment = Alignment.BottomCenter // Alinea el contenido del Box
        ) { CircularProgressIndicator() }
    }
}

// ------------------------------------------------------------
// CONTENT PURO: header fijo + lista scroll + totales
// ------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreenContent(
    navController: NavController,
    movimientos: List<MovimientoUi>,
    onAddClick: () -> Unit = {},
    onItemClick: (MovimientoUi) -> Unit = {},
    onSyncBanco: () -> Unit ={},
    syncing: Boolean = false,                // 👈 NUEVO
    lastSyncMessage: String? = null          // 👈 NUEVO
) {
    // --- Estado de filtros ---
    var filtroTipo by remember { mutableStateOf(FiltroTipo.TODOS) }

    // Mes/Año seleccionado (por defecto: mes actual)
    val calNow = remember {
        Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() }
    }
    var filtroYear by remember { mutableIntStateOf(calNow.get(Calendar.YEAR)) }
    var filtroMonth by remember { mutableIntStateOf(calNow.get(Calendar.MONTH)) } // 0..11

    // --- Aplica filtros ---
    val movimientosFiltrados = remember(movimientos, filtroTipo, filtroYear, filtroMonth) {
        filtrarMovimientos(movimientos, filtroTipo, filtroYear, filtroMonth)
    }
    // Totales calculados sobre la lista actual
    val (ingresos, gastos, saldo) = remember(movimientos) { calcularTotales(movimientos) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(lastSyncMessage) {
        if (lastSyncMessage != null) {
            snackbarHostState.showSnackbar(lastSyncMessage)
        }
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir movimiento")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp) // deja hueco para el FAB
        ) {
            // Header fijo arriba
            stickyHeader {
                SaldoStickyHeader(
                    navController = navController,
                    ingresos = ingresos,
                    gastos = gastos,
                    saldo = saldo,
                    onSyncBanco = onSyncBanco,
                    syncing = syncing
                )
            }

            if (movimientos.isEmpty()) {
                item { EstadoVacioMovimientos() }
            } else {
                items(
                    items = movimientos,
                    key = { m: MovimientoUi -> m.id } // clave estable
                ) { mov: MovimientoUi ->
                    MovimientoItem(
                        movimiento = mov,
                        onClick = { onItemClick(mov) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

}




// ------------------------------------------------------------
// COMPONENTES DE UI
// ------------------------------------------------------------


@Composable
private fun ResumenChip(title: String, amount: Double, positive: Boolean) {
    val bg = if (positive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val fg = if (positive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    Surface(color = bg, contentColor = fg, shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(amount.toCurrency(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MovimientoItem(
    movimiento: MovimientoUi,
    onClick: () -> Unit = {}
) {
    val esIngreso = movimiento.importe >= 0.0
    val amountColor = if (esIngreso) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

    ListItem(
        headlineContent = {
            Text(movimiento.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(movimiento.fechaTexto, style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
            Text(
                text = movimiento.importe.toCurrency(withSign = true),
                color = amountColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp),
        colors = ListItemDefaults.colors()
    )
}

@Composable
private fun EstadoVacioMovimientos() {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text("No hay movimientos aún.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ------------------------------------------------------------
// HELPERS
// ------------------------------------------------------------

/**
 * Mapper dominio -> UI para esta pantalla.
 * - Formatea fecha Long a "dd/MM/yyyy"
 * - Aplica signo al importe (positivo si ingreso, negativo si gasto)
 */
private fun Movimiento.toUi(): MovimientoUi {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
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

/** Calcula ingresos, gastos (positivo) y saldo */
private fun calcularTotales(movimientos: List<MovimientoUi>): Triple<Double, Double, Double> {
    val ingresos = movimientos.filter { it.importe >= 0 }.sumOf { it.importe }
    val gastosAbs = movimientos.filter { it.importe < 0 }.sumOf { -it.importe }
    val saldo = ingresos - gastosAbs
    return Triple(ingresos, gastosAbs, saldo)
}

/** Formatea en EUR/ES; withSign añade +/− en pantalla */
private fun Double.toCurrency(withSign: Boolean = false): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val base = nf.format(kotlin.math.abs(this))
    return if (!withSign) nf.format(this) else if (this >= 0) "+$base" else "-$base"
}


// ===== Barra de filtros (tipo + mes/año) =====
@Composable
private fun FiltrosBar(
    filtroTipo: FiltroTipo,
    onTipoChange: (FiltroTipo) -> Unit,
    year: Int,
    monthZeroBased: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val mesNombre = remember(year, monthZeroBased) {
        // "agosto 2025"
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year); set(Calendar.MONTH, monthZeroBased)
        }
        SimpleDateFormat("LLLL yyyy", Locale("es", "ES")).format(cal.time)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Filtro por tipo
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filtroTipo == FiltroTipo.TODOS,
                onClick = { onTipoChange(FiltroTipo.TODOS) },
                label = { Text("Todos") }
            )
            FilterChip(
                selected = filtroTipo == FiltroTipo.INGRESOS,
                onClick = { onTipoChange(FiltroTipo.INGRESOS) },
                label = { Text("Ingresos") }
            )
            FilterChip(
                selected = filtroTipo == FiltroTipo.GASTOS,
                onClick = { onTipoChange(FiltroTipo.GASTOS) },
                label = { Text("Gastos") }
            )
        }

        Spacer(Modifier.height(8.dp))

        // Selector mes/año simple (← Mes →)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPrevMonth) { Text("←") }
            Text(mesNombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onNextMonth) { Text("→") }
        }
    }
}

// ===== Lógica de filtro =====
private fun filtrarMovimientos(
    movimientos: List<MovimientoUi>,
    filtroTipo: FiltroTipo,
    year: Int,
    monthZeroBased: Int
): List<MovimientoUi> {
    // 1) Filtra por mes/año usando fechaMillis (rápido y robusto)
    val inicioMes = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthZeroBased)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val finMes = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthZeroBased)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        add(Calendar.MONTH, 1)
        add(Calendar.MILLISECOND, -1)
    }.timeInMillis

    val porMes = movimientos.filter { it.fechaMillis in inicioMes..finMes }

    // 2) Filtra por tipo
    val porTipo = when (filtroTipo) {
        FiltroTipo.TODOS -> porMes
        FiltroTipo.INGRESOS -> porMes.filter { it.importe >= 0 }
        FiltroTipo.GASTOS -> porMes.filter { it.importe < 0 }
    }

    // 3) (Ya vienen ordenados desde el wrapper; si quieres asegurar, reordena por fechaMillis desc)
    return porTipo.sortedWith(
        compareByDescending<MovimientoUi> { it.fechaMillis }
            .thenByDescending { it.id }
    )

}

@Composable
private fun SaldoStickyHeader(
    navController: NavController,
    ingresos: Double,
    gastos: Double,
    saldo: Double,
    onSyncBanco: () -> Unit = {},
    syncing: Boolean = false
) {
    Surface(tonalElevation = 2.dp, shadowElevation = 4.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // boton atras
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "volver al menu")
                }
                Text("Resumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onSyncBanco, enabled = !syncing) {   // 👈 deshabilita si está sincronizando
                    Text(if (syncing) "Sincronizando..." else "Sincronizar banco")
                }
                Spacer(Modifier.width(24.dp))
                // boton para ir a estadisticas
                OutlinedButton(onClick = { navController.navigate("estadisticas") }) {
                    Icon(Icons.Default.BarChart, contentDescription = "ir a estadisticas")
                }
            }
            if (syncing) { LinearProgressIndicator(Modifier.fillMaxWidth()) }  // 👈 barra de progreso
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResumenChip("Ingresos", ingresos, true)
                ResumenChip("Gastos", gastos, false)
                ResumenChip("Saldo", saldo, saldo >= 0)
            }
        }
    }
}



