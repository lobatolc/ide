package br.com.ide.domain.usecase

import br.com.ide.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<Unit> {

        if (email.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Informe o e-mail.")
            )
        }

        if (password.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Informe a senha.")
            )
        }

        return authRepository.login(
            email = email.trim(),
            password = password
        )
    }
}