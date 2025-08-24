package com.dls.pymetask.presentation.movimientos

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dls.pymetask.R

@Composable
internal fun SaldoStickyHeader(
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = stringResource(R.string.nav_back_to_menu))
                }
                Text(stringResource(R.string.movements_summary), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onImportCsv) {
                    Icon(Icons.Default.Download, contentDescription = stringResource(R.string.movements_import_csv))
                    Text(" CSV")
                }
                OutlinedButton(onClick = { navController.navigate("estadisticas") }) {
                    Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.movements_go_stats))
                }
            }
            if (syncing) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                ResumenChip(title = stringResource(R.string.movements_income), amount = ingresos, positive = true)
                ResumenChip(title = stringResource(R.string.movements_expenses), amount = gastos, positive = false)
                ResumenChip(title = stringResource(R.string.movements_balance), amount = saldo, positive = saldo >= 0)
            }
        }
    }
}
