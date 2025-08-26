package com.dls.pymetask.presentation.weather

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.dls.pymetask.data.location.LocationClient
import com.dls.pymetask.data.mappers.WeatherDailyUi
import com.dls.pymetask.data.mappers.WeatherTodayUi
import com.dls.pymetask.domain.useCase.weather.GetWeatherUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class DashboardWeatherTest {

    // Evita el diálogo del sistema concediendo permisos antes de lanzar la Activity
    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun fakeWeatherViewModel(): WeatherViewModel {
        val weatherFlow = MutableStateFlow(
            WeatherViewModel.UiState(
                isLoading = false,
                city = "Madrid",
                today = WeatherTodayUi(tempC = 25, wmoCode = 1),
                week = listOf(
                    WeatherDailyUi(LocalDate.of(2025, 8, 25), 20, 30, 1),
                    WeatherDailyUi(LocalDate.of(2025, 8, 26), 21, 31, 2)
                ),
                error = null
            )
        )

        val location = mockk<Location>()
        every { location.latitude } returns 40.0
        every { location.longitude } returns -3.0

        val locationClient = mockk<LocationClient>()
        val getWeather = mockk<GetWeatherUseCase>()
        val geocoder = mockk<Geocoder>()

        coEvery { locationClient.getCurrentLocation() } returns location
        coEvery { getWeather(40.0, -3.0) } returns Pair(
            WeatherTodayUi(tempC = 25, wmoCode = 1),
            listOf(
                WeatherDailyUi(LocalDate.of(2025, 8, 25), 20, 30, 1),
                WeatherDailyUi(LocalDate.of(2025, 8, 26), 21, 31, 2)
            )
        )

        every { geocoder.getFromLocation(40.0, -3.0, 1) } returns listOf(
            mockk<Address> { every { locality } returns "Madrid" }
        )

        return object : WeatherViewModel(getWeather, locationClient, geocoder) {
            override val ui = weatherFlow
        }
    }

    @Test
    fun muestraClimaYForecast_correctamente() {
        val viewModel = fakeWeatherViewModel()

        composeRule.setContent {
            DashboardWeatherSection(viewModel = viewModel)
        }

        // Espera a la primera composición
        composeRule.waitForIdle()

        // Comprobaciones iniciales (tolerantes a formato)
        composeRule.onNodeWithText("25°C", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Madrid", substring = true).assertIsDisplayed()

        // Abre la bottom sheet clicando en el contenedor clickable (Card)
        composeRule.onAllNodes(hasClickAction()).onFirst().performClick()

        val locale = Locale.getDefault()
        val d1 = LocalDate.of(2025, 8, 25)
        val d2 = LocalDate.of(2025, 8, 26)
        val d1Full = d1.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        val d2Full = d2.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        val d1Short = d1.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)     // ej. "lun."/"Mon"
        val d2Short = d2.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

        // Espera a que la hoja esté visible (cualquiera de las variantes del primer día)
        composeRule.waitUntil(timeoutMillis = 4_000) {
            composeRule.onAllNodesWithText(d1Full, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithText(d1Short, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithText(d1Short.replace(".", "").take(3), substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }

        // Aserciones robustas para d1
        val d1Candidates = listOf(d1Full, d1Short, d1Short.replace(".", "").take(3))
        val d1Found = d1Candidates.any { cand ->
            composeRule.onAllNodesWithText(cand, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        check(d1Found) { "No se encontró el día 1 con ninguna variante: $d1Candidates" }
        // Comprueba que al menos uno de los candidatos está visible
        d1Candidates.firstOrNull { cand ->
            try { composeRule.onNodeWithText(cand, substring = true, ignoreCase = true).assertIsDisplayed(); true } catch (_: Throwable) { false }
        } ?: error("Ninguna variante visible para d1: $d1Candidates")

        // Aserciones robustas para d2
        val d2Candidates = listOf(d2Full, d2Short, d2Short.replace(".", "").take(3))
        val d2Found = d2Candidates.any { cand ->
            composeRule.onAllNodesWithText(cand, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        check(d2Found) { "No se encontró el día 2 con ninguna variante: $d2Candidates" }
        d2Candidates.firstOrNull { cand ->
            try { composeRule.onNodeWithText(cand, substring = true, ignoreCase = true).assertIsDisplayed(); true } catch (_: Throwable) { false }
        } ?: error("Ninguna variante visible para d2: $d2Candidates")
    }
}

