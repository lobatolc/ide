package br.com.ide.domain.usecase

import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.repository.AuthRepository
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class GetCurrentUserProfileUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke():
            Result<UserProfile> {

        val userId =
            authRepository.getCurrentUserId()
                ?: return Result.failure(
                    IllegalStateException(
                        "User not authenticated"
                    )
                )

        return userRepository.getUserById(
            userId
        )
    }
}