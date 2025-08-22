package com.dls.pymetask.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.domain.useCase.weather.GetWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// SOLO PARA PRUEBA RÁPIDA (puedes borrarlo luego)
@HiltViewModel
class WeatherSmokeViewModel @Inject constructor(
    private val getWeather: GetWeatherUseCase
) : ViewModel() {
//    val log = MutableStateFlow<String>("")
//    init {
//        viewModelScope.launch {
//            try {
//                val (today, week) = getWeather(43.263, -2.935)
//                log.value = "OK: today=${today?.tempC}°C, days=${week.size}"
//            } catch (e: Exception) {
//                log.value = "ERROR: ${e.message}"
//            }
//        }
//    }
}
