package com.dls.pymetask.presentation.auth.register

import app.cash.turbine.test
import com.dls.pymetask.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private lateinit var viewModel: RegisterViewModel
    private val authRepository: AuthRepository = mockk()

    private val email = "test@example.com"
    private val pass = "123456"
    private val nombre = "David"

    @Before
    fun setup() {
        viewModel = RegisterViewModel(authRepository)
    }

    @Test
    fun `register success updates registerSuccess`() = runTest {
        coEvery { authRepository.register(email, pass, nombre, null) } returns Result.success(Unit)

        viewModel.registerSuccess.test {
            // valor inicial
            assertEquals(false, awaitItem())

            viewModel.register(email, pass, nombre)

            // cambio a true tras éxito
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `register failure sets errorMessage`() = runTest {
        val msg = "User already exists"
        coEvery { authRepository.register(email, pass, nombre, null) } returns Result.failure(Exception(msg))

        viewModel.errorMessage.test {
            // primer valor: null inicial del StateFlow
            assertEquals(null, awaitItem())

            viewModel.register(email, pass, nombre)

            // siguiente valor: mensaje de error desde onFailure
            assertEquals(msg, awaitItem())
        }
    }

    @Test
    fun `register updates isLoading correctly`() = runTest {
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Result.success(Unit)

        viewModel.isLoading.test {
            // inicial
            assertEquals(false, awaitItem())

            viewModel.register(email, pass, nombre)

            // al iniciar
            assertEquals(true, awaitItem())
            // al finalizar
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `setError updates errorMessage immediately`() = runTest {
        viewModel.errorMessage.test {
            assertEquals(null, awaitItem())
            viewModel.setError("Campos vacíos")
            assertEquals("Campos vacíos", awaitItem())
        }
    }
}

