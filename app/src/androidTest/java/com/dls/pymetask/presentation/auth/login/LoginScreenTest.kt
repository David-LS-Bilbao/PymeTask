


package com.dls.pymetask.presentation.auth.login



import android.content.IntentSender
import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier.Companion.any
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.internal.tls.OkHostnameVerifier.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val viewModel = mockk<LoginViewModel>(relaxed = true)

    private val isLoading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val loginSuccess = MutableStateFlow(false)
    private val googleIntent = MutableStateFlow<IntentSender?>(null)

    @Test
    fun loginScreen_rendersCorrectlyAndTriggersLogin() {
        // Mock estados
        io.mockk.every { viewModel.isLoading } returns isLoading
        io.mockk.every { viewModel.errorMessage } returns errorMessage
        io.mockk.every { viewModel.loginSuccess } returns loginSuccess
        io.mockk.every { viewModel.googleSignInIntent } returns googleIntent

        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {},
                onLoginSuccess = {}
            )
        }

        // Escribir en email y password
        composeTestRule.onNodeWithTag("emailField").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("passwordField").performTextInput("123456")

        // Clic en botón de login
        composeTestRule.onNodeWithText("Iniciar sesión").performClick()

        // Verifica que se llamó a login()
        coVerify { viewModel.login("test@example.com", "123456") }
    }

    @Test
    fun loginScreen_showsErrorIfFieldsAreEmpty() {
        // Mock estados
        io.mockk.every { viewModel.isLoading } returns isLoading
        io.mockk.every { viewModel.errorMessage } returns errorMessage
        io.mockk.every { viewModel.loginSuccess } returns loginSuccess
        io.mockk.every { viewModel.googleSignInIntent } returns googleIntent

        composeTestRule.setContent {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {},
                onLoginSuccess = {}
            )
        }

        // Clic sin rellenar campos
        composeTestRule.onNodeWithText("Iniciar sesión").performClick()

        // Verifica que se llama a setError()
        verify { viewModel.setError(any()) }
    }
}
