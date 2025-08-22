package com.dls.pymetask.presentation.weather



import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    // === Lanzador de permisos: COARSE + FINE ===
    var asked by remember { mutableStateOf(false) } // para no re-lanzar en recomposiciones
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) {
            viewModel.loadByCurrentLocation()
        }
    }

    // Solicita permisos cuando se pinta por primera vez
    LaunchedEffect(Unit) {
        if (!asked) {
            asked = true
            val perms = mutableListOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            // En Android 12+ no necesitamos nada extra para este caso; esto es suficiente.
            permissionLauncher.launch(perms.toTypedArray())
        }
    }

    // Formatos de fecha (usa el locale del sistema)
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM") }

    // === Card del dashboard (compacta) ===
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = ui.error == null && ui.today != null) { sheetOpen = true },
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color= colorResource(R.color.blueWeather))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono redondo con color/acento
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

        // Pie de card: condición o error
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        val footer = when {
            ui.error != null -> stringResource(R.string.weather_retry)
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

    // === Hoja inferior (semanal) ===
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

// Estado interno para abrir/cerrar el sheet.
// (Fuera del composable principal para evitar recomposiciones innecesarias)
private var sheetOpen by mutableStateOf(false)
