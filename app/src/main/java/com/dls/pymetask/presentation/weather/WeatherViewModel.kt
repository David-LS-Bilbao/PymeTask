package com.dls.pymetask.presentation.weather



import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.location.LocationClient
import com.dls.pymetask.data.mappers.WeatherDailyUi
import com.dls.pymetask.data.mappers.WeatherTodayUi
import com.dls.pymetask.domain.useCase.weather.GetWeatherUseCase
//import com.dls.pymetask.data.remote.weather.WeatherDailyUi
//import com.dls.pymetask.data.remote.weather.WeatherTodayUi
//import com.dls.pymetask.domain.weather.GetWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel para cargar el tiempo usando la ubicación actual.
 * - Se asume que la UI ya gestionó permisos de ubicación.
 * - Intenta resolver el nombre de la ciudad con Geocoder (best effort).
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeather: GetWeatherUseCase,
    private val locationClient: LocationClient,
    private val geocoder: Geocoder // lo proveemos abajo en DI
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val city: String = "",
        val today: WeatherTodayUi? = null,
        val week: List<WeatherDailyUi> = emptyList(),
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    /**
     * Carga el tiempo según la ubicación actual del dispositivo.
     * Debe llamarse tras conceder permisos desde la UI.
     */
    fun loadByCurrentLocation() {
        _ui.value = _ui.value.copy(isLoading = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loc = locationClient.getCurrentLocation()
                    ?: error("No se pudo obtener la ubicación actual")

                val lat = loc.latitude
                val lon = loc.longitude

                val (today, week) = getWeather(lat, lon)

                // Best-effort: intentamos resolver una localidad legible con Geocoder
                val cityName = runCatching {
                    val list = geocoder.getFromLocation(lat, lon, 1)
                    val addr = list?.firstOrNull()
                    // Ciudades: locality, subAdminArea, adminArea (fallbacks)
                    addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: ""
                }.getOrElse { "" }

                _ui.value = UiState(
                    isLoading = false,
                    city = cityName,
                    today = today,
                    week = week,
                    error = null
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * Permite recargar en caso de error.
     */
    fun retry() = loadByCurrentLocation()
}
