package com.dls.pymetask.presentation.ajustes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dls.pymetask.BuildConfig
import com.dls.pymetask.R
import com.dls.pymetask.data.preferences.DefaultAppPreferences
import com.dls.pymetask.data.preferences.ThemeMode
import com.dls.pymetask.presentation.navigation.Routes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Pantalla de Ajustes:
 * - Tema (claro/oscuro/sistema)
 * - Idioma (es/en/fr) con recreación inmediata
 * - Tamaño de fuente (pequeño/mediano/grande) persistente a nivel app
 * - Enlaces a FAQ/Guía (placeholder con Intent)
 * - Info de la app (versión)
 * - Reset de preferencias (tema+idioma+fuente+prefs de MIME internas)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    navController: NavController,
    // ViewModel de tema ya existente (mantén tu implementación actual)
    viewModel: ThemeViewModel = hiltViewModel(),
    // Nuevo ViewModel de idioma (abajo te proporciono la clase)
    languageViewModel: LanguageViewModel = hiltViewModel(),
    // Nuevo ViewModel para tamaño de fuente (abajo te proporciono la clase)
    textScaleViewModel: TextScaleViewModel = hiltViewModel()
) {
    val theme by viewModel.themeMode.collectAsState()
    val languageCode by languageViewModel.languageCode.collectAsState()
    val textScale by textScaleViewModel.textScale.collectAsState() // SMALL/MEDIUM/LARGE

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados UI para diálogos
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Opciones de Tema (textos localizados)
    val opcionesTema = listOf(
        ThemeMode.LIGHT to stringResource(R.string.theme_light),
        ThemeMode.DARK to stringResource(R.string.theme_dark),
        ThemeMode.SYSTEM to stringResource(R.string.theme_system)
    )

    // Idiomas (textos localizados) y códigos ISO
    val idiomasVisibles = listOf(
        stringResource(R.string.lang_es),
        stringResource(R.string.lang_en),
        stringResource(R.string.lang_fr)
    )
    val idiomasCodigos = listOf("es", "en", "fr")

    // Nombre visible del idioma guardado
    val idiomaSeleccionadoNombre = remember(languageCode) {
        val idx = idiomasCodigos.indexOf(languageCode)
        idiomasVisibles.getOrElse(idx) { idiomasVisibles.first() }
    }

    // Tamaños de fuente visibles
    val tamaniosFuente = listOf(
        stringResource(R.string.font_small),
        stringResource(R.string.font_medium),
        stringResource(R.string.font_large)
    )
    // Mapeo enum->texto
    val fuenteSeleccionadaNombre = remember(textScale) {
        when (textScale) {
            TextScale.SMALL -> tamaniosFuente[0]
            TextScale.MEDIUM -> tamaniosFuente[1]
            TextScale.LARGE -> tamaniosFuente[2]
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // ====== Tema ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_theme)) },
            supportingContent = {
                Text(opcionesTema.firstOrNull { it.first == theme }?.second ?: "")
            },
            modifier = Modifier.fillMaxWidth().clickable { showThemeDialog = true }
        )

        // ====== Idioma ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_language)) },
            supportingContent = { Text(idiomaSeleccionadoNombre) },
            modifier = Modifier.fillMaxWidth().clickable { showLanguageDialog = true }
        )

        // ====== Tamaño de fuente ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_font_size)) },
            supportingContent = { Text(fuenteSeleccionadaNombre) },
            modifier = Modifier.fillMaxWidth().clickable { showFontSizeDialog = true }
        )

        // ====== FAQ ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_faq)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Navega a la pantalla interna de FAQ
                    // Usamos una ruta estable (no traducir el nombre de la ruta)
                    navController.navigate(Routes.FAQ)
            }
        )

        // ====== Guía/Manual ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_guide)) },
            modifier = Modifier.fillMaxWidth().clickable {
                // Navega a la pantalla interna de Instrucciones/Guía
                navController.navigate(Routes.INSTRUCTIONS)
            }
        )

        // ====== Info de la app ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_app_info)) },
            modifier = Modifier.fillMaxWidth().clickable { showInfoDialog = true }
        )

        // ====== Reset preferencias ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_reset_prefs)) },
            supportingContent = { Text(stringResource(R.string.settings_reset_prefs)) },
            leadingContent = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth().clickable {
                // 1) Limpia asociaciones de apertura de archivos internas (tu clase existente)
                val prefs = DefaultAppPreferences(context)
                prefs.obtenerTodas().forEach { (mime, _) ->
                    if (mime.contains("*") || mime.contains("octet-stream")) {
                        prefs.eliminarApp(mime)
                    }
                }
                // 2) Tema -> Sistema
                viewModel.setTheme(ThemeMode.SYSTEM)
                // 3) Idioma -> Sistema
                scope.launch {
                    languageViewModel.setLanguage("system")
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                }
                // 4) Fuente -> Mediana
                scope.launch { textScaleViewModel.setTextScale(TextScale.MEDIUM) }

                Toast.makeText(context, R.string.settings_reset_done, Toast.LENGTH_SHORT).show()
                // 5) Recrea actividad para aplicar todo de golpe (tema/idioma/tipografía)
                (context as? android.app.Activity)?.recreate()
            }
        )

        // ===================== DIÁLOGOS =====================

        // --- Tema ---
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

        // --- Idioma ---
        if (showLanguageDialog) {
            OpcionesDialog(
                titulo = stringResource(R.string.settings_select_language),
                opciones = idiomasVisibles,
                seleccionada = idiomasCodigos.indexOf(languageCode).takeIf { it >= 0 } ?: 0,
                onSeleccionar = { index ->
                    val code = idiomasCodigos.getOrElse(index) { "es" }
                    scope.launch {
                        // 1) Persistimos
                        languageViewModel.setLanguage(code)
                        // 2) Esperamos a que el flujo refleje el nuevo valor
                        languageViewModel.languageCode.first { it == code }
                        // 3) Aplicamos a nivel de app y recreamos
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(code)
                        )
                        (context as? android.app.Activity)?.recreate()
                        showLanguageDialog = false
                    }
                },
                onCerrar = { showLanguageDialog = false }
            )
        }



        // --- Tamaño de fuente ---
        if (showFontSizeDialog) {
            OpcionesDialog(
                titulo = stringResource(R.string.settings_select_font_size),
                opciones = tamaniosFuente,
                seleccionada = when (textScale) {
                    TextScale.SMALL -> 0
                    TextScale.MEDIUM -> 1
                    TextScale.LARGE -> 2
                },
                onSeleccionar = { idx ->
                    scope.launch {
                        val newScale = when (idx) {
                            0 -> TextScale.SMALL
                            2 -> TextScale.LARGE
                            else -> TextScale.MEDIUM
                        }
                        textScaleViewModel.setTextScale(newScale)
                        // Forzamos recreación para recalcular tipografías globales
                        (context as? android.app.Activity)?.recreate()
                        showFontSizeDialog = false
                    }
                },
                onCerrar = { showFontSizeDialog = false }
            )
        }

        // --- Info App ---
        if (showInfoDialog) {
            val versionName = BuildConfig.VERSION_NAME
            val versionCode = BuildConfig.VERSION_CODE
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text(stringResource(R.string.info_title)) },
                text = {
                    Text(
                        // Muestra nombre de paquete, versión y un texto corto
                        "${stringResource(R.string.app_name)}\n" +
                                "v$versionName ($versionCode)\n" +
                                "package: ${context.packageName}"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }
    }
}








//package com.dls.pymetask.presentation.ajustes
//
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatDelegate
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.unit.dp
//import androidx.core.os.LocaleListCompat
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import com.dls.pymetask.R
//import com.dls.pymetask.data.preferences.DefaultAppPreferences
//import com.dls.pymetask.data.preferences.ThemeMode
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AjustesScreen(
//    navController: NavController,
//    // ViewModel de tema ya existente
//    viewModel: ThemeViewModel = hiltViewModel(),
//    // Nuevo ViewModel de idioma
//    languageViewModel: LanguageViewModel = hiltViewModel()
//) {
//    // Estado del tema almacenado
//    val theme by viewModel.themeMode.collectAsState()
//
//    // Estado del idioma almacenado (código "es", "en", "fr")
//    val languageCode by languageViewModel.languageCode.collectAsState()
//
//    val context = LocalContext.current
//    context as? android.app.Activity
//
//    val scope = rememberCoroutineScope()
//
//    // Estados para abrir diálogos
//    var showThemeDialog by remember { mutableStateOf(false) }
//    var showLanguageDialog by remember { mutableStateOf(false) }
//    var showFontSizeDialog by remember { mutableStateOf(false) }
//    var showInfoDialog by remember { mutableStateOf(false) }
//
//
//    // Mapea los idiomas a códigos ISO que guardaremos
//    val idiomasVisibles = listOf("Español", "Inglés", "Francés")
//    val idiomasCodigos = listOf("es", "en", "fr")
//
//    // Deriva el nombre visible a partir del código guardado
//    val idiomaSeleccionadoNombre = remember(languageCode) {
//        val idx = idiomasCodigos.indexOf(languageCode)
//        idiomasVisibles.getOrElse(idx) { "Español" }
//    }
//
//    // Fuente de ejemplo (persistencia se implementará después si lo deseas)
//    val tamaniosFuente = listOf("Pequeño", "Mediano", "Grande")
//    var fuenteSeleccionada by remember { mutableStateOf(tamaniosFuente[1]) }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        // Top bar con botón volver
//        TopAppBar(
//            title = { Text(stringResource(R.string.settings_title)) },
//            navigationIcon = {
//                IconButton(onClick = { navController.popBackStack() }) {
//                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
//                }
//            }
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//
//        // Opciones tema localizadas
//        val opcionesTema = listOf(
//            ThemeMode.LIGHT to stringResource(R.string.theme_light),
//            ThemeMode.DARK to stringResource(R.string.theme_dark),
//            ThemeMode.SYSTEM to stringResource(R.string.theme_system)
//        )
//
//// Idiomas visibles y códigos (nombres localizados)
//        val idiomasVisibles = listOf(
//            stringResource(R.string.lang_es),
//            stringResource(R.string.lang_en),
//            stringResource(R.string.lang_fr)
//        )
//        val idiomasCodigos = listOf("es", "en", "fr")
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.settings_theme)) },
//            supportingContent = {
//                Text(
//                    opcionesTema.firstOrNull { it.first == theme }?.second ?: ""
//                )
//            },
//            modifier = Modifier.fillMaxWidth().clickable { showThemeDialog = true }
//        )
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.settings_language)) },
//            supportingContent = { Text(idiomaSeleccionadoNombre) },
//            modifier = Modifier.fillMaxWidth().clickable { showLanguageDialog = true }
//        )
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.settings_font_size)) },
//            supportingContent = { Text(fuenteSeleccionada) },
//            modifier = Modifier.fillMaxWidth().clickable { showFontSizeDialog = true }
//        )
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.settings_faq)) },
//            modifier = Modifier.fillMaxWidth().clickable {
//                Toast.makeText(context, (R.string.coming_soon_faq), Toast.LENGTH_SHORT).show()
//            }
//        )
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.settings_guide)) },
//            modifier = Modifier.fillMaxWidth().clickable {
//                Toast.makeText(context, (R.string.coming_soon_instructions), Toast.LENGTH_SHORT)
//                    .show()
//            }
//        )
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.settings_app_info)) },
//            modifier = Modifier.fillMaxWidth().clickable { showInfoDialog = true }
//        )
//
//        ListItem(
//            headlineContent = { Text(stringResource(R.string.settings_reset_prefs)) },
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
//
//
//        // ===================== DIÁLOGOS =====================
//
//        // Diálogo selección de Tema
//        if (showThemeDialog) {
//            OpcionesDialog(
//                titulo = stringResource(R.string.select_theme),
//                opciones = opcionesTema.map { it.second },
//                seleccionada = opcionesTema.indexOfFirst { it.first == theme },
//                onSeleccionar = {
//                    viewModel.setTheme(opcionesTema[it].first)
//                    showThemeDialog = false
//                },
//                onCerrar = { showThemeDialog = false }
//            )
//        }
//
//        // Diálogo selección de Idioma
//        if (showLanguageDialog) {
//            OpcionesDialog(
//                // ⚠️ usa la clave que tengas en strings: settings_select_language
//                titulo = stringResource(R.string.settings_select_language),
//                opciones = idiomasVisibles,
//                seleccionada = idiomasCodigos.indexOf(languageCode).coerceAtLeast(0),
//                onSeleccionar = { index ->
//                    val code = idiomasCodigos.getOrElse(index) { "es" }
//
//                    scope.launch {
//                        // 1) Pide guardar (si no es suspend, no pasa nada; seguimos)
//                        languageViewModel.setLanguage(code)
//
//                        // 2) ESPERA hasta que el StateFlow refleje el nuevo valor
//                        languageViewModel.languageCode.first { it == code }
//
//                        // 3) Aplica locales y recrea (ya persistido -> no se pisa en attachBaseContext)
//                        AppCompatDelegate.setApplicationLocales(
//                            LocaleListCompat.forLanguageTags(code)
//                        )
//                        (context as? android.app.Activity)?.recreate()
//
//                        showLanguageDialog = false
//                    }
//
//                },
//                onCerrar = { showLanguageDialog = false }
//            )
//        }
//
//        // Diálogo tamaño de fuente (demo visual)
//        if (showFontSizeDialog) {
//            OpcionesDialog(
//                titulo = "Seleccionar tamaño de fuente",
//                opciones = tamaniosFuente,
//                seleccionada = tamaniosFuente.indexOf(fuenteSeleccionada),
//                onSeleccionar = {
//                    // TODO: Persistir más adelante si quieres.
//                    fuenteSeleccionada = tamaniosFuente[it]
//                    showFontSizeDialog = false
//                },
//                onCerrar = { showFontSizeDialog = false }
//            )
//        }
//
//        // Diálogo info
//        if (showInfoDialog) {
//            AlertDialog(
//                onDismissRequest = { showInfoDialog = false },
//                title = { Text(stringResource(R.string.info_title)) },
//                text = { Text(stringResource(R.string.info_text)) },
//                confirmButton = {
//                    TextButton(onClick = { showInfoDialog = false }) {
//                        Text(stringResource(R.string.close))
//                    }
//                }
//            )
//        }
//    }
//}
