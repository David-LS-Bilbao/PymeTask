package com.dls.pymetask.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.dls.pymetask.presentation.ajustes.ThemeViewModel
import com.dls.pymetask.presentation.navigation.PymeNavGraph
import com.dls.pymetask.ui.theme.PymeTaskTheme

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PymeTaskAppRoot(
    mainViewModel: MainViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isUserLoggedIn by mainViewModel.isUserLoggedIn.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()

    // 🟢 APLICA el tema aquí:
    PymeTaskTheme(themeMode = themeMode) {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isUserLoggedIn != null) {
                    PymeNavGraph(
                        navController = navController,
                        startDestination = if (isUserLoggedIn == true) "dashboard" else "login"
                    )
                }
            }
        }
    }
}