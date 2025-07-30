package com.dls.pymetask.data.repository



import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.dls.pymetask.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.core.content.edit

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun register(email: String, password: String, nombre: String, fotoUrl: String?): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID no disponible"))

            val userMap = mapOf(
                "nombre" to nombre,
                "fotoUrl" to fotoUrl
            )

            firestore.collection("usuarios")
                .document(uid)
                .set(userMap)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @SuppressLint("UseKtx")
    override fun logout(context: Context) {
        auth.signOut()

        // Eliminar flag local
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit {
                remove("sesion_activa")
            }
    }


    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
    override fun isUserLoggedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("AuthCheck", "Usuario actual: ${currentUser?.email ?: "NULO"}")
        return currentUser != null
    }

    // samsumg s24++ y similares
    override fun marcarSesionActiva(context: Context) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit {
                putBoolean("sesion_activa", true)
            }
    }

    override fun sesionMarcada(context: Context): Boolean {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getBoolean("sesion_activa", false)
    }

}
