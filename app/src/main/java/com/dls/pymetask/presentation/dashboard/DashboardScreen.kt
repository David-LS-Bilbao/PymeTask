package com.dls.pymetask.presentation.dashboard



import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dls.pymetask.data.preferences.DefaultAppPreferences
import com.dls.pymetask.main.MainViewModel
import com.dls.pymetask.presentation.auth.login.LoginViewModel
import com.dls.pymetask.presentation.navigation.Routes
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto

@SuppressLint("SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCardClick: (String) -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
    navController: NavController
) {
    val cards = listOf(
        DashboardCard("Movimientos", "Ver movimientos registrados", Icons.Default.Euro),
        DashboardCard("Archivos", "Ver archivos guardados", Icons.Default.Folder),
        DashboardCard("Agenda", "Agenda de tareas", Icons.Default.ViewAgenda),
        DashboardCard("Notas", "Ver notas guardadas", Icons.AutoMirrored.Filled.List)
    )
    var showLogoutDialog by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route



//desactivar modo landscape
    val context = LocalContext.current
    val activity = context as? Activity
    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    // reactivar modo landscape
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }//---------------------------------------------------





    Scaffold(modifier = Modifier.fillMaxSize(),

        // barra de navegación superior con íconos
        topBar = {
            TopAppBar(
                title = { Text("PymeTask",
                    fontSize = 26.sp,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("perfil_user")
                    }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Usuario")
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }

            )
        },
        // barra de navegación inferior con iconos
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") }
                )
                NavigationBarItem(
                    selected = currentRoute == "estadisticas",
                    onClick = { navController.navigate("estadisticas") },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas") }
                )
                NavigationBarItem(
                    selected = currentRoute == "contactos",
                    onClick = { navController.navigate(Routes.CONTACTOS) },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Contactos") }
                )
                NavigationBarItem(
                    selected = currentRoute == "ajustes",
                    onClick = { navController.navigate("ajustes") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") }
                )
            }
        },
        // fondo de la pantalla segun el tema
        containerColor = MaterialTheme.colorScheme.background


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
                color = MaterialTheme.colorScheme.onBackground// color del texto segun el tema
            )

            Text(
                text = "Administra tu pyme fácilmente",
                fontSize = 16.sp,
                fontFamily = Roboto,
                color =  MaterialTheme.colorScheme.onBackground
            )

            // cuadrícula de tarjetas con clics en cada una
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),// número de columnas
                verticalArrangement = Arrangement.spacedBy(12.dp),// espacio vertical entre elementos
                horizontalArrangement = Arrangement.spacedBy(12.dp),// espacio horizontal entre elementos
                modifier = Modifier.fillMaxHeight()
            ) {
                // lista de tarjetas con datos
                items(cards) { card ->
                    DashboardCardItem(card = card, onClick = { onCardClick(card.title) })
                }
            }
        }
    }


    // cuadro de diálogo de cierre de sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Text(
                    "Cerrar sesión",
                    modifier = Modifier.clickable {
                        viewModel.logout(context)
                        showLogoutDialog = false
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    },
                    color = Color.Red
                )
            },
            dismissButton = {
                Text(
                    "Cancelar",
                    modifier = Modifier.clickable { showLogoutDialog = false }
                )
            },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") }
        )
    }//---------------------------------------
}

