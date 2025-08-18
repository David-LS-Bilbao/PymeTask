package com.dls.pymetask.presentation.ajustes

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.data.preferences.DefaultAppPreferences
import com.dls.pymetask.data.preferences.ThemeMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val theme by viewModel.themeMode.collectAsState()
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val opcionesTema = listOf(
        ThemeMode.LIGHT to "Claro",
        ThemeMode.DARK to "Oscuro",
        ThemeMode.SYSTEM to "Por sistema"
    )
    val idiomas = listOf("Español", "Inglés", "Francés")
    val tamaniosFuente = listOf("Pequeño", "Mediano", "Grande")
    var idiomaSeleccionado by remember { mutableStateOf(idiomas.first()) }
    var fuenteSeleccionada by remember { mutableStateOf(tamaniosFuente[1]) }

    Column(modifier = Modifier.padding(16.dp)) {

        TopAppBar(
            title = { Text("Ajustes") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        )
        Spacer(Modifier.height(16.dp))

        ListItem(
            headlineContent = { Text("Tema de la aplicación") },
            supportingContent = { Text(opcionesTema.firstOrNull { it.first == theme }?.second ?: "") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showThemeDialog = true }
        )

        ListItem(
            headlineContent = { Text("Idioma") },
            supportingContent = { Text(idiomaSeleccionado) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLanguageDialog = true } // CAMBIADO AQUÍ
        )

        ListItem(
            headlineContent = { Text("Tamaño de fuente") },
            supportingContent = { Text(fuenteSeleccionada) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showFontSizeDialog = true } // CAMBIADO AQUÍ
        )

        ListItem(
            headlineContent = { Text("Preguntas frecuentes (FAQ)") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* navegar o mostrar FAQ */ } // CAMBIADO AQUÍ
        )

        ListItem(
            headlineContent = { Text("Instrucciones de uso") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* navegar o mostrar instrucciones */ } // CAMBIADO AQUÍ
        )

        ListItem(
            headlineContent = { Text("Información de la app") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showInfoDialog = true } // CAMBIADO AQUÍ
        )

        ListItem(
            headlineContent = { Text("Restablecer Preferencias") },
            supportingContent = { Text("__") },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Reset MIME",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val prefs = DefaultAppPreferences(context)
                    prefs.obtenerTodas().forEach { (mime, _) ->
                        if (mime.contains("*") || mime.contains("octet-stream")) {
                            prefs.eliminarApp(mime)
                        }
                    }
                    Toast.makeText(context, "Preferencias de archivo restablecidas", Toast.LENGTH_SHORT).show()
                }
        )


        // Dentro de Column(...) junto al resto de ListItem, añade este bloque:

        ListItem(
            headlineContent = { Text("Bancos (conectar/desconectar)") },
            supportingContent = { Text("Gestiona la conexión con tu banco") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // 👉 Navega a la pantalla específica de bancos
                    navController.navigate("ajustes_bancos")
                }
        )




        HorizontalDivider(modifier = Modifier.fillMaxWidth()) // Ajustado para que ocupe el ancho


    }

    // Diálogos ------------------------------------------------------------------------------------

    if (showThemeDialog) {
        OpcionesDialog(
            titulo = "Seleccionar tema",
            opciones = opcionesTema.map { it.second },
            seleccionada = opcionesTema.indexOfFirst { it.first == theme },
            onSeleccionar = {
                viewModel.setTheme(opcionesTema[it].first)
                showThemeDialog = false
            },
            onCerrar = { showThemeDialog = false }
        )
    }

    if (showLanguageDialog) {
        OpcionesDialog(
            titulo = "Seleccionar idioma",
            opciones = idiomas,
            seleccionada = idiomas.indexOf(idiomaSeleccionado),
            onSeleccionar = {
                idiomaSeleccionado = idiomas[it]
                showLanguageDialog = false
            },
            onCerrar = { showLanguageDialog = false }
        )
    }

    if (showFontSizeDialog) {
        OpcionesDialog(
            titulo = "Seleccionar tamaño de fuente",
            opciones = tamaniosFuente,
            seleccionada = tamaniosFuente.indexOf(fuenteSeleccionada),
            onSeleccionar = {
                fuenteSeleccionada = tamaniosFuente[it]
                showFontSizeDialog = false
            },
            onCerrar = { showFontSizeDialog = false }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Información") },
            text = { Text("Versión 1.0\nAplicación creada por David\n© 2025") },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

}
