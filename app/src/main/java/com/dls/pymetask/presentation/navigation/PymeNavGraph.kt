package com.dls.pymetask.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dls.pymetask.presentation.agenda.AgendaScreen
import com.dls.pymetask.presentation.agenda.TareaFormScreen
import com.dls.pymetask.presentation.auth.login.LoginScreen
import com.dls.pymetask.presentation.auth.login.LoginViewModel
import com.dls.pymetask.presentation.auth.register.RegisterScreen
import com.dls.pymetask.presentation.auth.register.RegisterViewModel
import com.dls.pymetask.presentation.contactos.ContactosScreen
import com.dls.pymetask.presentation.dashboard.DashboardScreen
import com.dls.pymetask.presentation.movimientos.CrearMovimientoScreen
import com.dls.pymetask.presentation.movimientos.EditarMovimientoScreen
import com.dls.pymetask.presentation.movimientos.MovimientosScreen
import com.dls.pymetask.presentation.estadisticas.EstadisticasScreen
import com.dls.pymetask.presentation.ajustes.AjustesScreen
import com.dls.pymetask.presentation.archivos.ArchivosScreen
import com.dls.pymetask.presentation.archivos.ContenidoCarpetaScreen
import com.dls.pymetask.presentation.contactos.CrearContactoScreen
import com.dls.pymetask.presentation.contactos.DetalleContactoScreen
import com.dls.pymetask.presentation.contactos.EditarContactoScreen

import com.dls.pymetask.presentation.notas.NotaFormScreen
import com.dls.pymetask.presentation.notas.NotasScreen
import com.dls.pymetask.presentation.perfil.EditarPerfilScreen
import com.dls.pymetask.presentation.perfil.PerfilUserScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PymeNavGraph(
    navController: NavHostController,
    startDestination: String = "login"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { navController.navigate("dashboard") }
            )
        }
        composable("register") {
            val viewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterClicked = { email, password, nombre ->
                    viewModel.register(email, password,nombre)
                }
            )
            val registerSuccess by viewModel.registerSuccess.collectAsState()
            LaunchedEffect(registerSuccess) {
                if (registerSuccess) navController.navigate("dashboard")
            }
        }
        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                onCardClick = { section ->
                    // Aquí puedes manejar navegación a detalles según el título
                    when (section) {
                        "Movimientos" -> navController.navigate("movimientos")
                        "Archivos" -> navController.navigate("archivos")
                        "Agenda" -> navController.navigate("agenda")
                        "Notas" -> navController.navigate("notas")

                    }
                }
            )
        }
        composable("movimientos") {
            MovimientosScreen(
                navController = navController,
            )
        }

        composable("crear_movimiento") {
            CrearMovimientoScreen(
                navController = navController
            )
        }
        // editar movimiento nuevo
        composable(
            route = "editar_movimiento/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            id?.let {
                EditarMovimientoScreen(navController = navController, movimientoId = it)
            }
        }
        composable("estadisticas") {
            EstadisticasScreen(navController = navController)
        }
        composable("ajustes") {
            AjustesScreen(navController = navController)
        }
        composable("archivos") {
            ArchivosScreen(navController = navController)
        }
        composable("agenda") {
            AgendaScreen(navController = navController)
        }
        composable("notas") {
            NotasScreen(navController = navController)
        }
        composable("contactos") {
            ContactosScreen(navController = navController)
        }
        composable("perfil_user") {
            PerfilUserScreen(navController = navController)
        }

        composable("editar_perfil") {
            EditarPerfilScreen(onBack = { navController.popBackStack() })
        }



        composable(
            route = "editar_contacto/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            id?.let {
                EditarContactoScreen(navController = navController, contactoId = it)
            }
        }
        composable("crear_contacto") {
            CrearContactoScreen(navController = navController)
        }

        composable("detalle_contacto/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            backStack.arguments?.getString("id")?.let {
                DetalleContactoScreen(navController = navController, contactoId = it)
            }
        }
        composable("nota_form") {
            NotaFormScreen(navController = navController)
        }

        composable(
            route = "nota_form?notaId={notaId}",
            arguments = listOf(navArgument("notaId") {
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            NotaFormScreen(
                navController = navController,
                notaId = backStackEntry.arguments?.getString("notaId")
            )
        }

        composable("contenido_carpeta/{carpetaId}") { backStackEntry ->
            val carpetaId = backStackEntry.arguments?.getString("carpetaId") ?: ""
            ContenidoCarpetaScreen(carpetaId = carpetaId, navController = navController)
        }


        composable(
            route = "tarea_form?taskId={taskId}",
            arguments = listOf(navArgument("taskId") {
                nullable = true
                defaultValue = null
            })
        ) {
            val taskId = it.arguments?.getString("taskId")
            TareaFormScreen(taskId = taskId, navController = navController)
        }





    }
}

