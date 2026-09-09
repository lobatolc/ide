package br.com.ide.domain.usecase

import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke():
            Result<List<UserProfile>> {

        return userRepository.getUsers()
    }
}