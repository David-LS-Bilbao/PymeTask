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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Movimiento
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------------------------------------------------
// MODELO DE UI (exclusivo de la vista)
// ------------------------------------------------------------
data class MovimientoUi(
    val id: String,
    val titulo: String,
    val fechaTexto: String,   // fecha ya formateada para UI
    val importe: Double       // positivo si ingreso, negativo si gasto
)

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
    val movimientosUi = remember(ordenados) {
        ordenados.map { it.toUi() }
    }

    // 4) Delegamos navegación por callbacks (el content no conoce NavController)
    MovimientosScreenContent(
        movimientos = movimientosUi,
        onAddClick = { navController.navigate("crear_movimiento") },
        onItemClick = { ui -> navController.navigate("editar_movimiento/${ui.id}") }
    )
}

// ------------------------------------------------------------
// CONTENT PURO: header fijo + lista scroll + totales
// ------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreenContent(
    movimientos: List<MovimientoUi>,
    onAddClick: () -> Unit = {},
    onItemClick: (MovimientoUi) -> Unit = {}
) {
    // Totales calculados sobre la lista actual
    val (ingresos, gastos, saldo) = remember(movimientos) { calcularTotales(movimientos) }

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
                SaldoStickyHeader(ingresos = ingresos, gastos = gastos, saldo = saldo)
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
private fun SaldoStickyHeader(
    ingresos: Double,
    gastos: Double,
    saldo: Double
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Resumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResumenChip(title = "Ingresos", amount = ingresos, positive = true)
                ResumenChip(title = "Gastos", amount = gastos, positive = false)
                ResumenChip(title = "Saldo", amount = saldo, positive = saldo >= 0)
            }
        }
    }
}

@Composable
private fun ResumenChip(title: String, amount: Double, positive: Boolean) {
    val bg = if (positive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val fg = if (positive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    Surface(color = bg, contentColor = fg, shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(amount.toCurrency(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        importe = importeUi
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
