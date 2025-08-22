package com.dls.pymetask.presentation.weather



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.dls.pymetask.R

/** Devuelve el icono Material para la clave de condición. */
fun iconForKey(key: String): ImageVector = when (key) {
    "sunny" -> Icons.Filled.WbSunny
    "rain"  -> Icons.Filled.WaterDrop
    "snow"  -> Icons.Filled.WaterDrop      // Puedes cambiar por un copo si usas un vector propio
    "storm" -> Icons.Filled.WaterDrop      // Idem (o un rayo si añades un drawable)
    "haze"  -> Icons.Filled.Waves
    else    -> Icons.Filled.Cloud
}

/** Devuelve el texto localizado para la clave de condición. */
@Composable
fun conditionText(key: String): String = when (key) {
    "sunny" -> stringResource(R.string.cond_sunny)
    "rain"  -> stringResource(R.string.cond_rain)
    "snow"  -> stringResource(R.string.cond_snow)
    "storm" -> stringResource(R.string.cond_storm)
    "haze"  -> stringResource(R.string.cond_haze)
    else    -> stringResource(R.string.cond_cloudy)
}
