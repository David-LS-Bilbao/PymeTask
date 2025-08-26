package com.dls.pymetask.presentation.ajustes


import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dls.pymetask.data.preferences.CurrencyOption
import com.dls.pymetask.data.preferences.DateFormatOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayPrefsViewModelInstrumentedTest {

    @Test
    fun actualizarPreferencias_seReflejaEnStateFlows() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = DisplayPrefsViewModel(app)

        // Date format
        vm.setDateFormat(DateFormatOption.RELATIVE)
        withTimeout(3_000) {
            while (vm.dateFormat.value != DateFormatOption.RELATIVE) delay(50)
        }
        assertEquals(DateFormatOption.RELATIVE, vm.dateFormat.value)

        // Currency
        vm.setCurrency(CurrencyOption.USD)
        withTimeout(3_000) {
            while (vm.currency.value != CurrencyOption.USD) delay(50)
        }
        assertEquals(CurrencyOption.USD, vm.currency.value)

        // Reminder default
        vm.setReminderDefault(true)
        withTimeout(3_000) {
            while (vm.reminderDefault.value != true) delay(50)
        }
        assertEquals(true, vm.reminderDefault.value)
    }
}
