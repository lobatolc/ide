package br.com.ide.presentation.feature.usermanagementdetails

import androidx.annotation.StringRes
import br.com.ide.domain.model.Church
import br.com.ide.domain.model.District
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole

data class UserManagementDetailsUiState(
    val manager: UserProfile? = null,
    val target: UserProfile? = null,

    val roles: List<UserRole> = emptyList(),
    val districts: List<District> = emptyList(),
    val churches: List<Church> = emptyList(),

    val selectedRole: UserRole? = null,
    val selectedDistrictId: String? = null,
    val selectedChurchId: String? = null,

    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,

    @StringRes
    val errorMessage: Int? = null
)