package com.dls.pymetask.presentation.auth


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.Intents.intended
import com.dls.pymetask.main.MainActivity
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Tests instrumentados para RedirectActivity
 *
 * Verifican que:
 * 1) Si llega un deep link (data != null), redirige a MainActivity y se cierra.
 * 2) Si no llega data (null), también redirige a MainActivity y se cierra (fail-safe).
 *
 * Requisitos de dependencias (module: app):
 *   androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.0")
 *   androidTestImplementation("androidx.test.ext:junit:1.1.5")
 *   androidTestImplementation("androidx.test:core:1.5.0")
 */
@RunWith(AndroidJUnit4::class)
class RedirectActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        // Inicializa el framework de Intents para poder verificar la navegación a MainActivity
        Intents.init()
    }

    @After
    fun tearDown() {
        // Libera los recursos de Espresso-Intents
        Intents.release()
    }

    @Test
    fun whenUriPresent_redirectsToMain_and_finishes() {
        val intent = Intent(context, RedirectActivity::class.java).apply {
            data = Uri.parse("pymetask://auth/callback?code=abc123")
        }

        ActivityScenario.launch<RedirectActivity>(intent).use { scenario ->
            // Verifica que se lanza MainActivity
            intended(allOf(hasComponent(MainActivity::class.java.name)))

            // Verifica que RedirectActivity se cierra (finishing o destroyed)
            scenario.onActivity { activity ->
                assertTrue(activity.isFinishing || activity.isDestroyed)
            }
        }
    }

    @Test
    fun whenUriNull_still_redirectsToMain_and_finishes() {
        val intent = Intent(context, RedirectActivity::class.java) // sin data

        ActivityScenario.launch<RedirectActivity>(intent).use { scenario ->
            // Verifica que se lanza MainActivity también en ausencia de data
            intended(allOf(hasComponent(MainActivity::class.java.name)))

            // Verifica cierre de RedirectActivity
            scenario.onActivity { activity ->
                assertTrue(activity.isFinishing || activity.isDestroyed)
            }
        }
    }
}
