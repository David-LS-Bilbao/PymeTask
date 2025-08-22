package com.dls.pymetask.domain.useCase.weather

import com.dls.pymetask.data.location.LocationClient
import com.dls.pymetask.data.mappers.WeatherDailyUi
import com.dls.pymetask.data.mappers.WeatherTodayUi

class GetWeatherByDeviceLocationUseCase(
    private val locationClient: LocationClient,
    private val getWeatherUseCase: GetWeatherUseCase
) {
    suspend operator fun invoke(): Result<Pair<WeatherTodayUi?, List<WeatherDailyUi>>> {
        val loc = locationClient.getCurrentLocation()
            ?: return Result.failure(IllegalStateException("No se pudo obtener la ubicación"))
        return runCatching { getWeatherUseCase(loc.latitude, loc.longitude) }
    }
}
