package br.com.ide.presentation.feature.usermanagement

sealed interface UserManagementEvent {

    data class SearchChanged(
        val value: String
    ) : UserManagementEvent

    data class DistrictSelected(
        val districtId: String?
    ) : UserManagementEvent

    data class ChurchSelected(
        val churchId: String?
    ) : UserManagementEvent

    data object Refresh :
        UserManagementEvent
}