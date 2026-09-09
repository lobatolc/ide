package br.com.ide.domain.usecase

import br.com.ide.domain.model.GoogleUser
import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class CompleteGoogleRegistrationUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        googleUser: GoogleUser,
        firstName: String,
        lastName: String,
        sabbathSchoolClass: SabbathSchoolClass
    ): Result<Unit> {

        val user = UserProfile(
            id = googleUser.id,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = googleUser.email.trim(),
            sabbathSchoolClass = sabbathSchoolClass,
            role = UserRole.MISSIONARY
        )

        return userRepository.createUser(user)
    }
}