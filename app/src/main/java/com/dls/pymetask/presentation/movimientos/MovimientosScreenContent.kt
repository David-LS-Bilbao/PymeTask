@file:Suppress("DEPRECATION")

package com.dls.pymetask.presentation.movimientos

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
//import com.dls.pymetask.data.remote.bank.auth.OAuthManager
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
    val importe: Double,      // positivo si ingreso, negativo si gasto
    val fechaMillis: Long     // para filtros por mes
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
    // Estado de sync del VM (mutableStateOf en el VM)
    val syncing = viewModel.syncing
    val lastMsg = viewModel.lastSyncResult

    // Activity segura desde el contexto (evita crashes en Preview)
    val context = LocalContext.current
    remember { context.findActivity() }

    val userId = com.dls.pymetask.utils.Constants.getUserIdSeguro(context) ?: ""

    val meses by viewModel.meses.collectAsState()
    val noHayMas by viewModel.noHayMas.collectAsState()

    LaunchedEffect(userId) {
        viewModel.startMonthPaging(userId)
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) viewModel.importCsv(
            context, uri) }
    )

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



    // 4) Delegamos navegación y acciones por callbacks (el content no conoce OAuthManager ni lógica)
    MovimientosScreenContent(
        navController = navController,
        movimientos = movimientosUi,
        onAddClick = { navController.navigate("crear_movimiento") },
        onItemClick = { ui -> navController.navigate("editar_movimiento/${ui.id}") },
         onImportCsv = { picker.launch(arrayOf("text/*", "text/csv", "application/vnd.ms-excel")) },
        syncing = syncing,
        lastSyncMessage = lastMsg,
                // 👇 NUEVO: secciones mensuales y control de paginación
        meses = meses,
        noHayMas = noHayMas,
        onMostrarMas = { viewModel.loadNextMonth(userId) },

    )
}


// ------------------------------------------------------------
// CONTENT PURO: header fijo + lista scroll + totales
// ------------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreenContent(
    navController: NavController,
    movimientos: List<MovimientoUi>,
    onAddClick: () -> Unit = {},
    onItemClick: (MovimientoUi) -> Unit = {},
    syncing: Boolean = false,
    lastSyncMessage: String? = null,
    onImportCsv: () -> Unit = {},            // NUEVO
    meses: List<MovimientosViewModel.MesSection> = emptyList(),
    noHayMas: Boolean = false,
    onMostrarMas: () -> Unit = {}
) {
    // --- Estado de filtros (si decides activarlos abajo) ---
    var filtroTipo by remember { mutableStateOf(FiltroTipo.TODOS) }
    val calNow = remember { Calendar.getInstance().apply { timeInMillis = System.currentTimeMillis() } }
    var filtroYear by remember { mutableIntStateOf(calNow.get(Calendar.YEAR)) }
    var filtroMonth by remember { mutableIntStateOf(calNow.get(Calendar.MONTH)) } // 0..11

    // --- Aplica filtros (ahora no se renderiza la barra; la dejamos lista) ---
    remember(movimientos, filtroTipo, filtroYear, filtroMonth) {
        filtrarMovimientos(movimientos, filtroTipo, filtroYear, filtroMonth)
    }

    // Totales (puedes cambiar a 'movimientosFiltrados' si quieres que el header refleje el filtro)
    val (iTotal, gTotal, saldoTotal) = remember(movimientos) { calcularTotales(movimientos) }

    // Snackbar para feedback de sync
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(lastSyncMessage) {
        if (lastSyncMessage != null) snackbarHostState.showSnackbar(lastSyncMessage)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },      // <-- ahora sí se muestra el snackbar
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
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Header fijo arriba
            stickyHeader {
                SaldoStickyHeader(
                    navController = navController,
                    ingresos = iTotal,
                    gastos = gTotal,
                    saldo = saldoTotal,
                    syncing = syncing,
                    onImportCsv = onImportCsv
                )
            }

            // Si aún no hay meses cargados, muestra estado vacío
            if (meses.isEmpty()) {
                item { EstadoVacioMovimientos() }
            } else {
                // Por cada mes cargado: cabecera + items de ese mes
                meses.forEach { section ->
                    item(key = "header-${section.year}-${section.month}") {
                        Text(
                            text = section.title(), // "Agosto 2025"
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(
                        items = section.items.map { it.toUi() }, // mapeamos dominio -> UI
                        key = { it.id }
                    ) { movUi ->
                        MovimientoItem(
                            movimiento = movUi,
                            onClick = { onItemClick(movUi) }
                        )
                        HorizontalDivider()
                    }
                }

                // Footer: Mostrar más / No hay más
                item(key = "footer") {
                    Spacer(Modifier.height(12.dp))
                    if (!noHayMas) {
                        OutlinedButton(
                            onClick = onMostrarMas,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) { Text("Mostrar más") }
                    } else {
                        Text(
                            "No hay más movimientos",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(Modifier.height(48.dp))
                }
            }


        }
    }
}

// ------------------------------------------------------------
// COMPONENTES DE UI
// ------------------------------------------------------------

/** Busca de forma segura la Activity desde un Context (evita crashes en Preview). */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

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
            .clickable { onClick() },
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

/** Mapper dominio -> UI para esta pantalla. */
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

/** Calcula ingresos, gastos (positivo) y saldo. */
private fun calcularTotales(movimientos: List<MovimientoUi>): Triple<Double, Double, Double> {
    val ingresos = movimientos.filter { it.importe >= 0 }.sumOf { it.importe }
    val gastosAbs = movimientos.filter { it.importe < 0 }.sumOf { -it.importe }
    val saldo = ingresos - gastosAbs
    return Triple(ingresos, gastosAbs, saldo)
}

/** Formatea en EUR/ES; withSign añade +/− en pantalla. */
private fun Double.toCurrency(withSign: Boolean = false): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val base = nf.format(kotlin.math.abs(this))
    return if (!withSign) nf.format(this) else if (this >= 0) "+$base" else "-$base"
}
// ===== Lógica de filtro =====
private fun filtrarMovimientos(
    movimientos: List<MovimientoUi>,
    filtroTipo: FiltroTipo,
    year: Int,
    monthZeroBased: Int
): List<MovimientoUi> {
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
    val porTipo = when (filtroTipo) {
        FiltroTipo.TODOS -> porMes
        FiltroTipo.INGRESOS -> porMes.filter { it.importe >= 0 }
        FiltroTipo.GASTOS -> porMes.filter { it.importe < 0 }
    }
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
    syncing: Boolean = false,
    onImportCsv: () -> Unit = {},
) {
    Surface(tonalElevation = 2.dp, shadowElevation = 4.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                // scroll horizontal
                modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                // botón atrás
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "volver al menú")
                }
                Text("Resumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))

                // boton importar CSV
                OutlinedButton(onClick = onImportCsv) { Text("Importar CSV") }
                Spacer(Modifier.width(8.dp))

                Spacer(Modifier.width(24.dp))
                // botón para ir a estadísticas
                OutlinedButton(onClick = { navController.navigate("estadisticas") }) {
                    Icon(Icons.Default.BarChart, contentDescription = "ir a estadísticas")
                }
            }
            if (syncing) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())) {
                ResumenChip("Ingresos", ingresos, true)
                ResumenChip("Gastos", gastos, false)
                ResumenChip("Saldo", saldo, saldo >= 0)
            }
        }
    }
}

