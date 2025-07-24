package com.dls.pymetask.data.repository



import com.dls.pymetask.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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


//    override suspend fun register(email: String, password: String, nombre: String, fotoUrl: String?): Result<Unit> {
//        return try {
//            auth.createUserWithEmailAndPassword(email, password).await()
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
