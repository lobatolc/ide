package br.com.ide.domain.repository

import br.com.ide.domain.model.UserProfile

interface UserRepository {

    suspend fun saveUser(
        user: UserProfile
    ): Result<Unit>

    suspend fun getUserById(
        userId: String
    ): Result<UserProfile>

    suspend fun userExists(
        userId: String
    ): Result<Boolean>


}