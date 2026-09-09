package br.com.ide.domain.usecase

import br.com.ide.domain.error.UserManagementError
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.policy.UserManagementPolicy
import br.com.ide.domain.repository.ChurchRepository
import br.com.ide.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserAssignmentUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val churchRepository: ChurchRepository,
    private val userManagementPolicy: UserManagementPolicy
) {

    suspend operator fun invoke(
        manager: UserProfile,
        target: UserProfile,
        newRole: UserRole,
        newDistrictId: String?,
        newChurchId: String?
    ): Result<Unit> {

        if (
            !userManagementPolicy.canManageUser(
                manager = manager,
                target = target
            )
        ) {
            return Result.failure(
                UserManagementError.CannotManageUser
            )
        }

        if (
            !userManagementPolicy.canAssignRole(
                manager = manager,
                target = target,
                newRole = newRole
            )
        ) {
            return Result.failure(
                UserManagementError.CannotAssignRole
            )
        }

        val normalizedDistrictId =
            normalizeDistrictId(
                role = newRole,
                districtId = newDistrictId
            )

        val normalizedChurchId =
            normalizeChurchId(
                role = newRole,
                churchId = newChurchId
            )

        val assignmentError =
            validateAssignment(
                role = newRole,
                districtId = normalizedDistrictId,
                churchId = normalizedChurchId
            )

        if (assignmentError != null) {
            return Result.failure(
                assignmentError
            )
        }

        val permissionError =
            validateAssignmentPermissions(
                manager = manager,
                target = target,
                role = newRole,
                districtId = normalizedDistrictId,
                churchId = normalizedChurchId
            )

        if (permissionError != null) {
            return Result.failure(
                permissionError
            )
        }

        val churchDistrictError =
            validateChurchDistrictRelationship(
                role = newRole,
                districtId = normalizedDistrictId,
                churchId = normalizedChurchId
            )

        if (churchDistrictError != null) {
            return Result.failure(
                churchDistrictError
            )
        }

        return userRepository.updateUserAssignment(
            userId = target.id,
            role = newRole,
            districtId = normalizedDistrictId,
            churchId = normalizedChurchId
        )
    }

    private fun normalizeDistrictId(
        role: UserRole,
        districtId: String?
    ): String? {

        return when (role) {

            UserRole.ADMIN ->
                null

            UserRole.PASTOR,
            UserRole.LEADER,
            UserRole.MISSIONARY ->
                districtId
        }
    }

    private fun normalizeChurchId(
        role: UserRole,
        churchId: String?
    ): String? {

        return when (role) {

            UserRole.ADMIN,
            UserRole.PASTOR ->
                null

            UserRole.LEADER,
            UserRole.MISSIONARY ->
                churchId
        }
    }

    private fun validateAssignment(
        role: UserRole,
        districtId: String?,
        churchId: String?
    ): UserManagementError? {

        return when (role) {

            UserRole.MISSIONARY,
            UserRole.LEADER -> {

                when {

                    districtId.isNullOrBlank() ->
                        UserManagementError.DistrictRequired

                    churchId.isNullOrBlank() ->
                        UserManagementError.ChurchRequired

                    else ->
                        null
                }
            }

            UserRole.PASTOR -> {

                if (districtId.isNullOrBlank()) {
                    UserManagementError.DistrictRequired
                } else {
                    null
                }
            }

            UserRole.ADMIN ->
                null
        }
    }

    private fun validateAssignmentPermissions(
        manager: UserProfile,
        target: UserProfile,
        role: UserRole,
        districtId: String?,
        churchId: String?
    ): UserManagementError? {

        return when (manager.role) {

            UserRole.ADMIN -> {
                validateAdminAssignment(
                    role = role,
                    districtId = districtId,
                    churchId = churchId
                )
            }

            UserRole.PASTOR -> {
                validatePastorAssignment(
                    manager = manager,
                    target = target,
                    role = role,
                    districtId = districtId,
                    churchId = churchId
                )
            }

            UserRole.LEADER,
            UserRole.MISSIONARY -> {
                UserManagementError.CannotManageUser
            }
        }
    }

    private fun validatePastorAssignment(
        manager: UserProfile,
        target: UserProfile,
        role: UserRole,
        districtId: String?,
        churchId: String?
    ): UserManagementError? {

        val managerDistrictId =
            manager.districtId
                ?: return UserManagementError.PastorWithoutDistrict

        if (
            target.districtId !=
            managerDistrictId
        ) {
            return UserManagementError.CannotManageOtherDistrict
        }

        if (
            districtId !=
            managerDistrictId
        ) {
            return UserManagementError.CannotMoveToOtherDistrict
        }

        if (
            role != UserRole.MISSIONARY &&
            role != UserRole.LEADER
        ) {
            return UserManagementError.InvalidRoleForPastor
        }

        if (
            churchId.isNullOrBlank()
        ) {
            return UserManagementError.ChurchRequired
        }

        return null
    }

    private fun validateAdminAssignment(
        role: UserRole,
        districtId: String?,
        churchId: String?
    ): UserManagementError? {

        return when (role) {

            UserRole.ADMIN -> {
                UserManagementError.AdminRoleNotAssignable
            }

            UserRole.PASTOR -> {

                if (districtId.isNullOrBlank()) {
                    UserManagementError.DistrictRequired
                } else {
                    null
                }
            }

            UserRole.MISSIONARY,
            UserRole.LEADER -> {

                when {

                    districtId.isNullOrBlank() ->
                        UserManagementError.DistrictRequired

                    churchId.isNullOrBlank() ->
                        UserManagementError.ChurchRequired

                    else ->
                        null
                }
            }
        }
    }

    private suspend fun validateChurchDistrictRelationship(
        role: UserRole,
        districtId: String?,
        churchId: String?
    ): UserManagementError? {

        if (
            role != UserRole.MISSIONARY &&
            role != UserRole.LEADER
        ) {
            return null
        }

        if (
            districtId.isNullOrBlank() ||
            churchId.isNullOrBlank()
        ) {
            return null
        }

        val churchResult =
            churchRepository.getChurchById(
                churchId
            )

        if (churchResult.isFailure) {
            return UserManagementError
                .UnableToValidateChurch
        }

        val church =
            churchResult.getOrNull()
                ?: return UserManagementError
                    .UnableToValidateChurch

        if (
            church.districtId !=
            districtId
        ) {
            return UserManagementError
                .ChurchDoesNotBelongToDistrict
        }

        return null
    }
}