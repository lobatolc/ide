package br.com.ide.domain.usecase

import br.com.ide.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        idToken: String
    ): Result<Unit> {
        return authRepository.loginWithGoogle(idToken)
    }
}