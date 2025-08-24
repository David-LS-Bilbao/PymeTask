package com.dls.pymetask.presentation.movimientos

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Movimiento

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovimientosScreen(
    navController: NavController,
    viewModel: MovimientosViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userId = com.dls.pymetask.utils.Constants.getUserIdSeguro(context) ?: ""

    val syncing = viewModel.syncing
    val lastMsg = viewModel.lastSyncResult

    val meses by viewModel.meses.collectAsState()
    val noHayMas by viewModel.noHayMas.collectAsState()
    val movimientosDomain by viewModel.movimientos.collectAsState()

    LaunchedEffect(userId) { viewModel.startMonthPaging(userId) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) viewModel.importCsv(context, uri) }
    )

    // Mapeo a UI y orden fuera del Content para que Content sea “tonto”
    val ordenados = remember(movimientosDomain) {
        movimientosDomain.sortedWith(
            compareByDescending<Movimiento> { it.fecha }.thenByDescending { it.id }
        )
    }
    val movimientosUi = remember(ordenados) { ordenados.map { it.toUi() } }

    MovimientosScreenContent(
        navController = navController,
        movimientos = movimientosUi,
        onAddClick = { navController.navigate("crear_movimiento") },
        onItemClick = { ui -> navController.navigate("editar_movimiento/${ui.id}") },
        onImportCsv = { picker.launch(arrayOf("text/*", "text/csv", "application/vnd.ms-excel")) },
        syncing = syncing,
        lastSyncMessage = lastMsg,
        meses = meses,
        noHayMas = noHayMas,
        onMostrarMas = { viewModel.loadNextMonth(userId) },
    )
}
