package br.com.ide.domain.usecase

import br.com.ide.domain.model.GoogleLoginResult
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class HandleGoogleLoginUseCase @Inject constructor(
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(
        idToken: String
    ): Result<GoogleLoginResult> {

        return loginWithGoogleUseCase(idToken)
            .fold(
                onSuccess = { googleUser ->

                    userRepository
                        .userExists(googleUser.id)
                        .map { exists ->

                            if (exists) {
                                GoogleLoginResult.ExistingUser
                            } else {
                                GoogleLoginResult.NewUser(
                                    user = googleUser
                                )
                            }
                        }
                },

                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
    }
}