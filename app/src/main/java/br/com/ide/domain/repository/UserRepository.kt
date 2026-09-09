package br.com.ide.domain.repository

import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole

interface UserRepository {

    suspend fun createUser(
        user: UserProfile
    ): Result<Unit>

    suspend fun updateUserProfile(
        user: UserProfile
    ): Result<Unit>

    suspend fun getUserById(
        userId: String
    ): Result<UserProfile>

    suspend fun userExists(
        userId: String
    ): Result<Boolean>

    suspend fun getUsers():
            Result<List<UserProfile>>

    suspend fun getUsersByDistrict(
        districtId: String
    ): Result<List<UserProfile>>

    suspend fun getUsersByChurch(
        churchId: String
    ): Result<List<UserProfile>>

    suspend fun updateUserAssignment(
        userId: String,
        role: UserRole,
        districtId: String?,
        churchId: String?
    ): Result<Unit>
}