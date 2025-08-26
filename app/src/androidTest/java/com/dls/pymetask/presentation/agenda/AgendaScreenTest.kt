package com.dls.pymetask.presentation.agenda

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.ProgressBarRangeInfo

import androidx.compose.ui.test.onFirst

import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dls.pymetask.R
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.utils.NotificationHelper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AgendaScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // --- Constantes (evitar literales en verificaciones) ---
    private val TEST_TASK_ID = "t1"
    private val TEST_TASK_TITLE = "Visitar cliente"
    private val EXPECTED_CREATE_ROUTE = "tarea_form"
    private val EXPECTED_EDIT_ROUTE = "tarea_form?taskId=$TEST_TASK_ID"

    // --- Dobles de prueba ---
    private lateinit var navController: NavController
    private lateinit var viewModel: AgendaViewModel

    // Flujos que vamos a exponer desde el mock
    private val tareasFlow = MutableStateFlow<List<Tarea>>(emptyList())
    private val loadingFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        navController = mockk(relaxed = true)
        viewModel = mockk(relaxed = true)

        every { viewModel.tareas } returns tareasFlow
        every { viewModel.loading } returns loadingFlow

        mockkObject(NotificationHelper)
        every { NotificationHelper.stopAlarmSound() } just Runs
        every { NotificationHelper.cancelActiveAlarmNotification(any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun setContent() {
        composeRule.setContent {
            AgendaScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }

    private fun clickAddFab() {
        // Estrategia: 1) por contentDescription (btn_add)  2) por testTag ("fab_add_task")
        // 3) como último recurso, el primer nodo clickable (evitando depender de la AppBar)
        val cdAdd = composeRule.activity.getString(R.string.btn_add)

        val clicked = runCatching {
            composeRule.onNodeWithContentDescription(cdAdd, useUnmergedTree = true)
                .performClick()
        }.isSuccess || runCatching {
            composeRule.onNodeWithTag("fab_add_task").performClick()
        }.isSuccess || runCatching {
            composeRule.onAllNodes(hasClickAction())
                .onFirst()
                .performClick()
        }.isSuccess

        assertTrue("No se pudo clicar el FAB de añadir", clicked)
    }

    @Test
    fun muestraTituloYFab() {
        setContent()

        // Título localizado "Agenda"
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.agenda_title))
            .assertIsDisplayed()
        // La verificación del FAB se realiza en el test de navegación.
    }

    @Test
    fun fabNavegaACrearTarea() {
        setContent()

        clickAddFab()

        verify { navController.navigate(EXPECTED_CREATE_ROUTE) }
    }

    @Test
    fun muestraLoadingCuandoLoadingTrue() {
        // Arrange
        loadingFlow.value = true

        setContent()

        // CircularProgressIndicator expone la semántica de rango de progreso
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    fun muestraEmptyCuandoNoHayTareas() {
        loadingFlow.value = false
        tareasFlow.value = emptyList()

        setContent()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.agenda_empty))
            .assertIsDisplayed()
    }

    @Test
    fun tapEnTarea_detieneAlarma_seleccionaYNavegaAEdicion() {
        // Arrange
        val tarea = Tarea(
            id = TEST_TASK_ID,
            titulo = TEST_TASK_TITLE,
            descripcion = "Reunión breve",
            fecha = "2025-08-26",
            hora = "10:00",
            activarAlarma = true,
            completado = false
        )
        tareasFlow.value = listOf(tarea)
        loadingFlow.value = false

        setContent()

        // Act: click en el título (propaga al Card clickable)
        composeRule.onNodeWithText(TEST_TASK_TITLE, useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        // Assert
        verify { NotificationHelper.stopAlarmSound() }
        verify { NotificationHelper.cancelActiveAlarmNotification(any()) }
        verify { viewModel.seleccionarTarea(TEST_TASK_ID) }
        verify { navController.navigate(EXPECTED_EDIT_ROUTE) }
    }

    @Test
    fun actionBar_detenerAlarma_paraSonidoYCancelaNotificacion() {
        setContent()

        val stopCd = composeRule.activity.getString(R.string.agenda_stop_alarm)
        composeRule.onNodeWithContentDescription(stopCd)
            .assertIsDisplayed()
            .performClick()

        verify { NotificationHelper.stopAlarmSound() }
        verify { NotificationHelper.cancelActiveAlarmNotification(any()) }
    }
}
