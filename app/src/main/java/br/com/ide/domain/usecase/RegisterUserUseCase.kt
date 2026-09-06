package br.com.ide.domain.usecase

import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.repository.AuthRepository
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        sabbathSchoolClass: SabbathSchoolClass,
        password: String
    ): Result<Unit> {

        val authResult = authRepository.register(
            email = email,
            password = password
        )

        val uid = authResult.getOrElse { exception ->
            return Result.failure(exception)
        }

        val user = UserProfile(
            id = uid,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
            sabbathSchoolClass = sabbathSchoolClass,
            role = UserRole.MISSIONARY
        )

        return userRepository.saveUser(user)
    }
}