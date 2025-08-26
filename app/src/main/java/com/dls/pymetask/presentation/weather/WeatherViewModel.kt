package com.dls.pymetask.presentation.weather



import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dls.pymetask.data.location.LocationClient
import com.dls.pymetask.data.mappers.WeatherDailyUi
import com.dls.pymetask.data.mappers.WeatherTodayUi
import com.dls.pymetask.domain.useCase.weather.GetWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * ViewModel para cargar el tiempo usando la ubicación actual.
 * - Se asume que la UI ya gestionó permisos de ubicación.
 * - Intenta resolver el nombre de la ciudad con Geocoder (best effort).
 */

@HiltViewModel
open class WeatherViewModel @Inject constructor(
    private val getWeather: GetWeatherUseCase,
    private val locationClient: LocationClient,
    private val geocoder: Geocoder
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val city: String = "",
        val today: WeatherTodayUi? = null,
        val week: List<WeatherDailyUi> = emptyList(),
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    open val ui: StateFlow<UiState> = _ui



    /**
     * Carga el tiempo según la ubicación actual del dispositivo.
     * Debe llamarse tras conceder permisos desde la UI.
     */




    private val TAG = "WeatherVM"

    fun loadByCurrentLocation() {
        _ui.value = _ui.value.copy(isLoading = true, error = null)
        Log.d(TAG, "loadByCurrentLocation() -> isLoading=true")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loc = locationClient.getCurrentLocation()
                Log.d(TAG, "location = $loc")

                val safeLoc = loc ?: error("No se pudo obtener la ubicación actual")
                val lat = safeLoc.latitude
                val lon = safeLoc.longitude
                Log.d(TAG, "coords = $lat,$lon (obteniendo tiempo…)")

                val (today, week) = getWeather(lat, lon)
                Log.d(TAG, "weather TODAY=$today; WEEK=${week.size} días")

                val cityName = runCatching {
                    val list = geocoder.getFromLocation(lat, lon, 1)
                    val addr = list?.firstOrNull()
                    addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: ""
                }.getOrElse { "" }
                Log.d(TAG, "city = '$cityName'")

                _ui.value = UiState(false, cityName, today, week, null)
                Log.d(TAG, "state -> Success(today=${today!=null}, week=${week.size})")
            } catch (e: SecurityException) {
                Log.e(TAG, "permiso denegado", e)
                _ui.value = _ui.value.copy(isLoading = false, error = "Permiso de ubicación denegado")
            } catch (e: Exception) {
                Log.e(TAG, "falló carga", e)
                _ui.value = _ui.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    fun loadFor(lat: Double, lon: Double) {
        _ui.value = _ui.value.copy(isLoading = true, error = null)
        Log.d(TAG, "loadFor($lat,$lon)")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (today, week) = getWeather(lat, lon)
                Log.d(TAG, "weather (manual) TODAY=$today; WEEK=${week.size} días")
                val cityName = runCatching {
                    val list = geocoder.getFromLocation(lat, lon, 1)
                    val addr = list?.firstOrNull()
                    addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: ""
                }.getOrElse { "" }
                Log.d(TAG, "city (manual)='$cityName'")
                _ui.value = UiState(false, cityName, today, week, null)
            } catch (e: Exception) {
                Log.e(TAG, "falló carga manual", e)
                _ui.value = _ui.value.copy(isLoading = false, error = e.message ?: "Error")
            }
        }
    }















//    fun loadByCurrentLocation() {
//        _ui.value = _ui.value.copy(isLoading = true, error = null)
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val loc = locationClient.getCurrentLocation()
//                    ?: error("No se pudo obtener la ubicación actual")
//
//                val lat = loc.latitude
//                val lon = loc.longitude
//
//                val (today, week) = getWeather(lat, lon)
//
//                // Best-effort: intentamos resolver una localidad legible con Geocoder
//                val cityName = geocoder.cityName(lat, lon)
//                _ui.value = UiState(
//                    isLoading = false,
//                    city = cityName,
//                    today = today,
//                    week = week,
//                    error = null
//                )
//            } catch (e: Exception) {
//                _ui.value = _ui.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
//            }
//        }
//    }

}

private suspend fun Geocoder.cityName(lat: Double, lon: Double): String =
    withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                getFromLocation(lat, lon, 1) { list ->
                    val addr = list.firstOrNull()
                    val name = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: ""
                    cont.resume(name)
                }
            }
        } else {
            runCatching {
                val list = getFromLocation(lat, lon, 1)
                val addr = list?.firstOrNull()
                addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: ""
            }.getOrDefault("")
        }
    }

