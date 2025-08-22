package com.dls.pymetask.domain.repository

import com.dls.pymetask.data.mappers.WeatherDailyUi
import com.dls.pymetask.data.mappers.WeatherTodayUi


/**
 * Abstracción de acceso a datos del tiempo.
 * Devolvemos modelos UI-friendly por simplicidad.
 * (Si quieres, luego migramos a entidades puras de dominio.)
 */
interface WeatherRepository {
    /**
     * Obtiene el tiempo actual y el pronóstico semanal para la lat/lon dadas.
     */
    suspend fun getTodayAndWeek(
        latitude: Double,
        longitude: Double
    ): Pair<WeatherTodayUi?, List<WeatherDailyUi>>
}
