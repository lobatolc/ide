package br.com.ide.presentation.feature.completeregistration

import androidx.annotation.StringRes
import br.com.ide.domain.model.GoogleUser
import br.com.ide.domain.model.SabbathSchoolClass

data class CompleteRegistrationUiState(
    val googleUser: GoogleUser? = null,

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
    val isCompleted: Boolean = false
)