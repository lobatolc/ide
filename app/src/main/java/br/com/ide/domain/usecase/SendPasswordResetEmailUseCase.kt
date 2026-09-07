package br.com.ide.domain.usecase

import br.com.ide.domain.repository.AuthRepository
import javax.inject.Inject

class SendPasswordResetEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String
    ): Result<Unit> {
        return authRepository.sendPasswordResetEmail(
            email = email
        )
    }
}