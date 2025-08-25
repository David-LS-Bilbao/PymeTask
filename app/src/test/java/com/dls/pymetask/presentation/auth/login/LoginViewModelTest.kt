package com.dls.pymetask.presentation.auth.login

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import app.cash.turbine.test
import com.dls.pymetask.data.auth.GoogleAuthUiClient
import com.dls.pymetask.data.auth.SignInResult
import com.dls.pymetask.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var googleAuthClient: GoogleAuthUiClient
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        authRepository = mockk()
        googleAuthClient = mockk()
        viewModel = LoginViewModel(authRepository, googleAuthClient)
    }

    @Test
    fun `login success updates loginSuccess`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.success(Unit)

        viewModel.login("test@email.com", "123456")

        viewModel.loginSuccess.test {
            val initial = awaitItem() // normalmente es false
            val result = awaitItem()  // esperamos el cambio a true
            assertTrue(result)
            cancelAndConsumeRemainingEvents()
        }
    }



    @Test
    fun `login failure sets errorMessage`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("Login failed"))

        viewModel.login("fail@email.com", "wrong")

        viewModel.errorMessage.test {
            assertEquals("Login failed", awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setError updates errorMessage`() = runTest {
        viewModel.setError("Campos vacíos")

        viewModel.errorMessage.test {
            assertEquals("Campos vacíos", awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `launchGoogleSignIn sets intentSender`() = runTest {
        val intentSender = mockk<IntentSender>()
        coEvery { googleAuthClient.signIn() } returns SignInResult(intentSender = intentSender)

        viewModel.launchGoogleSignIn()

        viewModel.googleSignInIntent.test {
            assertEquals(intentSender, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `onGoogleSignInResult success updates loginSuccess`() = runTest {
        val intent = mockk<Intent>()
        val user = mockk<FirebaseUser>()
        coEvery { googleAuthClient.signInWithIntent(intent) } returns SignInResult(user = user)
        coEvery { authRepository.marcarSesionActiva(any()) } just Runs

        viewModel.onGoogleSignInResult(context, intent)

        viewModel.loginSuccess.test {
            assertTrue(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `onGoogleSignInResult failure sets errorMessage`() = runTest {
        val intent = mockk<Intent>()
        coEvery { googleAuthClient.signInWithIntent(intent) } returns SignInResult(user = null, errorMessage = "Google error", intentSender = null)

        viewModel.onGoogleSignInResult(context, intent)

        viewModel.errorMessage.test {
            assertEquals("Google error", awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
