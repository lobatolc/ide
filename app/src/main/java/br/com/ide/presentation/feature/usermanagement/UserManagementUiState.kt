package br.com.ide.presentation.feature.usermanagement

import androidx.annotation.StringRes
import br.com.ide.domain.model.Church
import br.com.ide.domain.model.District
import br.com.ide.domain.model.UserProfile

data class UserManagementUiState(
    val manager: UserProfile? = null,

    val users: List<UserProfile> = emptyList(),
    val filteredUsers: List<UserProfile> = emptyList(),

    val districts: List<District> = emptyList(),
    val churches: List<Church> = emptyList(),

    val searchQuery: String = "",

    val selectedDistrictId: String? = null,
    val selectedChurchId: String? = null,

    val isLoading: Boolean = true,

    @StringRes
    val errorMessage: Int? = null
)