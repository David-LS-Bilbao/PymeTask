@file:Suppress("DEPRECATION")

package com.dls.pymetask.data.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.dls.pymetask.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(): SignInResult {
        val result = try {
            oneTapClient.beginSignIn(
                BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(context.getString(R.string.default_web_client_id))
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .build()
            ).await()
        } catch (e: Exception) {
            return SignInResult(errorMessage = e.message)
        }

        val intent = result.pendingIntent.intentSender
        return SignInResult(intentSender = intent)
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val token = credential.googleIdToken ?: return SignInResult(errorMessage = "Token nulo")

        val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
        return try {
            auth.signInWithCredential(firebaseCredential).await()
            SignInResult(user = auth.currentUser)
        } catch (e: Exception) {
            SignInResult(errorMessage = e.message)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}

data class SignInResult(
    val user: FirebaseUser? = null,
    val errorMessage: String? = null,
    val intentSender: IntentSender? = null
)
