package com.dls.pymetask.domain.useCase.weather

import com.dls.pymetask.data.mappers.WeatherDailyUi
import com.dls.pymetask.data.mappers.WeatherTodayUi
import com.dls.pymetask.domain.repository.WeatherRepository


/**
 * Caso de uso: pedir a repositorio el tiempo actual + semana.
 */
class GetWeatherUseCase(
    private val repo: WeatherRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Pair<WeatherTodayUi?, List<WeatherDailyUi>> = repo.getTodayAndWeek(latitude, longitude)
}
