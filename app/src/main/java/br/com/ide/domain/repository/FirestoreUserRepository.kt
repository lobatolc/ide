package br.com.ide.data.repository

import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole
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

    override suspend fun getUserById(
        userId: String
    ): Result<UserProfile> {
        return try {

            val document = firestore
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (!document.exists()) {
                return Result.failure(
                    IllegalStateException(
                        "User profile not found"
                    )
                )
            }

            val sabbathSchoolClass =
                document
                    .getString("sabbathSchoolClass")
                    ?.let {
                        runCatching {
                            SabbathSchoolClass.valueOf(it)
                        }.getOrNull()
                    }
                    ?: SabbathSchoolClass.ADULTOS

            val role =
                document
                    .getString("role")
                    ?.let {
                        runCatching {
                            UserRole.valueOf(it)
                        }.getOrNull()
                    }
                    ?: UserRole.MISSIONARY

            val user = UserProfile(
                id = document.id,
                firstName =
                    document.getString("firstName").orEmpty(),
                lastName =
                    document.getString("lastName").orEmpty(),
                email =
                    document.getString("email").orEmpty(),
                sabbathSchoolClass =
                    sabbathSchoolClass,
                role = role
            )

            Result.success(user)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun userExists(
        userId: String
    ): Result<Boolean> {
        return try {

            val document = firestore
                .collection("users")
                .document(userId)
                .get()
                .await()

            Result.success(
                document.exists()
            )

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}