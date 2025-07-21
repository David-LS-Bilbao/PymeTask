package com.dls.pymetask.main

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.dls.pymetask.presentation.navigation.PymeNavGraph
import com.dls.pymetask.ui.theme.PymeTaskTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PymeTaskAppRoot()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PymeTaskAppRoot(viewModel: MainViewModel=hiltViewModel()) {

    val navController = rememberNavController()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()


    PymeTaskTheme {


        Scaffold { innerPadding ->
            // Usamos Box para aplicar correctamente el padding
            Box(modifier = Modifier
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
