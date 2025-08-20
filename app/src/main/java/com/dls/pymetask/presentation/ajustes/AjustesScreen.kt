package com.dls.pymetask.presentation.ajustes

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.R
import com.dls.pymetask.data.preferences.DefaultAppPreferences
import com.dls.pymetask.data.preferences.ThemeMode
import com.dls.pymetask.utils.applyAppLanguage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    // ViewModel de tema ya existente
    viewModel: ThemeViewModel = hiltViewModel(),
    // Nuevo ViewModel de idioma
    languageViewModel: LanguageViewModel = hiltViewModel()
) {
    // Estado del tema almacenado
    val theme by viewModel.themeMode.collectAsState()

    // Estado del idioma almacenado (código "es", "en", "fr")
    val languageCode by languageViewModel.languageCode.collectAsState()

    val context = LocalContext.current
    val activity = context as? android.app.Activity

    // Estados para abrir diálogos
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }


    // Mapea los idiomas a códigos ISO que guardaremos
    val idiomasVisibles = listOf("Español", "Inglés", "Francés")
    val idiomasCodigos = listOf("es", "en", "fr")

    // Deriva el nombre visible a partir del código guardado
    val idiomaSeleccionadoNombre = remember(languageCode) {
        val idx = idiomasCodigos.indexOf(languageCode)
        idiomasVisibles.getOrElse(idx) { "Español" }
    }

    // Fuente de ejemplo (persistencia se implementará después si lo deseas)
    val tamaniosFuente = listOf("Pequeño", "Mediano", "Grande")
    var fuenteSeleccionada by remember { mutableStateOf(tamaniosFuente[1]) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Top bar con botón volver
        TopAppBar(
            title = { Text(stringResource(R.string.settings_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        )

        Spacer(Modifier.height(16.dp))


        // Opciones tema localizadas
        val opcionesTema = listOf(
            ThemeMode.LIGHT to stringResource(R.string.theme_light),
            ThemeMode.DARK to stringResource(R.string.theme_dark),
            ThemeMode.SYSTEM to stringResource(R.string.theme_system)
        )

// Idiomas visibles y códigos (nombres localizados)
        val idiomasVisibles = listOf(
            stringResource(R.string.lang_es),
            stringResource(R.string.lang_en),
            stringResource(R.string.lang_fr)
        )
        val idiomasCodigos = listOf("es", "en", "fr")

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_theme)) },
            supportingContent = {
                Text(
                    opcionesTema.firstOrNull { it.first == theme }?.second ?: ""
                )
            },
            modifier = Modifier.fillMaxWidth().clickable { showThemeDialog = true }
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_language)) },
            supportingContent = { Text(idiomaSeleccionadoNombre) },
            modifier = Modifier.fillMaxWidth().clickable { showLanguageDialog = true }
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_font_size)) },
            supportingContent = { Text(fuenteSeleccionada) },
            modifier = Modifier.fillMaxWidth().clickable { showFontSizeDialog = true }
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_faq)) },
            modifier = Modifier.fillMaxWidth().clickable {
                Toast.makeText(context, (R.string.coming_soon_faq), Toast.LENGTH_SHORT).show()
            }
        )

//        ListItem(
//            headlineContent = { Text(stringResource(R.string.settings_instructions)) },
//            modifier = Modifier.fillMaxWidth().clickable {
//                Toast.makeText(context, (R.string.coming_soon_instructions), Toast.LENGTH_SHORT)
//                    .show()
//            }
//        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_app_info)) },
            modifier = Modifier.fillMaxWidth().clickable { showInfoDialog = true }
        )

//        ListItem(
//            headlineContent = { Text(stringResource(R.string.settings_reset_mime)) },
//            supportingContent = { Text("__") },
//            leadingContent = {
//                Icon(
//                    Icons.Default.Delete,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            },
//            modifier = Modifier.fillMaxWidth().clickable {
//                val prefs = DefaultAppPreferences(context)
//                prefs.obtenerTodas().forEach { (mime, _) ->
//                    if (mime.contains("*") || mime.contains("octet-stream")) {
//                        prefs.eliminarApp(mime)
//                    }
//                }
//                Toast.makeText(context, (R.string.settings_reset_done), Toast.LENGTH_SHORT).show()
//
//            }
//        )


        // ===================== DIÁLOGOS =====================

        // Diálogo selección de Tema
        if (showThemeDialog) {
            OpcionesDialog(
                titulo = stringResource(R.string.select_theme),
                opciones = opcionesTema.map { it.second },
                seleccionada = opcionesTema.indexOfFirst { it.first == theme },
                onSeleccionar = {
                    viewModel.setTheme(opcionesTema[it].first)
                    showThemeDialog = false
                },
                onCerrar = { showThemeDialog = false }
            )
        }

        // Diálogo selección de Idioma
        if (showLanguageDialog) {
            OpcionesDialog(
                titulo = stringResource(R.string.select_language),
                opciones = idiomasVisibles,
                seleccionada = idiomasCodigos.indexOf(languageCode).coerceAtLeast(0),
                onSeleccionar = { index ->
                    val code = listOf("es","en","fr")[index]
                    languageViewModel.setLanguage(code)                  // 1) Persistir en DataStore
                    applyAppLanguage(context, code) // 2) Aplicar (13+ o compat)
                    activity?.recreate()                                  // 3) Re-crear para recomponer strings
                    showLanguageDialog = false
                },
                onCerrar = { showLanguageDialog = false }
            )
        }

        // Diálogo tamaño de fuente (demo visual)
        if (showFontSizeDialog) {
            OpcionesDialog(
                titulo = "Seleccionar tamaño de fuente",
                opciones = tamaniosFuente,
                seleccionada = tamaniosFuente.indexOf(fuenteSeleccionada),
                onSeleccionar = {
                    // TODO: Persistir más adelante si quieres.
                    fuenteSeleccionada = tamaniosFuente[it]
                    showFontSizeDialog = false
                },
                onCerrar = { showFontSizeDialog = false }
            )
        }

        // Diálogo info
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text(stringResource(R.string.info_title)) },
                text = { Text(stringResource(R.string.info_text)) },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }
    }
}
