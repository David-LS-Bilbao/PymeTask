package com.dls.pymetask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.dls.pymetask.presentation.navigation.PymeNavGraph
import com.dls.pymetask.ui.theme.PymeTaskTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PymeTaskAppRoot()
        }
    }
}

@Composable
fun PymeTaskAppRoot() {
    PymeTaskTheme {
        val navController = rememberNavController()

        Scaffold { innerPadding ->
            // Usamos Box para aplicar correctamente el padding
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)) {
                PymeNavGraph(navController = navController)
            }
        }
    }
}
