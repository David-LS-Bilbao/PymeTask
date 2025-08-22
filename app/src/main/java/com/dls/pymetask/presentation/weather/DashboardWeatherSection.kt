package com.dls.pymetask.presentation.weather



import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.dls.pymetask.R
import java.time.format.DateTimeFormatter

/**
 * Sección completa del dashboard:
 * - Solicita permisos de ubicación.
 * - Carga el tiempo con el WeatherViewModel.
 * - Muestra card de "Tiempo hoy".
 * - Al pulsar, abre hoja inferior con el semanal.

 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardWeatherSection(
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()


    LaunchedEffect(ui) {
        Log.d("WeatherUI", "state: isLoading=${ui.isLoading}, today=${ui.today!=null}, week=${ui.week.size}, error=${ui.error}")
    }


    val context = LocalContext.current

    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var asked by rememberSaveable { mutableStateOf(false) }

    // Launcher para COARSE + FINE
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        Log.d("WeatherUI", "permisos: granted=$granted")
        if (granted) {
            viewModel.loadByCurrentLocation()
        }
    }

    val perms = remember {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // Helper: si hay permiso -> cargar; si no -> pedir permiso
    val requestOrLoad = remember(permissionLauncher, context) {
        {
            val hasCoarse = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasFine = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFine || hasCoarse) {
                viewModel.loadByCurrentLocation()
            } else {
                permissionLauncher.launch(perms)
            }
        }
    }

    // Al entrar por primera vez, no molestamos si ya hay permiso
    LaunchedEffect(Unit) {
        Log.d("WeatherUI", "Requesting permissions…")
        if (!asked) {
            asked = true
            requestOrLoad()
        }
    }

    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when {
                    ui.error != null -> requestOrLoad()              // Tap para reintentar si hubo error
                    ui.today != null -> sheetOpen = true             // Abrir semanal si hay datos
                }
            },
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        // Barra de progreso si está cargando
        if (ui.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colorResource(R.color.blueWeather))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val key = ui.today?.let { wmoToKey(it.wmoCode) } ?: "cloudy"
                Icon(
                    imageVector = iconForKey(key),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.weather_today_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                val subtitle = when {
                    ui.error != null -> stringResource(R.string.weather_error)
                    ui.today != null  -> buildString {
                        if (ui.city.isNotBlank()) append(ui.city).append(" · ")
                        append(ui.week.firstOrNull()?.date?.format(dateFmt) ?: "")
                    }
                    else -> ""
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Text(
                text = ui.today?.let { "${it.tempC}°C" } ?: "—",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

        // Footer: si hay error mostramos CTA de reintento
        if (ui.error != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weather_retry),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { requestOrLoad() }) {
                    Text(stringResource(R.string.weather_retry))
                }
            }
        } else {
            val footer = when {
                ui.today != null -> conditionText(wmoToKey(ui.today!!.wmoCode))
                else -> ""
            }
            Text(
                text = footer,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }

    if (sheetOpen && ui.week.isNotEmpty()) {
        ModalBottomSheet(onDismissRequest = { sheetOpen = false }) {
            WeeklyForecastSheet(
                city = ui.city,
                days = ui.week,
                onClose = { sheetOpen = false }
            )
        }
    }
}




//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DashboardWeatherSection(
//    viewModel: WeatherViewModel = hiltViewModel()
//) {
//    val ui by viewModel.ui.collectAsState()
//
//    var sheetOpen by rememberSaveable { mutableStateOf(false) } // <<-- aquí
//
//
//    // === Lanzador de permisos: COARSE + FINE ===
//    var asked by rememberSaveable { mutableStateOf(false) } // para no re-lanzar en recomposiciones
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions()
//    ) { result ->
//        val granted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
//                result[Manifest.permission.ACCESS_FINE_LOCATION] == true
//        if (granted) {
//            viewModel.loadByCurrentLocation()
//        }
//    }
//
//    // Solicita permisos cuando se pinta por primera vez
//    LaunchedEffect(Unit) {
//        if (!asked) {
//            asked = true
//            val perms = mutableListOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
//            // En Android 12+ no necesitamos nada extra para este caso; esto es suficiente.
//            permissionLauncher.launch(perms.toTypedArray())
//        }
//    }
//
//
//
//    // Formatos de fecha (usa el locale del sistema)
//    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM") }
//
//    // === Card del dashboard (compacta) ===
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(enabled = ui.error == null && ui.today != null) { sheetOpen = true },
//        shape = MaterialTheme.shapes.extraLarge,
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(color= colorResource(R.color.blueWeather))
//                .padding(20.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Icono redondo con color/acento
//            Box(
//                modifier = Modifier
//                    .size(56.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.primaryContainer),
//                contentAlignment = Alignment.Center
//            ) {
//                val key = ui.today?.let { wmoToKey(it.wmoCode) } ?: "cloudy"
//                Icon(
//                    imageVector = iconForKey(key),
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }
//
//            Spacer(Modifier.width(16.dp))
//
//            Column(Modifier.weight(1f)) {
//                Text(
//                    text = stringResource(R.string.weather_today_title),
//                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
//                )
//                val subtitle = when {
//                    ui.error != null -> stringResource(R.string.weather_error)
//                    ui.today != null  -> buildString {
//                        if (ui.city.isNotBlank()) append(ui.city).append(" · ")
//                        append(ui.week.firstOrNull()?.date?.format(dateFmt) ?: "")
//                    }
//                    else -> ""
//                }
//                Text(
//                    text = subtitle,
//                    style = MaterialTheme.typography.bodyLarge.copy(
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                )
//            }
//
//            Text(
//                text = ui.today?.let { "${it.tempC}°C" } ?: "—",
//                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
//            )
//        }
//
//        // Pie de card: condición o error
//        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
//        val footer = when {
//            ui.error != null -> stringResource(R.string.weather_retry)
//            ui.today != null -> conditionText(wmoToKey(ui.today!!.wmoCode))
//            else -> ""
//        }
//        Text(
//            text = footer,
//            style = MaterialTheme.typography.bodyMedium.copy(
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            ),
//            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
//        )
//    }
//
//    // === Hoja inferior (semanal) ===
//    if (sheetOpen && ui.week.isNotEmpty()) {
//        ModalBottomSheet(onDismissRequest = { sheetOpen = false }) {
//            WeeklyForecastSheet(
//                city = ui.city,
//                days = ui.week,
//                onClose = { sheetOpen = false }
//            )
//        }
//    }
//}

