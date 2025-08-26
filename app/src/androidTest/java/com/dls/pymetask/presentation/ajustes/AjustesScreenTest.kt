package com.dls.pymetask.presentation.ajustes

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dls.pymetask.R
import com.dls.pymetask.data.preferences.ThemeMode
import com.dls.pymetask.data.preferences.ThemePreferences
import com.dls.pymetask.main.MainActivity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AjustesScreenTest {



    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>() // <-- una actividad con @AndroidEntryPoint

    @Before
    fun init() {
        composeRule.activity.setContent {
            AjustesScreen(navController = mockk(relaxed = true))
        }
    }

    private fun s(id: Int) = composeRule.activity.getString(id)

    private fun fakeTextScaleViewModel(): TextScaleViewModel {
        val flow = MutableStateFlow(TextScale.LARGE)
        return mockk<TextScaleViewModel>(relaxed = true).apply {
            every { textScale } returns flow
            coEvery { setTextScale(any()) } coAnswers {
                flow.value = it.invocation.args[0] as TextScale
            }
        }
    }

    @Test
    fun resetPreferencias_temaSystem_idiomaSystem_fuenteMedium() {
        val themeFlow = MutableStateFlow(ThemeMode.DARK)
        val themePrefs = mockk<ThemePreferences>(relaxed = true)
        every { themePrefs.themeFlow } returns themeFlow
        coEvery { themePrefs.saveThemeMode(any()) } coAnswers { themeFlow.value = it.invocation.args[0] as ThemeMode }
        val themeVm = ThemeViewModel(themePrefs)

        val langFlow = MutableStateFlow("en")
        val langPrefs = mockk<com.dls.pymetask.data.preferences.LanguagePreferences>(relaxed = true)
        every { langPrefs.languageFlow } returns langFlow
        coEvery { langPrefs.saveLanguage(any()) } answers { langFlow.value = it.invocation.args[0] as String }
        val languageVm = LanguageViewModel(langPrefs)

        val textScaleVm = fakeTextScaleViewModel()
        val nav = mockk<NavController>(relaxed = true)

        composeRule.setContent {
            AjustesScreen(
                navController = nav,
                viewModel = themeVm,
                languageViewModel = languageVm,
                textScaleViewModel = textScaleVm
            )
        }

        composeRule.onNodeWithText(s(R.string.settings_reset_prefs), useUnmergedTree = true).performClick()

        composeRule.waitUntil(3_000) {
            themeFlow.value == ThemeMode.SYSTEM &&
                    langFlow.value == "system" &&
                    textScaleVm.textScale.value == TextScale.MEDIUM
        }
    }
}
