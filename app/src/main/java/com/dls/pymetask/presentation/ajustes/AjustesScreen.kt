package com.dls.pymetask.presentation.ajustes

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
import com.dls.pymetask.data.preferences.CurrencyOption
import com.dls.pymetask.data.preferences.DateFormatOption
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

    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }


    val displayVM: DisplayPrefsViewModel = hiltViewModel()
    val dateFormat by displayVM.dateFormat.collectAsState()
    val currency by displayVM.currency.collectAsState()
    val reminderDefault by displayVM.reminderDefault.collectAsState()


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


        // ====== Formato de fecha ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_date_format)) },
            supportingContent = {
                val txt = when (dateFormat) {
                    DateFormatOption.DMY_SLASH -> "dd/MM/yyyy"
                    DateFormatOption.DMY_TEXT -> "dd MMM"
                    DateFormatOption.RELATIVE -> stringResource(R.string.relative_today_tomorrow)
                }
                Text(txt)
            },
            modifier = Modifier.fillMaxWidth().clickable { showDateFormatDialog = true }
        )

// ====== Moneda por defecto ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_currency)) },
            supportingContent = {
                val txt = when (currency) {
                    CurrencyOption.EUR -> "€ Euro"
                    CurrencyOption.USD -> "$ USD"
                    CurrencyOption.GBP -> "£ GBP"
                }
                Text(txt)
            },
            modifier = Modifier.fillMaxWidth().clickable { showCurrencyDialog = true }
        )

// ====== Recordatorios por defecto ======
        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_default_reminder)) },
            supportingContent = { Text(stringResource(if (reminderDefault) R.string.enabled else R.string.disabled)) },
            modifier = Modifier.fillMaxWidth().clickable { displayVM.setReminderDefault(!reminderDefault) }
        )

        var showDateFormatDialog by remember { mutableStateOf(false) }
        var showCurrencyDialog by remember { mutableStateOf(false) }

        // --- Diálogo Formato de fecha ---
        if (showDateFormatDialog) {
            OpcionesDialog(
                titulo = stringResource(R.string.settings_select_date_format),
                opciones = listOf("dd/MM/yyyy", "dd MMM", stringResource(R.string.relative_today_tomorrow)),
                seleccionada = when (dateFormat) {
                    DateFormatOption.DMY_SLASH -> 0
                    DateFormatOption.DMY_TEXT -> 1
                    DateFormatOption.RELATIVE -> 2
                },
                onSeleccionar = { idx ->
                    val opt = when (idx) {
                        0 -> DateFormatOption.DMY_SLASH
                        1 -> DateFormatOption.DMY_TEXT
                        else -> DateFormatOption.RELATIVE
                    }
                    displayVM.setDateFormat(opt)
                    showDateFormatDialog = false
                },
                onCerrar = { showDateFormatDialog = false }
            )
        }

// --- Diálogo Moneda ---
        if (showCurrencyDialog) {
            OpcionesDialog(
                titulo = stringResource(R.string.settings_select_currency),
                opciones = listOf("€ Euro", "$ USD", "£ GBP"),
                seleccionada = when (currency) {
                    CurrencyOption.EUR -> 0
                    CurrencyOption.USD -> 1
                    CurrencyOption.GBP -> 2
                },
                onSeleccionar = { idx ->
                    displayVM.setCurrency(
                        when (idx) {
                            0 -> CurrencyOption.EUR
                            1 -> CurrencyOption.USD
                            else -> CurrencyOption.GBP
                        }
                    )
                    showCurrencyDialog = false
                },
                onCerrar = { showCurrencyDialog = false }
            )
        }





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
