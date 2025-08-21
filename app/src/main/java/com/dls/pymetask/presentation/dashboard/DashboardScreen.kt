
package com.dls.pymetask.presentation.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource // <-- plurales
import androidx.compose.ui.res.stringResource       // <-- i18n
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dls.pymetask.R
import com.dls.pymetask.presentation.agenda.AgendaViewModel
import com.dls.pymetask.presentation.auth.login.LoginViewModel
import com.dls.pymetask.presentation.navigation.Routes
import com.dls.pymetask.presentation.perfil.EditarPerfilViewModel
import com.dls.pymetask.ui.theme.Poppins
import com.dls.pymetask.ui.theme.Roboto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Suppress("DEPRECATION")
@SuppressLint("SourceLockedOrientationActivity", "NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCardClick: (String) -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
    navController: NavController,
    agendaViewModel: AgendaViewModel = hiltViewModel(),
    perfilViewModel: EditarPerfilViewModel = hiltViewModel()
) {
    // ---- Tarjetas localizadas (títulos/subtítulos) ----
    val cards = listOf(
        DashboardCard(
            title = stringResource(R.string.dashboard_card_movements_title),
            subtitle = stringResource(R.string.dashboard_card_movements_subtitle),
            icon = Icons.Default.Euro
        ),
        DashboardCard(
            title = stringResource(R.string.dashboard_card_files_title),
            subtitle = stringResource(R.string.dashboard_card_files_subtitle),
            icon = Icons.Default.Folder
        ),
        DashboardCard(
            title = stringResource(R.string.dashboard_card_agenda_title),
            subtitle = stringResource(R.string.dashboard_card_agenda_subtitle),
            icon = Icons.Default.ViewAgenda
        ),
        DashboardCard(
            title = stringResource(R.string.dashboard_card_notes_title),
            subtitle = stringResource(R.string.dashboard_card_notes_subtitle),
            icon = Icons.AutoMirrored.Filled.List
        )
    )

    var showLogoutDialog by remember { mutableStateOf(false) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Desactivar landscape
    val context = LocalContext.current
    val activity = context as? Activity
    LaunchedEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Recargar tareas en onResume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("DashboardScreen", "OnResume: recargando tareas")
                agendaViewModel.cargarTareas()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Estado de tareas
    val tareas by agendaViewModel.tareas.collectAsState()
    Log.d("DashboardScreen", "Tareas totales cargadas: ${tareas.size}")

    // Utils de fecha
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    fun parseFechaOrNull(fecha: String) =
        try {
            LocalDate.parse(fecha, isoFormatter)
        } catch (_: DateTimeParseException) {
            null
        }

    val hoy = LocalDate.now()
    val manana = hoy.plusDays(1)
    val tareasHoy = tareas.filter { parseFechaOrNull(it.fecha) == hoy }
    val tareasManana = tareas.filter { parseFechaOrNull(it.fecha) == manana }

    // 👇 Rutas FIJAS en el mismo orden que 'cards'
    val cardRoutes = listOf(
        Routes.MOVIMIENTOS,
        Routes.ARCHIVOS,
        Routes.AGENDA,
        Routes.NOTAS
    )

    LaunchedEffect(Unit) {
        agendaViewModel.cargarTareas()
        perfilViewModel.cargarDatosPerfil()
    }

    // Preparar saludo localizado
    val hour = LocalTime.now().hour
    val perfil by perfilViewModel.perfil.collectAsState()
    val userName = perfil.nombre.ifBlank {
        perfil.email.substringBefore('@').ifBlank { stringResource(R.string.user_generic_name) }
    }

    val greetingPrefix = when {
        hour in 8..20 -> stringResource(R.string.greeting_morning, userName)
        hour >= 21 -> stringResource(R.string.greeting_night, userName)
        else -> stringResource(R.string.greeting_hello, userName)
    }

    val greetingSuffix =
        if (hour in 8..20) {
            if (tareasHoy.isEmpty())
                stringResource(R.string.dashboard_tasks_today_none)
            else
                pluralStringResource(
                    R.plurals.dashboard_tasks_today,
                    tareasHoy.size,
                    tareasHoy.size
                )
        } else if (hour >= 21) {
            if (tareasManana.isEmpty())
                stringResource(R.string.dashboard_tasks_tomorrow_none)
            else
                pluralStringResource(
                    R.plurals.dashboard_tasks_tomorrow,
                    tareasManana.size,
                    tareasManana.size
                )
        } else "" // madrugada: solo "Hola %s"

    val saludo = listOf(greetingPrefix, greetingSuffix).filter { it.isNotBlank() }.joinToString(" ")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name), // "PymeTask"
                        fontSize = 26.sp,
                        fontFamily = Poppins,
                        maxLines = 1,
                        modifier = Modifier.height(56.dp),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(Routes.PERFIL_USER) }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = stringResource(R.string.dashboard_cd_user)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.common_logout)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                NavigationBarItem(
                    selected = currentRoute == Routes.DASHBOARD,
                    modifier = Modifier.height(56.dp),
                    onClick = { navController.navigate(Routes.DASHBOARD) },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = stringResource(R.string.nav_home)
                        )
                    }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.ESTADISTICAS,
                    onClick = { navController.navigate(Routes.ESTADISTICAS) },
                    icon = {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = stringResource(R.string.nav_stats)
                        )
                    }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.CONTACTOS,
                    onClick = { navController.navigate(Routes.CONTACTOS) },
                    icon = {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = stringResource(R.string.nav_contacts)
                        )
                    }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.AJUSTES,
                    onClick = { navController.navigate(Routes.AJUSTES) },
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    }
                )
            }
        },
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
                text = stringResource(R.string.dashboard_welcome_title),
                fontSize = 26.sp,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.dashboard_welcome_subtitle),
                fontSize = 16.sp,
                fontFamily = Roboto,
                color = MaterialTheme.colorScheme.onBackground
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = { navController.navigate(Routes.AGENDA) },
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = saludo,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) { // 👇 Emparejamos cada card con su route fija
                items(cards.size) { index ->
                    val card = cards[index]
                    val route = cardRoutes[index]
                    DashboardCardItem(card = card) {
                        navController.navigate(route)   // ← navegación por route estable
                        onCardClick(route)              // ← si usas el callback, pásale la route (opcional)
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                confirmButton = {
                    Text(
                        text = stringResource(R.string.common_logout),
                        modifier = Modifier.clickable {
                            viewModel.logout(context)
                            showLogoutDialog = false
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.DASHBOARD) { inclusive = true }
                            }
                        },
                        color = Color.Red
                    )
                },
                dismissButton = {
                    Text(
                        text = stringResource(R.string.common_cancel),
                        modifier = Modifier.clickable { showLogoutDialog = false }
                    )
                },
                title = { Text(stringResource(R.string.logout_title)) },
                text = { Text(stringResource(R.string.logout_text)) }
            )
        }
    }
}



