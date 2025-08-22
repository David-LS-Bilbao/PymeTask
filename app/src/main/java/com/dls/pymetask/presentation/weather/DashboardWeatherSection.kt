package com.dls.pymetask.presentation.weather



import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.dls.pymetask.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val context = LocalContext.current

    val locale = remember { Locale.getDefault() }
    val dateFmt = remember(locale) { DateTimeFormatter.ofPattern("EEEE, d MMMM", locale) }


    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    var asked by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (granted) viewModel.loadByCurrentLocation()
    }

    val perms = remember {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val requestOrLoad = remember(permissionLauncher, context) {
        {
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasFine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) viewModel.loadByCurrentLocation() else permissionLauncher.launch(perms)
        }
    }

    LaunchedEffect(Unit) {
        if (!asked) { asked = true; requestOrLoad() }
    }

    // ⬇️ NUEVO: definimos el color de fondo de TODA la card y su color de contenido
    val bg = colorResource(id = R.color.blueWeather)
    val onBg = MaterialTheme.colorScheme.onPrimary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when {
                    ui.error != null -> requestOrLoad()
                    ui.today != null -> sheetOpen = true
                }
            },
        shape = MaterialTheme.shapes.extraLarge,
        // ⬇️ sin sombra/halo
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            draggedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = bg,
            contentColor = Color.Black   // ← letras negras
        )
    ) {
        if (ui.isLoading) {
            // Usa un tono del onColor para que combine con el fondo
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = onBg.copy(alpha = 0.5f),
                trackColor = onBg.copy(alpha = 0.15f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), // ⬅️ quitamos el .background(...) aquí
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    // pequeño “pill” con el color de contenido atenuado para destacar el icono
                    .background(onBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val key = ui.today?.let { wmoToKey(it.wmoCode) } ?: "cloudy"
                Icon(
                    imageVector = iconForKey(key),
                    contentDescription = null,
                    // ⬇️ hereda el contentColor de la Card; si quieres más contraste, usa onBg
                    tint = Color.Gray
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {

                val todayDate = ui.week.firstOrNull()?.date ?: LocalDate.now()
                val titleText = remember(todayDate, dateFmt) {
                    // Capitaliza la primera letra (p.ej. “Viernes, 22 agosto”)
                    todayDate.format(dateFmt).replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
                    }
                }

                // TÍTULO: fecha del día
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.Black   // ← letras negras
                )

                // SUBTÍTULO: solo la ciudad (sin fecha)
                val subtitleCity = ui.city
                if (subtitleCity.isNotBlank()) {
                    Text(
                        text = subtitleCity,
                        style = MaterialTheme.typography.bodyMedium,
                         color = Color.Gray,   // ← letras grises,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                }

                Text(
                    text = ui.today?.let { "${it.tempC}°C" } ?: "—",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black ,  // ← letras grises
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .widthIn(min = 56.dp)          // ancho mínimo para que no “salte”
                        .wrapContentWidth(Alignment.End)
                )
            }

        }

        // Divider con el mismo esquema de color (suave)
        HorizontalDivider(
            thickness = DividerDefaults.Thickness,
            color = onBg.copy(alpha = 0.12f)
        )
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
