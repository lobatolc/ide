package br.com.ide.presentation.feature.editprofile

import androidx.annotation.StringRes
import br.com.ide.domain.model.SabbathSchoolClass

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val sabbathSchoolClass: SabbathSchoolClass? = null,

    @StringRes
    val firstNameError: Int? = null,

    @StringRes
    val lastNameError: Int? = null,

    @StringRes
    val sabbathSchoolClassError: Int? = null,

    @StringRes
    val errorMessage: Int? = null,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)