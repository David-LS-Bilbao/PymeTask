package com.dls.pymetask.data.repository




import com.dls.pymetask.data.mappers.WeatherDailyUi
import com.dls.pymetask.data.mappers.WeatherTodayUi
import com.dls.pymetask.data.mappers.toUiModels
import com.dls.pymetask.domain.repository.WeatherApi
import com.dls.pymetask.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementación que llama a Open‑Meteo y mapea a modelos de UI.
 * Incluye un cachecito en memoria por par (lat,lon) redondeado a 2 decimales para evitar
 * duplicar llamadas si se refresca rápido.
 */
class WeatherRepositoryImpl(
    private val api: WeatherApi
) : WeatherRepository {

    private val memoryCache = mutableMapOf<Pair<Double, Double>, Pair<WeatherTodayUi?, List<WeatherDailyUi>>>()

    override suspend fun getTodayAndWeek(
        latitude: Double,
        longitude: Double
    ): Pair<WeatherTodayUi?, List<WeatherDailyUi>> = withContext(Dispatchers.IO) {
        val key = (latitude.round2() to longitude.round2())
        memoryCache[key] ?: run {
            val dto = api.getWeekForecast(latitude = latitude, longitude = longitude)
            val mapped = dto.toUiModels()
            memoryCache[key] = mapped
            mapped
        }
    }

    private fun Double.round2(): Double = kotlin.math.round(this * 100.0) / 100.0
}
