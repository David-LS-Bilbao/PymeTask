package com.dls.pymetask.presentation.estadisticas

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Movimiento
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class EstadisticasScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // ---- Helpers ----------------------------------------------------------

    private fun buildMovimiento(
        id: String,
        cantidad: Double,
        ingreso: Boolean,
        fechaCal: Calendar
    ) = Movimiento(
        id = id,
        titulo = "Test",
        cantidad = cantidad,
        ingreso = ingreso,
        fecha = fechaCal.timeInMillis,
        subtitulo = "",
        userId = "u1"
    )

    private fun string(resId: Int) = composeRule.activity.getString(resId)

    // ---- Tests ------------------------------------------------------------

    @Test
    fun cuandoNoHayMovimientos_muestraTextoVacio() {
        // Stats VM mockeado con estado por defecto (sin movimientos)
        val statsVm = mockk<EstadisticasViewModel>(relaxed = true)
        val uiFlow = MutableStateFlow(EstadisticasUiState()) // estado vacío por defecto
        every { statsVm.ui } returns uiFlow

        // Movimientos VM con flujo vacío
        val movVm = mockk<com.dls.pymetask.presentation.movimientos.MovimientosViewModel>(relaxed = true)
        every { movVm.movimientos } returns MutableStateFlow(emptyList())

        val nav = mockk<androidx.navigation.NavController>(relaxed = true)

        composeRule.setContent {
            EstadisticasScreen(
                navController = nav,
                movimientosVm = movVm,
                statsVm = statsVm
            )
        }

        // Verifica el mensaje de lista vacía en el resumen
        composeRule.onNodeWithText(string(R.string.stats_empty_movements)).assertExists()
    }

    @Test
    fun alPulsarChipHoy_llamaSetPeriodoHoy() {
        val statsVm = mockk<EstadisticasViewModel>(relaxed = true)
        val uiFlow = MutableStateFlow(EstadisticasUiState()) // periodo por defecto
        every { statsVm.ui } returns uiFlow

        val movVm = mockk<com.dls.pymetask.presentation.movimientos.MovimientosViewModel>(relaxed = true)
        every { movVm.movimientos } returns MutableStateFlow(emptyList())
        val nav = mockk<androidx.navigation.NavController>(relaxed = true)

        composeRule.setContent {
            EstadisticasScreen(navController = nav, movimientosVm = movVm, statsVm = statsVm)
        }

        // Click en chip "Hoy"
        composeRule.onNodeWithText(string(R.string.stats_period_today)).performClick()

        // Verificación: se llamó al VM con Periodo.HOY
        verify { statsVm.setPeriodo(Periodo.HOY) }
    }

    @Test
    fun alPulsarToggle_debeLlamarToggleModo() {
        val statsVm = mockk<EstadisticasViewModel>(relaxed = true)
        val uiFlow = MutableStateFlow(EstadisticasUiState(modo = Modo.MES))
        every { statsVm.ui } returns uiFlow

        val movVm = mockk<com.dls.pymetask.presentation.movimientos.MovimientosViewModel>(relaxed = true)
        every { movVm.movimientos } returns MutableStateFlow(emptyList())
        val nav = mockk<androidx.navigation.NavController>(relaxed = true)

        composeRule.setContent {
            EstadisticasScreen(navController = nav, movimientosVm = movVm, statsVm = statsVm)
        }

        // Icono de acción en AppBar (cambia entre MES/COMPARAR)
        composeRule
            .onNodeWithContentDescription(string(R.string.stats_toggle_mode))
            .performClick()

        verify { statsVm.toggleModo() }
    }

    @Test
    fun cuandoLleganMovimientos_seInvocaSetMovimientos() {
        val statsVm = mockk<EstadisticasViewModel>(relaxed = true)
        val uiFlow = MutableStateFlow(EstadisticasUiState(modo = Modo.MES))
        every { statsVm.ui } returns uiFlow

        val movVm = mockk<com.dls.pymetask.presentation.movimientos.MovimientosViewModel>(relaxed = true)

        val cal = Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 10, 12, 0, 0) }
        val testId = "mov-1"
        val lista = listOf(buildMovimiento(testId, cantidad = 100.0, ingreso = true, fechaCal = cal))
        val movFlow: StateFlow<List<Movimiento>> = MutableStateFlow(lista)
        every { movVm.movimientos } returns movFlow

        val nav = mockk<androidx.navigation.NavController>(relaxed = true)

        composeRule.setContent {
            EstadisticasScreen(navController = nav, movimientosVm = movVm, statsVm = statsVm)
        }

        // LaunchedEffect en la pantalla llama a statsVm.setMovimientos(movimientos)
        // Verificamos que se recibe la lista enviada
        verify { statsVm.setMovimientos(match { it.size == 1 && it.first().id == testId }) }
    }
}
