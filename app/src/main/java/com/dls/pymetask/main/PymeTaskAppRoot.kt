package com.dls.pymetask.main

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.dls.pymetask.presentation.ajustes.TextScaleViewModel
import com.dls.pymetask.presentation.ajustes.ThemeViewModel
import com.dls.pymetask.presentation.navigation.PymeNavGraph
import com.dls.pymetask.ui.theme.PymeTaskTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PymeTaskAppRoot(
    mainViewModel: MainViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    textScaleViewModel: TextScaleViewModel = hiltViewModel()
) {
    // Un único NavController gestionado por Compose
    val navController = rememberNavController()

    // Obtener el Intent de la Activity actual
    val context = LocalContext.current
    val activity = context as? Activity

    // Observamos el estado de autenticación tipado
    val authState by mainViewModel.authState.collectAsState()

    // Tema actual de la app
    val themeMode by themeViewModel.themeMode.collectAsState()
    val textScale by textScaleViewModel.textScale.collectAsState()


    LaunchedEffect(Unit) {
        mainViewModel.checkLoginStatus()
    }

    // Manejar navegación desde notificación
    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedIn) {
            val intent = activity?.intent
            if (intent?.action == "com.dls.pymetask.OPEN_TASK" &&
                intent.getBooleanExtra("openAgenda", false)) {
                // Navegar a Agenda después de que se monte el NavGraph
                navController.navigate("agenda")
            }
        }
    }

    PymeTaskTheme(themeMode = themeMode, textScaleFactor = textScale.factor) {
        Scaffold(
            contentWindowInsets = WindowInsets(0.dp) // evita padding extra por insets
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (authState) {
                    is AuthState.Checking -> {
                        // Indicador mientras resolvemos el estado inicial
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is AuthState.LoggedIn -> {
                        // Montamos el NavGraph UNA sola vez con el destino inicial adecuado
                        PymeNavGraph(
                            navController = navController,
                            startDestination = "dashboard"
                        )
                    }
                    is AuthState.LoggedOut -> {
                        PymeNavGraph(
                            navController = navController,
                            startDestination = "login"
                        )
                    }
                }
            }
        }
    }
}
