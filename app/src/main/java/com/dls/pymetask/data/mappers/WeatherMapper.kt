package com.dls.pymetask.data.mappers


import com.dls.pymetask.data.remote.WeatherResponseDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Modelos simples para la UI (los usaremos en el ViewModel luego).
 * - WeatherTodayUi: ciudad la pondremos más tarde (por ahora vacío)
 * - WeatherDailyUi: un día del semanal
 */
data class WeatherTodayUi(
    val tempC: Int,
    val wmoCode: Int
)

data class WeatherDailyUi(
    val date: LocalDate,
    val minC: Int,
    val maxC: Int,
    val wmoCode: Int
)

/** Convierte la respuesta Open‑Meteo en Today + Lista semanal. */
fun WeatherResponseDto.toUiModels(): Pair<WeatherTodayUi?, List<WeatherDailyUi>> {
    val today = when {
        currentWeather != null -> {
            val t = (currentWeather.temperature ?: Double.NaN).toInt()
            val code = currentWeather.weatherCode ?: -1
            WeatherTodayUi(tempC = t, wmoCode = code)
        }
        current != null -> {
            val t = (current.temperature2m ?: Double.NaN).toInt()
            val code = current.weatherCode ?: -1
            WeatherTodayUi(tempC = t, wmoCode = code)
        }
        else -> null
    }

    val list = buildList {
        val dates = daily?.dates.orEmpty()
        val mins = daily?.tempMin.orEmpty()
        val maxs = daily?.tempMax.orEmpty()
        val codes = daily?.weatherCodes.orEmpty()
        val n = listOf(dates.size, mins.size, maxs.size, codes.size).minOrNull() ?: 0
        val parser = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        for (i in 0 until n) {
            val date = runCatching { LocalDate.parse(dates[i], parser) }.getOrNull() ?: continue
            add(
                WeatherDailyUi(
                    date = date,
                    minC = mins[i].toInt(),
                    maxC = maxs[i].toInt(),
                    wmoCode = codes[i]
                )
            )
        }
    }
    return today to list
}

