package br.com.ide.data.repository

import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun saveUser(
        user: UserProfile
    ): Result<Unit> {

        return try {

            val data = hashMapOf(
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "email" to user.email,
                "sabbathSchoolClass" to user.sabbathSchoolClass.name,
                "role" to user.role.name,
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore
                .collection("users")
                .document(user.id)
                .set(data)
                .await()

            Result.success(Unit)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}