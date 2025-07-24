package com.dls.pymetask.presentation.ajustes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.data.preferences.ThemeMode
import androidx.compose.runtime.livedata.observeAsState



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val theme by viewModel.themeMode.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tema de la aplicación", fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(8.dp))

        listOf(
            ThemeMode.LIGHT to "Claro",
            ThemeMode.DARK to "Oscuro",
            ThemeMode.SYSTEM to "Por sistema"
        ).forEach { (mode, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = theme == mode,
                    onClick = { viewModel.setTheme(mode) }
                )
                Text(label)
            }
        }
    }
}



