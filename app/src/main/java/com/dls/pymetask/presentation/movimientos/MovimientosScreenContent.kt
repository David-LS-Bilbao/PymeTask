// NUEVA VERSION
@file:Suppress("DEPRECATION")

package com.dls.pymetask.presentation.movimientos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.data.mappers.toUi
import com.dls.pymetask.presentation.navigation.Routes
import java.text.NumberFormat
import java.util.Locale

/**
 * Modelo mínimo para la VISTA (no cambia tu dominio).
 * - id: identificador único para keys estables en la lista
 * - titulo: lo que mostraremos como concepto/descripcion
 * - fecha: texto de fecha (formato que ya tengas)
 * - importe: cantidad (positiva = ingreso, negativa = gasto)
 */
data class MovimientoUi(
    val id: String,
    val titulo: String,
    val fecha: String,
    val importe: Double
)

/**
 * Pantalla de movimientos con:
 * 1) Cabecera fija (sticky) con la tarjeta de saldo (ingresos, gastos, saldo)
 * 2) Lista con scroll de movimientos
 *
 * NOTA IMPORTANTE:
 * Esta versión NO depende de tu ViewModel. Recibe la lista ya “lista para UI”.
 * Luego, cuando me pases tu Movimiento.kt/VM, creo el wrapper que mapea y conecta.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreenContent(

    viewModel: MovimientosViewModel = hiltViewModel(),
    movimientos: List<MovimientoUi>,
    onAddClick: () -> Unit = {},           // callback para FAB "Añadir"
    onItemClick: (MovimientoUi) -> Unit = {}, // abrir detalle/editar si lo deseas
    onDeleteClick: (MovimientoUi) -> Unit = {} // opcional: swipe/botón borrar futuro
) {
    // -- 1) Cálculo de totales en la propia vista (sin tocar tu VM)
    val (ingresos, gastos, saldo) = remember(movimientos) {
        calcularTotales(movimientos)
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
                .padding(padding)
        ) {
            // -- 2) Header fijo con el saldo (queda pegado arriba al hacer scroll)
            stickyHeader {
                SaldoStickyHeader(
                    ingresos = ingresos,
                    gastos = gastos,
                    saldo = saldo
                )
            }
            // -- 3) Lista de movimientos
            items(
                items = movimientos,
                key = { it.id } // clave estable: importante para rendimiento
            ) { mov ->
                MovimientoItem(
                    movimiento = mov,
                    onClick = { onItemClick(mov) }
                    // onDeleteClick lo dejamos preparado para la siguiente iteración
                )
            }
            // -- 4) Estado vacío (si no hay datos)
            if (movimientos.isEmpty()) {
                item {
                    EstadoVacioMovimientos()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MovimientosScreen(
    navController: NavController,
    viewModel: MovimientosViewModel = hiltViewModel()
) {
    // 1) Colecta del VM
    // Ajusta el nombre/estado según tu VM real (ahora asumo un StateFlow<List<Movimiento>>)
    val movimientosDomain by viewModel.movimientos.collectAsState()

    // 2) Mapeo a la UI mínima que usa la vista
    val movimientosUi = remember(movimientosDomain) {
        movimientosDomain.map { it.toUi() }
    }


    // 3) Navegación: usa tus rutas existentes
    MovimientosScreenContent(
        movimientos = movimientosUi,
        onAddClick = { navController.navigate("crear_movimiento") },
        onItemClick = { ui -> navController.navigate("editar_movimiento/${ui.id}") }
    )
}


/**
 * Cabecera fija con tarjeta de saldo.
 * Muestra:
 * - Total ingresos (suma de importes >= 0)
 * - Total gastos (suma de |importes negativos|)
 * - Saldo = ingresos - gastos
 */
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
            Text(
                text = "Resumen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResumenChip(
                    title = "Ingresos",
                    amount = ingresos,
                    emphasis = ChipEmphasis.Positive
                )
                ResumenChip(
                    title = "Gastos",
                    amount = gastos,
                    emphasis = ChipEmphasis.Negative
                )
                ResumenChip(
                    title = "Saldo",
                    amount = saldo,
                    emphasis = if (saldo >= 0) ChipEmphasis.Positive else ChipEmphasis.Negative
                )
            }
        }
    }
}
/**
 * Pequeña “chip/tarjeta” para cada cifra del resumen.
 * Cambia levemente el estilo según el tipo (positivo/negativo).
 */
private enum class ChipEmphasis { Positive, Negative }

@Composable
private fun ResumenChip(
    title: String,
    amount: Double,
    emphasis: ChipEmphasis
) {
    val color = when (emphasis) {
        ChipEmphasis.Positive -> MaterialTheme.colorScheme.primaryContainer
        ChipEmphasis.Negative -> MaterialTheme.colorScheme.errorContainer
    }
    val textColor = when (emphasis) {
        ChipEmphasis.Positive -> MaterialTheme.colorScheme.onPrimaryContainer
        ChipEmphasis.Negative -> MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        color = color,
        contentColor = textColor,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 0.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            Text(
                text = amount.toCurrency(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Item de la lista de movimientos.
 * - Muestra título, fecha e importe (verde si >= 0, rojo si < 0).
 * - Aquí solo dibujamos; las acciones (click, delete) se pasan por callback.
 */
@Composable
private fun MovimientoItem(
    movimiento: MovimientoUi,
    onClick: () -> Unit = {}
) {
    val esIngreso = movimiento.importe >= 0.0
    val amountColor = if (esIngreso)
        MaterialTheme.colorScheme.tertiary
    else
        MaterialTheme.colorScheme.error

    ListItem(
        headlineContent = {
            Text(
                text = movimiento.titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(
                text = movimiento.fecha,
                style = MaterialTheme.typography.bodySmall
            )
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
            .padding(horizontal = 8.dp, vertical = 2.dp),
        overlineContent = null,
        leadingContent = null,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        colors = ListItemDefaults.colors()
    )
    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
}
/** Estado vacío cuando no hay movimientos que mostrar. */
@Composable
private fun EstadoVacioMovimientos() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No hay movimientos aún.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Utilidad: calcula totales a partir de la lista:
 * - ingresos: suma de importes >= 0
 * - gastos:   suma de |importes| negativos (expuesto en positivo)
 * - saldo:    ingresos - gastos
 */
private fun calcularTotales(movimientos: List<MovimientoUi>): Triple<Double, Double, Double> {
    val ingresos = movimientos.filter { it.importe >= 0 }.sumOf { it.importe }
    val gastosAbs = movimientos.filter { it.importe < 0 }.sumOf { -it.importe }
    val saldo = ingresos - gastosAbs
    return Triple(ingresos, gastosAbs, saldo)
}

/**
 * Formateo a moneda local (España).
 * - withSign: si true, antepone + en positivos para mayor claridad.
 */
private fun Double.toCurrency(withSign: Boolean = false): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    val base = nf.format(kotlin.math.abs(this))
    return if (!withSign) {
        nf.format(this)
    } else {
        if (this >= 0) "+$base" else "-$base"
    }
}