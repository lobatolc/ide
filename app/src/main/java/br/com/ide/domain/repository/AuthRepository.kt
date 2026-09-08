package br.com.ide.domain.repository

import br.com.ide.domain.model.GoogleUser

interface AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit>

    suspend fun loginWithGoogle(
        idToken: String
    ): Result<GoogleUser>

    fun getCurrentAuthenticatedUser(): GoogleUser?

    suspend fun register(
        email: String,
        password: String
    ): Result<String>

    suspend fun sendPasswordResetEmail(
        email: String
    ): Result<Unit>

    fun isUserLoggedIn(): Boolean

    fun logout()

    fun getCurrentUserId(): String?
}