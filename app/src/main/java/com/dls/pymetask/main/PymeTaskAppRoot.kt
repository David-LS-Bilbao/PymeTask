package com.dls.pymetask.main

import android.os.Build

import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.dls.pymetask.data.remote.bank.auth.OAuthManager
import com.dls.pymetask.presentation.ajustes.ThemeViewModel
import com.dls.pymetask.presentation.navigation.PymeNavGraph
import com.dls.pymetask.ui.theme.PymeTaskTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PymeTaskAppRoot(
    oauthManager: OAuthManager,                        // ✅ NUEVO parámetro

    mainViewModel: MainViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    taskIdInicial: String? = null
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val isUserLoggedIn by mainViewModel.isUserLoggedIn.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()

    LaunchedEffect(taskIdInicial, isUserLoggedIn) {
        if (taskIdInicial != null && isUserLoggedIn == true) {
            navController.navigate("tarea_form?taskId=$taskIdInicial")
        }
    }



    // Llama explícitamente a checkLoginStatus
    LaunchedEffect(Unit) {
        mainViewModel.checkLoginStatus(context)
    }

    // 🟢 APLICA el tema aquí:
    PymeTaskTheme(themeMode = themeMode) {
        Scaffold (
            contentWindowInsets = WindowInsets(0.dp) // evita padding extra por insets

        ){ innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isUserLoggedIn != null) {
                    PymeNavGraph(
                        navController = navController,
                        oauthManager = oauthManager,
                        startDestination = if (isUserLoggedIn == true) "dashboard" else "login")
                }
            }
        }
    }
}