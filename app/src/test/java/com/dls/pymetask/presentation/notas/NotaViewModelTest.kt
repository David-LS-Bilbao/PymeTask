package com.dls.pymetask.presentation.notas

import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.domain.useCase.nota.NotaUseCases
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NotaViewModelTest {

    private lateinit var viewModel: NotaViewModel
    private lateinit var useCases: NotaUseCases

    // Constantes para evitar literales en verificaciones/mocks
    private val TEST_ID = "1"
    private val TEST_FECHA = 1_756_000_000_000L
    private val TEST_NOTA = Nota(
        id = TEST_ID,
        titulo = "Título de prueba",
        contenido = "Contenido de prueba",
        fecha = TEST_FECHA,
        colorHex = "#FFFFFF"
    )

    @BeforeEach
    fun setUp() {
        useCases = mockk(relaxed = true)
    }

    @Test
    fun `cargarNotas actualiza lista de notas`() = runTest {
        // Main = dispatcher de test (sin necesidad de advanceUntilIdle)
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        viewModel = NotaViewModel(useCases)

        coEvery { useCases.getNotas() } returns listOf(TEST_NOTA)

        viewModel.cargarNotas()

        assertEquals(listOf(TEST_NOTA), viewModel.notas.value)

        Dispatchers.resetMain()
    }

    @Test
    fun `seleccionarNota actualiza notaActual`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        viewModel = NotaViewModel(useCases)

        coEvery { useCases.getNota(TEST_ID) } returns TEST_NOTA

        viewModel.seleccionarNota(TEST_ID)

        assertEquals(TEST_NOTA, viewModel.notaActual)

        Dispatchers.resetMain()
    }

    @Test
    fun `guardarNota guarda y recarga`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        viewModel = NotaViewModel(useCases)

        // Al guardar, el VM llama a addNota y luego cargarNotas -> getNotas
        coEvery { useCases.getNotas() } returns listOf(TEST_NOTA)

        viewModel.guardarNota(TEST_NOTA)

        coVerify { useCases.addNota(TEST_NOTA) }
        coVerify { useCases.getNotas() }

        Dispatchers.resetMain()
    }

    @Test
    fun `eliminarNotaPorId elimina y recarga`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        viewModel = NotaViewModel(useCases)

        coEvery { useCases.getNotas() } returns emptyList()

        viewModel.eliminarNotaPorId(TEST_ID)

        coVerify { useCases.deleteNota(TEST_ID) }
        coVerify { useCases.getNotas() }

        Dispatchers.resetMain()
    }

    @Test
    fun `limpiarNotaActual pone null`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        viewModel = NotaViewModel(useCases)

        viewModel.notaActual = TEST_NOTA
        viewModel.limpiarNotaActual()

        assertNull(viewModel.notaActual)

        Dispatchers.resetMain()
    }
}
