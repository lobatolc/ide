package br.com.ide.presentation.feature.profile

import androidx.annotation.StringRes
import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.domain.model.UserRole

data class ProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val sabbathSchoolClass: SabbathSchoolClass? = null,
    val role: UserRole = UserRole.MISSIONARY,
    val photoUrl: String? = null,

    @StringRes
    val firstNameError: Int? = null,

    @StringRes
    val lastNameError: Int? = null,

    @StringRes
    val errorMessage: Int? = null,

    @StringRes
    val successMessage: Int? = null,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false
)