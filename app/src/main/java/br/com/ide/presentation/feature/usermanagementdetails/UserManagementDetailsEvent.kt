package br.com.ide.presentation.feature.usermanagementdetails

import br.com.ide.domain.model.UserRole

sealed interface UserManagementDetailsEvent {

    data class RoleSelected(
        val role: UserRole
    ) : UserManagementDetailsEvent

    data class DistrictSelected(
        val districtId: String
    ) : UserManagementDetailsEvent

    data class ChurchSelected(
        val churchId: String
    ) : UserManagementDetailsEvent

    data object Save :
        UserManagementDetailsEvent
}