
package com.dls.pymetask.presentation.movimientos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dls.pymetask.R

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
    onImportCsv: () -> Unit = {},
    meses: List<MovimientosViewModel.MesSection> = emptyList(),
    noHayMas: Boolean = false,
    onMostrarMas: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(lastSyncMessage) {
        if (lastSyncMessage != null) snackbarHostState.showSnackbar(lastSyncMessage)
    }

    // Totales (si algún día activas filtros visibles, cambia a movimientosFiltrados)
    val (iTotal, gTotal, saldoTotal) = remember(movimientos) { calcularTotales(movimientos) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { FloatingActionButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.movements_add))
        } }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
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

            if (meses.isEmpty()) {
                item { EstadoVacioMovimientos() }
            } else {
                meses.forEach { section ->
                    item(key = "header-${section.year}-${section.month}") {
                        Text(
                            text = section.title(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    items(
                        items = section.items.map { it.toUi() },
                        key = { it.id }
                    ) { movUi ->
                        MovimientoItem(
                            movimiento = movUi,
                            onClick = { onItemClick(movUi) }
                        )
                        HorizontalDivider()
                    }
                }

                item(key = "footer") {
                    Spacer(Modifier.height(12.dp))
                    if (!noHayMas) {
                        OutlinedButton(
                            onClick = onMostrarMas,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) { Text(stringResource(R.string.movements_show_more)) }
                    } else {
                        Text(
                            stringResource(R.string.movements_no_more),
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
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


