package com.dls.pymetask.presentation.ajustes


import com.dls.pymetask.data.preferences.LanguagePreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanguageViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val fakeFlow = MutableStateFlow("es")

    @Test
    fun `estado inicial y cambios en flow se reflejan en languageCode`() = runTest {
        val prefs = mockk<LanguagePreferences>(relaxed = true)
        every { prefs.languageFlow } returns fakeFlow

        val vm = LanguageViewModel(prefs) // usa tu VM real
        assertEquals("es", vm.languageCode.value)

        // Evitar inferencia: tipamos el parámetro
        fakeFlow.update { _: String -> "fr" }
        assertEquals("fr", vm.languageCode.value)
    }

    @Test
    fun `setLanguage delega en preferences`() = runTest {
        val prefs = mockk<LanguagePreferences>(relaxed = true)
        every { prefs.languageFlow } returns fakeFlow

        val vm = LanguageViewModel(prefs)
        vm.setLanguage("en")

        coVerify { prefs.saveLanguage("en") }
    }
}


