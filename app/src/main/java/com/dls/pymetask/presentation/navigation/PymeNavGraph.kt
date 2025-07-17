package com.dls.pymetask.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dls.pymetask.presentation.auth.login.LoginScreen
import com.dls.pymetask.presentation.auth.login.LoginViewModel
import com.dls.pymetask.presentation.auth.register.RegisterScreen
import com.dls.pymetask.presentation.auth.register.RegisterViewModel
import com.dls.pymetask.presentation.dashboard.DashboardScreen

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
                onRegisterClicked = { email, password, _ ->
                    viewModel.register(email, password)
                }
            )
            val registerSuccess by viewModel.registerSuccess.collectAsState()
            LaunchedEffect(registerSuccess) {
                if (registerSuccess) navController.navigate("dashboard")
            }
        }
        composable("dashboard") {
            DashboardScreen(
                onCardClick = { section ->
                    // Aquí puedes manejar navegación a detalles según el título
                }
            )
        }
    }
}
