package com.dls.pymetask.data.auth

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.tasks.Tasks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoogleAuthUiClientTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun newClient(oneTap: SignInClient) = GoogleAuthUiClient(
        context = context,
        oneTapClient = oneTap
    )

    @Test
    fun signIn_success_returnsIntentSender() = runBlocking {
        val oneTap = mockk<SignInClient>()
        // Mock de BeginSignInResult con un PendingIntent real
        val flags = if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
        val pi = PendingIntent.getActivity(context, 0, Intent(context, javaClass), flags)
        val beginResult = mockk<BeginSignInResult> {
            every { pendingIntent } returns pi
        }
        every { oneTap.beginSignIn(any()) } returns Tasks.forResult(beginResult)

        val client = newClient(oneTap)
        val result = client.signIn()

        Assert.assertNotNull(result.intentSender)
        Assert.assertEquals(null, result.errorMessage)
    }

    @Test
    fun signIn_failure_returnsErrorMessage() = runBlocking {
        val oneTap = mockk<SignInClient>()
        every { oneTap.beginSignIn(any()) } returns Tasks.forException(IllegalStateException("boom"))

        val client = newClient(oneTap)
        val result = client.signIn()

        Assert.assertEquals("boom", result.errorMessage)
        Assert.assertEquals(null, result.intentSender)
    }

    @Test
    fun signInWithIntent_tokenNull_returnsTokenNulo() = runBlocking {
        val oneTap = mockk<SignInClient>()
        val intent = Intent("test")

        // Credential sin token
        val credential = mockk<SignInCredential> {
            every { googleIdToken } returns null
        }
        every { oneTap.getSignInCredentialFromIntent(intent) } returns credential

        val client = newClient(oneTap)
        val result = client.signInWithIntent(intent)

        Assert.assertEquals("Token nulo", result.errorMessage)
        Assert.assertEquals(null, result.user)
    }
}