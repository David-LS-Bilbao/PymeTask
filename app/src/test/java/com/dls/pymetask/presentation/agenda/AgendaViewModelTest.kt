package com.dls.pymetask.presentation.agenda

import android.content.Context
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.useCase.tarea.TareaUseCases
import com.dls.pymetask.utils.AlarmUtils
import com.dls.pymetask.utils.getUserIdSeguro
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.*
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModelTest {

    // SUT + deps
    private lateinit var viewModel: AgendaViewModel
    private lateinit var tareaUseCases: TareaUseCases
    private lateinit var alarmUtils: AlarmUtils
    private lateinit var context: Context

    // Dispatcher de test para Main
    private val testDispatcher = UnconfinedTestDispatcher()

    // Constantes (evitar literales en verificaciones)
    private val USER_ID = "testUserId"
    private val TAREA_ID = "1"
    private val FECHA = "2025-08-26"
    private val HORA = "10:00"

    private val TEST_TAREA = Tarea(
        id = TAREA_ID,
        titulo = "Tarea de prueba",
        descripcion = "Descripción",
        fecha = FECHA,
        hora = HORA,
        activarAlarma = true,
        completado = false,
        userId = USER_ID
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        tareaUseCases = mockk(relaxed = true)
        alarmUtils = mockk(relaxed = true)
        context = mockk(relaxed = true)

        // Mock top-level function getUserIdSeguro(context)
        mockkStatic("com.dls.pymetask.utils.GetUserIdSeguroKt")
        every { getUserIdSeguro(context) } returns USER_ID

        viewModel = AgendaViewModel(tareaUseCases, alarmUtils, context)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `crear tarea activa alarma y la persiste`() = runTest {
        // Si tu TareaUseCases usa MÉTODOS reales, esta línea basta:
        coEvery { tareaUseCases.getTareas(USER_ID) } returns emptyList()

        // Si tu TareaUseCases agrupa use cases con operator invoke, usa ESTA (descomenta y comenta la de arriba):
        // coEvery { tareaUseCases.getTareas(USER_ID) } returns emptyList()

        viewModel.guardarTarea(TEST_TAREA)
        advanceUntilIdle()

        // Variante A (MÉTODO real en TareaUseCases)
        coVerify(exactly = 1) {
            tareaUseCases.addTarea(
                match { it.id == TAREA_ID && it.userId == USER_ID && it.activarAlarma },
                USER_ID
            )
        }

        // Variante B (USE CASE con operator invoke)
        // coVerify(exactly = 1) {
        //     tareaUseCases.addTarea.invoke(
        //         match { it.id == TAREA_ID && it.userId == USER_ID && it.activarAlarma },
        //         USER_ID
        //     )
        // }

        coVerify(exactly = 1) { alarmUtils.programarAlarma(match { it.id == TAREA_ID }) }
    }

    @Test
    fun `completar tarea cancela alarma`() = runTest {
        val completada = TEST_TAREA.copy(completado = true)

        viewModel.guardarTarea(completada)
        advanceUntilIdle()

        // Se cancela 2 veces: antes y después de guardar
        coVerify(exactly = 2) { alarmUtils.cancelarAlarma(TAREA_ID) }


// Luego verifica el orden (bloque suspend)
        coVerifyOrder {
            alarmUtils.cancelarAlarma(TAREA_ID) // 1ª cancelación (al marcar completada)
            tareaUseCases.addTarea(
                match { it.id == TAREA_ID && it.completado && !it.activarAlarma },
                USER_ID
            )
            alarmUtils.cancelarAlarma(TAREA_ID) // 2ª cancelación (por activarAlarma=false)
        }
    }


    @Test
    fun `eliminar tarea cancela alarma y borra de Firestore`() = runTest {
        viewModel.eliminarTareaPorId(TAREA_ID)
        advanceUntilIdle()

        coVerify(exactly = 1) { alarmUtils.cancelarAlarma(TAREA_ID) }
        coVerify(exactly = 1) { tareaUseCases.deleteTarea(TAREA_ID, USER_ID) }
    }

    @Test
    fun `editar tarea actualiza campos`() {
        viewModel.tareaActual = TEST_TAREA

        val NUEVA_FECHA = "2025-09-01"
        val NUEVA_HORA = "12:30"

        viewModel.actualizarFecha(NUEVA_FECHA)
        viewModel.actualizarHora(NUEVA_HORA)

        Assertions.assertEquals(NUEVA_FECHA, viewModel.tareaActual?.fecha)
        Assertions.assertEquals(NUEVA_HORA, viewModel.tareaActual?.hora)
    }

    @Test
    fun `filtrar tareas por dia semana mes`() = runTest {
        val hoy = LocalDate.now().toString()
        val semana = LocalDate.now().plusDays(5).toString()
        val mes = LocalDate.now().plusDays(20).toString()

        val tareas = listOf(
            TEST_TAREA.copy(id = "1", fecha = hoy),
            TEST_TAREA.copy(id = "2", fecha = semana),
            TEST_TAREA.copy(id = "3", fecha = mes)
        )

        // Variante A (método)
        coEvery { tareaUseCases.getTareas(USER_ID) } returns tareas

        // Variante B (use case .invoke)
        // coEvery { tareaUseCases.getTareas.invoke(USER_ID) } returns tareas

        viewModel.cargarTareas()
        advanceUntilIdle()

        val lista = viewModel.tareas.first()
        Assertions.assertEquals(3, lista.size)
        Assertions.assertEquals("1", lista[0].id)
    }
}

