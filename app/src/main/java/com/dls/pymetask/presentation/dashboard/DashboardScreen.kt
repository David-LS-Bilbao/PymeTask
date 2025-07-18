package com.dls.pymetask.presentation.dashboard



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCardClick: (String) -> Unit = {}
) {
    val cards = listOf(
        DashboardCard("Movimientos", "Ver movimientos registrados", Icons.Default.Euro),
        DashboardCard("Archivos", "Ver archivos guardados", Icons.Default.Folder),
        DashboardCard("Clientes", "Ver lista de clientes", Icons.Default.Person),
        DashboardCard("Notas", "Ver notas guardadas", Icons.Default.List)
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PymeTask", fontFamily = Poppins, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Group, contentDescription = "Clientes") })
                NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") })
            }
        },
        containerColor = Color(0xFFECEFF1) // background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bienvenido",
                fontSize = 26.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF263238)
            )

            Text(
                text = "Administra tu pyme fácilmente",
                fontSize = 16.sp,
                fontFamily = Roboto,
                color = Color(0xFF546E7A)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(cards) { card ->
                    DashboardCardItem(card = card, onClick = { onCardClick(card.title) })
                }
            }
        }
    }
}

@Composable
fun DashboardCardItem(card: DashboardCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = card.title,
                tint = Color(0xFF1976D2),
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFBBDEFB), shape = CircleShape)
                    .padding(4.dp)
            )

            Text(
                text = card.title,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Poppins,
                fontSize = 16.sp,
                color = Color(0xFF263238)
            )

            Text(
                text = card.subtitle,
                fontFamily = Roboto,
                fontSize = 14.sp,
                color = Color(0xFF546E7A)
            )
        }
    }
}

data class DashboardCard(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)
