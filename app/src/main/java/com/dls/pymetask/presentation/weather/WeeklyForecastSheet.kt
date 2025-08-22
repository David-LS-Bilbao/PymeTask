package com.dls.pymetask.presentation.weather



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dls.pymetask.R
import com.dls.pymetask.data.mappers.WeatherDailyUi
import java.time.format.DateTimeFormatter

/**
 * Lista semanal con min/max y condición.
 * Usa tipografías de MaterialTheme (respeta tu escala de texto).
 */
@Composable
fun WeeklyForecastSheet(
    city: String,
    days: List<WeatherDailyUi>,
    onClose: () -> Unit
) {
    val df = DateTimeFormatter.ofPattern("EEE d") // "mié 21"
    Column(Modifier.fillMaxWidth().padding(20.dp)) {
        Text(
            text = if (city.isNotBlank())
                stringResource(R.string.weather_weekly_title, city)
            else
                stringResource(R.string.weather_weekly_title, ""),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))

        days.forEach { d ->
            val key = wmoToKey(d.wmoCode)
            ListItem(
                leadingContent = { Icon(iconForKey(key), contentDescription = null) },
                headlineContent = { Text(d.date.format(df).replaceFirstChar { it.uppercase() }) },
                supportingContent = { Text("${d.minC}° / ${d.maxC}° · ${conditionText(key)}") }
            )
            Divider()
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.weather_close))
        }
    }
}
