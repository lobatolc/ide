package br.com.ide.data.repository

import br.com.ide.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {

        return try {

            firebaseAuth
                .signInWithEmailAndPassword(
                    email,
                    password
                )
                .await()

            Result.success(Unit)

        } catch (exception: Exception) {

            Result.failure(exception)
        }
    }

    override suspend fun loginWithGoogle(
        idToken: String
    ): Result<Unit> {

        return try {

            val credential =
                GoogleAuthProvider.getCredential(
                    idToken,
                    null
                )

            firebaseAuth
                .signInWithCredential(credential)
                .await()

            Result.success(Unit)

        } catch (exception: Exception) {

            Result.failure(exception)
        }
    }

    override suspend fun register(
        email: String,
        password: String
    ): Result<String> {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(
                    email,
                    password
                )
                .await()

            val uid = result.user?.uid
                ?: return Result.failure(
                    IllegalStateException("User UID not found")
                )

            Result.success(uid)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun sendPasswordResetEmail(
        email: String
    ): Result<Unit> {
        return try {
            firebaseAuth.useAppLanguage()

            firebaseAuth
                .sendPasswordResetEmail(email)
                .await()

            Result.success(Unit)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}