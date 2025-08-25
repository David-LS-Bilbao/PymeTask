package com.dls.pymetask.presentation.auth.register

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests instrumentados para RegisterScreen:
 * - Requiere que en RegisterScreen se añadan testTag() a los campos y botones.
 *   nameField, emailField, passwordField, confirmField, registerButton, navigateLoginLink
 *   (ver comentarios en cada selector).
 */
@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun whenAllFieldsFilled_callsOnRegisterClicked_withCorrectArgs() {
        val onNavigateToLogin: () -> Unit = mockk(relaxed = true)
        val onRegisterClicked: (String, String, String) -> Unit = mockk(relaxed = true)

        composeRule.setContent {
            RegisterScreen(
                onNavigateToLogin = onNavigateToLogin,
                onRegisterClicked = onRegisterClicked
            )
        }

        // Usa testTag (recomendado). Si no has añadido los tags, sustituye por onNodeWithText("...") con las cadenas visibles.
        composeRule.onNodeWithTag("nameField").performTextInput("David Test")
        composeRule.onNodeWithTag("emailField").performTextInput("david@test.com")
        composeRule.onNodeWithTag("passwordField").performTextInput("123456")
        composeRule.onNodeWithTag("confirmField").performTextInput("123456")

        composeRule.onNodeWithTag("registerButton").performClick()

        verify { onRegisterClicked("david@test.com", "123456", "David Test") }
    }

    @Test
    fun whenAnyFieldEmpty_doesNotCallOnRegisterClicked() {
        val onNavigateToLogin: () -> Unit = mockk(relaxed = true)
        val onRegisterClicked: (String, String, String) -> Unit = mockk(relaxed = true)

        composeRule.setContent {
            RegisterScreen(onNavigateToLogin = onNavigateToLogin, onRegisterClicked = onRegisterClicked)
        }

        // Rellenamos solo algunos campos
        composeRule.onNodeWithTag("nameField").performTextInput("David Test")
        composeRule.onNodeWithTag("emailField").performTextInput("david@test.com")
        // passwordField vacío intencionalmente
        composeRule.onNodeWithTag("registerButton").performClick()

        verify(exactly = 0) { onRegisterClicked(any(), any(), any()) }
    }

    @Test
    fun navigateLink_callsOnNavigateToLogin() {
        val onNavigateToLogin: () -> Unit = mockk(relaxed = true)
        val onRegisterClicked: (String, String, String) -> Unit = mockk(relaxed = true)

        composeRule.setContent {
            RegisterScreen(onNavigateToLogin = onNavigateToLogin, onRegisterClicked = onRegisterClicked)
        }

        // Si no añades testTag en el texto clicable, puedes buscar por el texto visible (ES): "Iniciar sesión"
        // composeRule.onNodeWithText("Iniciar sesión").performClick()
        composeRule.onNodeWithTag("navigateLoginLink").performClick()

        verify { onNavigateToLogin() }
    }
}