package com.dls.pymetask.data.remote


import com.squareup.moshi.Json

/**
 * DTOs tal como los devuelve Open‑Meteo.
 * Documentación: https://open-meteo.com/
 *
 * Notas:
 * - "current" viene con valores numéricos y weather_code (entero).
 * - "daily" trae arrays paralelos (fechas/min/max/códigos) de longitud N.
 * - En la capa de mapper normalizamos a modelos de dominio sencillos.
 */
data class WeatherResponseDto(
    @Json(name = "latitude") val latitude: Double?,
    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "timezone") val timezone: String?,
    @Json(name = "current") val current: CurrentDto?,
    @Json(name = "daily") val daily: DailyDto?
)

data class CurrentDto(
    @Json(name = "time") val time: String?,              // ISO "2025-08-21T10:00"
    @Json(name = "temperature_2m") val temperature2m: Double?, // °C
    @Json(name = "weather_code") val weatherCode: Int?   // código WMO
)

data class DailyDto(
    @Json(name = "time") val dates: List<String>?,             // ["2025-08-21", ...]
    @Json(name = "temperature_2m_min") val tempMin: List<Double>?, // [18.3, ...]
    @Json(name = "temperature_2m_max") val tempMax: List<Double>?, // [26.1, ...]
    @Json(name = "weather_code") val weatherCodes: List<Int>?  // [1, 3, 61, ...]
)
