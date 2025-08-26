package com.dls.pymetask.presentation.agenda


import android.content.Context
import com.dls.pymetask.domain.model.Tarea
import com.dls.pymetask.domain.useCase.tarea.TareaUseCases
import com.dls.pymetask.utils.AlarmUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals

import com.dls.pymetask.utils.getUserIdSeguro


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate


@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalCoroutinesApi
class AgendaViewModelTest {

    private lateinit var viewModel: AgendaViewModel
    private lateinit var tareaUseCases: TareaUseCases
    private lateinit var alarmUtils: AlarmUtils
    private lateinit var context: Context

    private val userId = "testUserId"

    private val testTarea = Tarea(
        id = "1",
        titulo = "Tarea de prueba",
        descripcion = "Descripción",
        fecha = "2025-08-26",
        hora = "10:00",
        activarAlarma = true,
        completado = false,
        userId = userId
    )

    @BeforeEach
    fun setUp() {
        tareaUseCases = mockk(relaxed = true)
        alarmUtils = mockk(relaxed = true)
        context = mockk(relaxed = true)

        mockkStatic("com.dls.pymetask.utils.GetUserIdSeguroKt")
        every { getUserIdSeguro(context) } returns userId

        viewModel = AgendaViewModel(tareaUseCases, alarmUtils, context)
    }

    @Test
    fun `crear tarea activa alarma y la persiste`() = runTest {
        viewModel.guardarTarea(testTarea)

        coVerify { tareaUseCases.addTarea(testTarea.copy(userId = userId), userId) }
        coVerify { alarmUtils.programarAlarma(testTarea) }
    }

    @Test
    fun `completar tarea cancela alarma`() = runTest {
        val completada = testTarea.copy(completado = true)

        viewModel.guardarTarea(completada)

        coVerify { alarmUtils.cancelarAlarma(completada.id) }
        coVerify { tareaUseCases.addTarea(completada.copy(userId = userId, activarAlarma = false), userId) }
    }

    @Test
    fun `eliminar tarea cancela alarma y borra de Firestore`() = runTest {
        viewModel.eliminarTareaPorId(testTarea.id)

        coVerify { alarmUtils.cancelarAlarma(testTarea.id) }
        coVerify { tareaUseCases.deleteTarea(testTarea.id, userId) }
    }

    @Test
    fun `editar tarea actualiza campos`() {
        viewModel.tareaActual = testTarea

        val nuevaFecha = "2025-09-01"
        val nuevaHora = "12:30"

        viewModel.actualizarFecha(nuevaFecha)
        viewModel.actualizarHora(nuevaHora)

        assertEquals(nuevaFecha, viewModel.tareaActual?.fecha)
        assertEquals(nuevaHora, viewModel.tareaActual?.hora)
    }

    @Test
    fun `filtrar tareas por dia semana mes`() = runTest {
        val hoy = LocalDate.now().toString()
        val semana = LocalDate.now().plusDays(5).toString()
        val mes = LocalDate.now().plusDays(20).toString()

        val tareas = listOf(
            testTarea.copy(id = "1", fecha = hoy),
            testTarea.copy(id = "2", fecha = semana),
            testTarea.copy(id = "3", fecha = mes)
        )

        coEvery { tareaUseCases.getTareas(userId) } returns tareas

        viewModel.cargarTareas()

        val lista = viewModel.tareas.first()

        assertEquals(3, lista.size)
        assertEquals("1", lista[0].id)
    }
}


