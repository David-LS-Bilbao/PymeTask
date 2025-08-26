// RedirectActivityTest.kt
package com.dls.pymetask.presentation.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dls.pymetask.main.MainActivity
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RedirectActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun whenUriPresent_redirectsToMain_and_finishes() {
        val intent = Intent(context, RedirectActivity::class.java).apply {
            data = Uri.parse("pymetask://auth/callback?code=abc123")
        }

        ActivityScenario.launch<RedirectActivity>(intent).use { scenario ->
            // Verifica que se lanza MainActivity
            androidx.test.espresso.intent.Intents.intended(allOf(hasComponent(MainActivity::class.java.name)))

            // Espera corta y comprueba que la Activity está destruida
            // (al ser una redirección rápida, su estado debe ser DESTROYED)
            assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        }
    }

    @Test
    fun whenUriNull_still_redirectsToMain_and_finishes() {
        val intent = Intent(context, RedirectActivity::class.java) // sin data

        ActivityScenario.launch<RedirectActivity>(intent).use { scenario ->
            androidx.test.espresso.intent.Intents.intended(allOf(hasComponent(MainActivity::class.java.name)))
            assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        }
    }
}
