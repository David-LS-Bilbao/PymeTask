package com.dls.pymetask.presentation.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.dls.pymetask.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.jvm.java

/**
 * Activity mínima que recibe el deep link OAuth (pymetask://auth/callback?code=...)
 * Intercambia el code por tokens y vuelve a MainActivity.
 */
@AndroidEntryPoint
class RedirectActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri? = intent?.data
        if (uri == null) {
            finishToMain()
            return
        }

        lifecycleScope.launch {
            finishToMain()
        }
    }

    private fun finishToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        finish()
    }
}
