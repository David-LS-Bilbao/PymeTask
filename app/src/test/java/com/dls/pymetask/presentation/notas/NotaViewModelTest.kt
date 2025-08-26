

package com.dls.pymetask.presentation.notas

import com.dls.pymetask.domain.model.Nota
import com.dls.pymetask.domain.useCase.nota.NotaUseCases
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class NotaViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val useCases: NotaUseCases = mockk(relaxed = true)
    private lateinit var viewModel: NotaViewModel

    private val TEST_NOTA = Nota(
        id = "1",
        titulo = "Titulo de prueba",
        contenido = "Contenido de prueba",
        fecha = 1234L,
        colorHex = "#FFFFFF",
        contactoId = null,
        posicion = 0
    )

    @Before
    fun setup() {
        viewModel = NotaViewModel(useCases)
    }

    @Test
    fun cargarNotas_actualizaLista() = runTest {
        val notas = listOf(TEST_NOTA)
        coEvery { useCases.getNotas() } returns notas

        viewModel.cargarNotas()

        assertEquals(notas, viewModel.notas.first())
        coVerify { useCases.getNotas() }
    }

    @Test
    fun seleccionarNota_actualizaNotaActual() = runTest {
        coEvery { useCases.getNota("1") } returns TEST_NOTA

        viewModel.seleccionarNota("1")

        assertEquals(TEST_NOTA, viewModel.notaActual)
    }

    @Test
    fun limpiarNotaActual_laPoneNull() {
        viewModel.notaActual = TEST_NOTA
        viewModel.limpiarNotaActual()
        assertNull(viewModel.notaActual)
    }

    @Test
    fun guardarNota_llamaAddYRecarga() = runTest {
        coEvery { useCases.getNotas() } returns listOf(TEST_NOTA)

        viewModel.guardarNota(TEST_NOTA)

        coVerify { useCases.addNota(TEST_NOTA) }
        coVerify { useCases.getNotas() }
    }

    @Test
    fun eliminarNotaPorId_llamaDeleteYRecarga() = runTest {
        coEvery { useCases.getNotas() } returns emptyList()

        viewModel.eliminarNotaPorId("1")

        coVerify { useCases.deleteNota("1") }
        coVerify { useCases.getNotas() }
    }
}

// =============================================
// Regla para reemplazar Dispatchers.Main en tests
// =============================================
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}



//package com.dls.pymetask.presentation.notas
//
//
//import com.dls.pymetask.domain.model.Nota
//import com.dls.pymetask.domain.useCase.nota.NotaUseCases
//import io.mockk.*
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.test.*
//import org.junit.*
//import org.junit.rules.TestWatcher
//import org.junit.runner.Description
//import kotlin.test.assertEquals
//import kotlin.test.assertNull
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class NotaViewModelTest {
//
//    @get:Rule val mainRule = MainDispatcherRule()
//
//    private val useCases: NotaUseCases = mockk(relaxed = true)
//    private lateinit var viewModel: NotaViewModel
//
//    private val TEST_NOTA = Nota(
//        id = "1",
//        titulo = "Titulo",
//        contenido = "Contenido",
//        fecha = 1234L,
//        colorHex = "#FFFFFF"
//    )
//
//    @Before
//    fun setup() {
//        viewModel = NotaViewModel(useCases)
//    }
//
//    @Test
//    fun cargarNotas_actualizaLista() = runTest {
//        val notas = listOf(TEST_NOTA)
//        coEvery { useCases.getNotas() } returns notas
//
//        viewModel.cargarNotas()
//
//        assertEquals(notas, viewModel.notas.first())
//        coVerify { useCases.getNotas() }
//    }
//
//    @Test
//    fun seleccionarNota_actualizaNotaActual() = runTest {
//        coEvery { useCases.getNota("1") } returns TEST_NOTA
//
//        viewModel.seleccionarNota("1")
//
//        assertEquals(TEST_NOTA, viewModel.notaActual)
//    }
//
//    @Test
//    fun limpiarNotaActual_laPoneNull() {
//        viewModel.notaActual = TEST_NOTA
//        viewModel.limpiarNotaActual()
//        assertNull(viewModel.notaActual)
//    }
//
//    @Test
//    fun guardarNota_llamaAddYRecarga() = runTest {
//        coEvery { useCases.getNotas() } returns listOf(TEST_NOTA)
//
//        viewModel.guardarNota(TEST_NOTA)
//
//        coVerify { useCases.addNota(TEST_NOTA) }
//        coVerify { useCases.getNotas() }
//    }
//
//    @Test
//    fun eliminarNotaPorId_llamaDeleteYRecarga() = runTest {
//        coEvery { useCases.getNotas() } returns emptyList()
//
//        viewModel.eliminarNotaPorId("1")
//
//        coVerify { useCases.deleteNota("1") }
//        coVerify { useCases.getNotas() }
//    }
//}
//
//// Regla para Dispatchers.Main en tests
//@OptIn(ExperimentalCoroutinesApi::class)
//class MainDispatcherRule(
//    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
//) : TestWatcher() {
//    override fun starting(description: Description) {
//        Dispatchers.setMain(dispatcher)
//    }
//    override fun finished(description: Description) {
//        Dispatchers.resetMain()
//    }
//}
