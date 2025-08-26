package com.dls.pymetask.presentation.ajustes

import com.dls.pymetask.data.preferences.ThemeMode
import com.dls.pymetask.data.preferences.ThemePreferences
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    // Un único flujo fake compartido por los tests
    private val fakeFlow: MutableStateFlow<ThemeMode> = MutableStateFlow(ThemeMode.SYSTEM)

    @Test
    fun `estado inicial refleja el flow de preferencias y reacciona a cambios`() = runTest {
        val prefs = mockk<ThemePreferences>(relaxed = true)
        every { prefs.themeFlow } returns fakeFlow

        val vm = ThemeViewModel(prefs)

        assertEquals(ThemeMode.SYSTEM, vm.themeMode.value)

        // Evitar inferencia ambigua: tipamos el parámetro
        fakeFlow.update { _: ThemeMode -> ThemeMode.DARK }
        assertEquals(ThemeMode.DARK, vm.themeMode.value)
    }

    @Test
    fun `setTheme delega en saveThemeMode`() = runTest {
        val prefs = mockk<ThemePreferences>(relaxed = true)
        every { prefs.themeFlow } returns fakeFlow

        val vm = ThemeViewModel(prefs)

        vm.setTheme(ThemeMode.LIGHT)

        coVerify { prefs.saveThemeMode(ThemeMode.LIGHT) }
    }
}

/** Regla para usar TestDispatcher como Dispatchers.Main */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
