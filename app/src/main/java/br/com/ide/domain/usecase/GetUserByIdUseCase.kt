package br.com.ide.domain.usecase

import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val userRepository:
    UserRepository
) {

    suspend operator fun invoke(
        userId: String
    ): Result<UserProfile> {
        return userRepository
            .getUserById(userId)
    }
}