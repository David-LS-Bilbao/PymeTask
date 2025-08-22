package com.dls.pymetask.domain.repository


import com.dls.pymetask.data.remote.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Servicio Retrofit para consultar Open‑Meteo.
 * No requiere API key.
 *
 * - current: temperatura y weather_code actual
 * - daily: 7 días con min/max y weather_code
 * - timezone=auto: ajusta a la zona del dispositivo
 * - forecast_days=7: solo una semana
 */
interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getWeekForecast(
        // Latitud y longitud del usuario (los pasaremos desde la capa de ubicación)
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,

        // Campos que queremos del tiempo actual
        @Query("current") current: String = "temperature_2m,weather_code",

        // Campos diarios para la semana
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min",

        // Zona horaria automática
        @Query("timezone") timezone: String = "auto",

        @Query("current_weather") currentWeather: Boolean = true,

        // Días de pronóstico
        @Query("forecast_days") days: Int = 7


    ): WeatherResponseDto
}
