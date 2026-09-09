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

    override suspend fun createUser(
        user: UserProfile
    ): Result<Unit> {

        return try {

            val data = hashMapOf(
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "email" to user.email,
                "sabbathSchoolClass" to
                        user.sabbathSchoolClass.name,
                "role" to
                        user.role.name,
                "churchId" to
                        user.churchId,
                "districtId" to
                        user.districtId,
                "createdAt" to
                        FieldValue.serverTimestamp()
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

    override suspend fun updateUserProfile(
        user: UserProfile
    ): Result<Unit> {

        return try {

            val updates = mapOf(
                "firstName" to user.firstName,
                "lastName" to user.lastName,
                "sabbathSchoolClass" to
                        user.sabbathSchoolClass.name
            )

            firestore
                .collection("users")
                .document(user.id)
                .update(updates)
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

            val document =
                firestore
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

            Result.success(
                document.toUserProfile()
            )

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun userExists(
        userId: String
    ): Result<Boolean> {

        return try {

            val document =
                firestore
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

    override suspend fun getUsersByDistrict(
        districtId: String
    ): Result<List<UserProfile>> {

        return try {

            val documents =
                firestore
                    .collection("users")
                    .whereEqualTo(
                        "districtId",
                        districtId
                    )
                    .get()
                    .await()
                    .documents

            val users =
                documents
                    .map {
                        it.toUserProfile()
                    }
                    .sortedWith(
                        compareBy(
                            UserProfile::firstName,
                            UserProfile::lastName
                        )
                    )

            Result.success(users)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun getUsersByChurch(
        churchId: String
    ): Result<List<UserProfile>> {

        return try {

            val documents =
                firestore
                    .collection("users")
                    .whereEqualTo(
                        "churchId",
                        churchId
                    )
                    .get()
                    .await()
                    .documents

            val users =
                documents
                    .map {
                        it.toUserProfile()
                    }
                    .sortedWith(
                        compareBy(
                            UserProfile::firstName,
                            UserProfile::lastName
                        )
                    )

            Result.success(users)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun updateUserAssignment(
        userId: String,
        role: UserRole,
        districtId: String?,
        churchId: String?
    ): Result<Unit> {

        return try {

            val updates = mapOf(
                "role" to role.name,
                "districtId" to districtId,
                "churchId" to churchId
            )

            firestore
                .collection("users")
                .document(userId)
                .update(updates)
                .await()

            Result.success(Unit)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun
            com.google.firebase.firestore.DocumentSnapshot
            .toUserProfile(): UserProfile {

        val sabbathSchoolClass =
            getString("sabbathSchoolClass")
                ?.let {
                    runCatching {
                        SabbathSchoolClass
                            .valueOf(it)
                    }.getOrNull()
                }
                ?: SabbathSchoolClass.ADULTOS

        val role =
            getString("role")
                ?.let {
                    runCatching {
                        UserRole.valueOf(it)
                    }.getOrNull()
                }
                ?: UserRole.MISSIONARY

        return UserProfile(
            id = id,

            firstName =
                getString(
                    "firstName"
                ).orEmpty(),

            lastName =
                getString(
                    "lastName"
                ).orEmpty(),

            email =
                getString(
                    "email"
                ).orEmpty(),

            sabbathSchoolClass =
                sabbathSchoolClass,

            role =
                role,

            churchId =
                getString(
                    "churchId"
                ),

            districtId =
                getString(
                    "districtId"
                )
        )
    }

    override suspend fun getUsers():
            Result<List<UserProfile>> {

        return try {

            val documents =
                firestore
                    .collection("users")
                    .get()
                    .await()
                    .documents

            val users =
                documents
                    .map {
                        it.toUserProfile()
                    }
                    .sortedWith(
                        compareBy(
                            UserProfile::firstName,
                            UserProfile::lastName
                        )
                    )

            Result.success(users)

        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}