package br.com.ide.domain.policy

import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole
import javax.inject.Inject

class UserManagementPolicy @Inject constructor() {

    fun canAccessUserManagement(
        manager: UserProfile
    ): Boolean {
        return manager.role == UserRole.PASTOR ||
                manager.role == UserRole.ADMIN
    }

    fun canManageUser(
        manager: UserProfile,
        target: UserProfile
    ): Boolean {

        if (manager.id == target.id) {
            return false
        }

        return when (manager.role) {

            UserRole.ADMIN -> {
                true
            }

            UserRole.PASTOR -> {
                canPastorManageTarget(
                    manager = manager,
                    target = target
                )
            }

            UserRole.LEADER,
            UserRole.MISSIONARY -> {
                false
            }
        }
    }

    fun canAssignRole(
        manager: UserProfile,
        target: UserProfile,
        newRole: UserRole
    ): Boolean {

        if (!canManageUser(manager, target)) {
            return false
        }

        return when (manager.role) {

            UserRole.ADMIN -> {
                newRole != UserRole.ADMIN
            }

            UserRole.PASTOR -> {
                newRole == UserRole.MISSIONARY ||
                        newRole == UserRole.LEADER
            }

            UserRole.LEADER,
            UserRole.MISSIONARY -> {
                false
            }
        }
    }

    fun canChangeDistrict(
        manager: UserProfile,
        target: UserProfile
    ): Boolean {

        if (!canManageUser(manager, target)) {
            return false
        }

        return manager.role == UserRole.ADMIN
    }

    fun canChangeChurch(
        manager: UserProfile,
        target: UserProfile,
        targetDistrictId: String?
    ): Boolean {

        if (!canManageUser(manager, target)) {
            return false
        }

        return when (manager.role) {

            UserRole.ADMIN -> {
                true
            }

            UserRole.PASTOR -> {
                manager.districtId != null &&
                        manager.districtId == targetDistrictId
            }

            UserRole.LEADER,
            UserRole.MISSIONARY -> {
                false
            }
        }
    }

    fun getAssignableRoles(
        manager: UserProfile,
        target: UserProfile
    ): List<UserRole> {

        if (!canManageUser(manager, target)) {
            return emptyList()
        }

        return when (manager.role) {

            UserRole.ADMIN -> {
                listOf(
                    UserRole.MISSIONARY,
                    UserRole.LEADER,
                    UserRole.PASTOR
                )
            }

            UserRole.PASTOR -> {
                listOf(
                    UserRole.MISSIONARY,
                    UserRole.LEADER
                )
            }

            UserRole.LEADER,
            UserRole.MISSIONARY -> {
                emptyList()
            }
        }
    }

    private fun canPastorManageTarget(
        manager: UserProfile,
        target: UserProfile
    ): Boolean {

        val managerDistrictId =
            manager.districtId
                ?: return false

        if (
            target.districtId !=
            managerDistrictId
        ) {
            return false
        }

        return when (target.role) {

            UserRole.MISSIONARY,
            UserRole.LEADER -> {
                true
            }

            UserRole.PASTOR,
            UserRole.ADMIN -> {
                false
            }
        }
    }
}