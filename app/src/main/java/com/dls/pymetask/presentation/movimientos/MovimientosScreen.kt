
package com.dls.pymetask.presentation.movimientos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.dls.pymetask.domain.model.Movimiento
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto

@Composable
fun MovimientosScreen(navController: NavController, viewModel: MovimientosViewModel = viewModel()) {
    val selectedTab by viewModel.tipoSeleccionado.collectAsState()
    val movimientosHoy by viewModel.movimientos.collectAsState()
    val dateFormat = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()) }
    val selectedDate = remember { java.util.Date() }

    LaunchedEffect(Unit) {
        viewModel.loadMovimientos()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("crear_movimiento") }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir movimiento")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Tus movimientos",
                fontSize = 22.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF263238)
            )

            Text(
                text = "Muestra tus ingresos y gastos",
                fontSize = 14.sp,
                fontFamily = Roboto,
                color = Color(0xFF546E7A),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = dateFormat.format(selectedDate),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Selecciona fecha") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Ícono de calendario") },
                readOnly = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResumenTab(
                    label = "Ingresos",
                    icon = Icons.Default.ArrowDownward,
                    amount = movimientosHoy.filter { it.ingreso }.sumOf { it.cantidad }.let { "+%.2f €".format(it) },
                    selected = selectedTab == "Ingresos",
                    onClick = { viewModel.setTipo("Ingresos") },
                    modifier = Modifier.weight(1f)
                )
                ResumenTab(
                    label = "Gastos",
                    icon = Icons.Default.ArrowUpward,
                    amount = movimientosHoy.filter { !it.ingreso }.sumOf { it.cantidad }.let { "–%.2f €".format(it) },
                    selected = selectedTab == "Gastos",
                    onClick = { viewModel.setTipo("Gastos") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Hoy",
                fontSize = 16.sp,
                fontFamily = Poppins,
                color = Color(0xFF263238)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(
                    when (selectedTab) {
                        "Ingresos" -> movimientosHoy.filter { it.ingreso }
                        "Gastos" -> movimientosHoy.filter { !it.ingreso }
                        else -> movimientosHoy
                    }
                ) { movimiento ->
                    MovementItem(movimiento = movimiento, onClick = {
                        navController.navigate("editar_movimiento/${movimiento.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun ResumenTab(
    label: String,
    icon: ImageVector,
    amount: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) Color(0xFF1976D2) else Color.White
    val contentColor = if (selected) Color.White else Color(0xFF263238)

    Card(
        modifier = modifier
            .height(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = "Icono de $label", tint = contentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, color = contentColor, fontFamily = Poppins, fontSize = 14.sp)
            }
            Text(amount, color = contentColor, fontFamily = Roboto)
        }
    }
}

@Composable
fun MovementItem(movimiento: Movimiento, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = if (movimiento.ingreso) Icons.Default.TrendingUp else Icons.Default.TrendingDown
            val iconColor = if (movimiento.ingreso) Color(0xFF4CAF50) else Color(0xFFD32F2F)

            Icon(
                icon,
                contentDescription = if (movimiento.ingreso) "Icono ingreso" else "Icono gasto",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor, shape = CircleShape)
                    .padding(6.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movimiento.titulo,
                    fontFamily = Poppins,
                    fontSize = 16.sp,
                    color = Color(0xFF263238),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = movimiento.subtitulo,
                    fontFamily = Roboto,
                    fontSize = 14.sp,
                    color = Color(0xFF546E7A)
                )
            }

            Text(
                text = (if (movimiento.ingreso) "+" else "–") + String.format("%.2f €", kotlin.math.abs(movimiento.cantidad)),
                fontFamily = Roboto,
                color = if (movimiento.ingreso) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

