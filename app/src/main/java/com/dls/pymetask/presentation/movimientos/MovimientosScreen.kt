package com.dls.pymetask.presentation.movimientos

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Movimiento
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MovimientosScreen(
    navController: NavController,
    viewModel: MovimientosViewModel = hiltViewModel()
) {
    val movimientos by viewModel.movimientos.collectAsState()
    val context = LocalContext.current

    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    val months = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val years = (2023..LocalDate.now().year).toList().reversed()

    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()

    val movimientosFiltrados = movimientos.filter {
        val date = it.fecha.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        date.monthValue == selectedMonth && date.year == selectedYear
    }.sortedByDescending {
        it.fecha.toDate()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tus movimientos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(Icons.Default.Home, contentDescription = "Inicio")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("crear_movimiento") }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir movimiento")
            }
        }

    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box {
                    OutlinedButton(onClick = { expandedMonth = true }) {
                        Text(text = months[selectedMonth - 1])
                    }
                    DropdownMenu(expanded = expandedMonth, onDismissRequest = { expandedMonth = false }) {
                        months.forEachIndexed { index, month ->
                            DropdownMenuItem(
                                text = { Text(month) },
                                onClick = {
                                    viewModel.onMonthSelected(index + 1)
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }

                Box {
                    OutlinedButton(onClick = { expandedYear = true }) {
                        Text(text = selectedYear.toString())
                    }
                    DropdownMenu(expanded = expandedYear, onDismissRequest = { expandedYear = false }) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString()) },
                                onClick = {
                                    viewModel.onYearSelected(year)
                                    expandedYear = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val movimientosPorDia = movimientosFiltrados.groupBy {
                it.fecha.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                movimientosPorDia.toSortedMap(reverseOrder()).forEach { (dia, listaMovimientos) ->
                    item {
                        Text("Día $dia", style = MaterialTheme.typography.titleMedium)
                    }
                    items(listaMovimientos) { movimiento ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Navegar a la pantalla de edición del movimiento
                                    navController.navigate("editar_movimiento/${movimiento.id}")
                                    Log.d("MovimientosScreen", "Movimiento seleccionado con ID: ${movimiento.id}")
                                    val ruta = "editar_movimiento/${movimiento.id}"
                                    Log.d("NAV_DEBUG", "Navegando a: $ruta")
                                    navController.navigate(ruta)
                                },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = if (movimiento.ingreso) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
                                    ),
                                    contentDescription = if (movimiento.ingreso) "Ingreso" else "Gasto",
                                    tint = if (movimiento.ingreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = movimiento.titulo, style = MaterialTheme.typography.titleMedium)
                                    Text(text = movimiento.subtitulo, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(
                                    text = if (movimiento.ingreso) "+${movimiento.cantidad}€" else "-${movimiento.cantidad}€",
                                    color = if (movimiento.ingreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}












// anterior que funciona-----------------------------------------------------------------------------

//package com.dls.pymetask.presentation.movimientos
//
//import android.os.Build
//import android.util.Log
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import com.dls.pymetask.R
//import java.time.LocalDate
//import java.time.ZoneId
//
//@OptIn(ExperimentalMaterial3Api::class)
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun MovimientosScreen(
//    navController: NavController,
//    viewModel: MovimientosViewModel = hiltViewModel()
//) {
//    val movimientos by viewModel.movimientos.collectAsState()
//    val context = LocalContext.current
//
//    var expandedMonth by remember { mutableStateOf(false) }
//    var expandedYear by remember { mutableStateOf(false) }
//
//    val months = listOf(
//        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
//        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
//    )
//    val years = (2023..LocalDate.now().year).toList().reversed()
//
//    val selectedMonth by viewModel.selectedMonth.collectAsState()
//    val selectedYear by viewModel.selectedYear.collectAsState()
//
//    val movimientosFiltrados = movimientos.filter {
//        val date = it.fecha.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
//        date.monthValue == selectedMonth && date.year == selectedYear
//    }.sortedByDescending {
//        it.fecha.toDate()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Tus movimientos") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = { navController.navigate("dashboard") }) {
//                        Icon(Icons.Default.Home, contentDescription = "Inicio")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = { navController.navigate("editar_movimiento") }) {
//                Icon(Icons.Default.Add, contentDescription = "Añadir movimiento")
//            }
//        }
//    ) { padding ->
//        Column(modifier = Modifier
//            .padding(padding)
//            .padding(16.dp)) {
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                Box {
//                    OutlinedButton(onClick = { expandedMonth = true }) {
//                        Text(text = months[selectedMonth - 1])
//                    }
//                    DropdownMenu(expanded = expandedMonth, onDismissRequest = { expandedMonth = false }) {
//                        months.forEachIndexed { index, month ->
//                            DropdownMenuItem(
//                                text = { Text(month) },
//                                onClick = {
//                                    viewModel.onMonthSelected(index + 1)
//                                    expandedMonth = false
//                                }
//                            )
//                        }
//                    }
//                }
//
//                Box {
//                    OutlinedButton(onClick = { expandedYear = true }) {
//                        Text(text = selectedYear.toString())
//                    }
//                    DropdownMenu(expanded = expandedYear, onDismissRequest = { expandedYear = false }) {
//                        years.forEach { year ->
//                            DropdownMenuItem(
//                                text = { Text(year.toString()) },
//                                onClick = {
//                                    viewModel.onYearSelected(year)
//                                    expandedYear = false
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            val movimientosPorDia = movimientosFiltrados.groupBy {
//                it.fecha.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().dayOfMonth
//            }
//
//            // Mostrar los movimientos por día
//            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                movimientosPorDia.toSortedMap(reverseOrder()).forEach { (dia, listaMovimientos) ->
//                    item {
//                        Text("Día $dia", style = MaterialTheme.typography.titleMedium)
//                    }
//                    items(listaMovimientos) { movimiento ->
//                        Card(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable {//
//                                    Log.d("MovimientosScreen", "Movimiento seleccionado: ${movimiento.titulo} + ${movimiento.id}" )
//                                    navController.navigate("editar_movimiento/${movimiento.id}")
//                                },
//                            elevation = CardDefaults.cardElevation(4.dp)
//                        ) {
//                            Row(
//                                modifier = Modifier.padding(16.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(
//                                    painter = painterResource(
//                                        id = if (movimiento.ingreso) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
//                                    ),
//                                    contentDescription = null,
//                                    tint = if (movimiento.ingreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
//                                    modifier = Modifier.size(32.dp)
//                                )
//                                Spacer(modifier = Modifier.width(16.dp))
//                                Column(modifier = Modifier.weight(1f)) {
//                                    Text(text = movimiento.titulo, style = MaterialTheme.typography.titleMedium)
//                                    Text(text = movimiento.subtitulo, style = MaterialTheme.typography.bodyMedium)
//                                }
//                                Text(
//                                    text = if (movimiento.ingreso) "+${movimiento.cantidad}€" else "-${movimiento.cantidad}€",
//                                    color = if (movimiento.ingreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
