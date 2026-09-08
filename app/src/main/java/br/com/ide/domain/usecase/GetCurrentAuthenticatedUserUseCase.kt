package br.com.ide.domain.usecase

import br.com.ide.domain.model.GoogleUser
import br.com.ide.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentAuthenticatedUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    operator fun invoke(): GoogleUser? {
        return authRepository
            .getCurrentAuthenticatedUser()
    }
}