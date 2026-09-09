package br.com.ide.domain.usecase

import br.com.ide.domain.error.UserManagementError
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.policy.UserManagementPolicy
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class GetManageableUsersUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val userManagementPolicy: UserManagementPolicy
) {

    suspend operator fun invoke(
        manager: UserProfile
    ): Result<List<UserProfile>> {

        if (
            !userManagementPolicy.canAccessUserManagement(
                manager
            )
        ) {
            return Result.failure(
                UserManagementError.CannotManageUser
            )
        }

        val usersResult =
            when (manager.role) {

                UserRole.ADMIN -> {
                    userRepository.getUsers()
                }

                UserRole.PASTOR -> {

                    val districtId =
                        manager.districtId

                    if (districtId.isNullOrBlank()) {
                        return Result.failure(
                            UserManagementError.PastorWithoutDistrict
                        )
                    }

                    userRepository.getUsersByDistrict(
                        districtId
                    )
                }

                UserRole.LEADER,
                UserRole.MISSIONARY -> {
                    return Result.failure(
                        UserManagementError.CannotManageUser
                    )
                }
            }

        return usersResult.map { users ->

            users.filter { target ->

                userManagementPolicy.canManageUser(
                    manager = manager,
                    target = target
                )
            }
        }
    }
}