package br.com.ide.presentation.feature.register

import androidx.annotation.StringRes
import br.com.ide.domain.model.SabbathSchoolClass

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val sabbathSchoolClass: SabbathSchoolClass? = null,
    val password: String = "",
    val confirmPassword: String = "",

    @StringRes
    val firstNameError: Int? = null,

    @StringRes
    val lastNameError: Int? = null,

    @StringRes
    val emailError: Int? = null,

    @StringRes
    val sabbathSchoolClassError: Int? = null,

    @StringRes
    val passwordError: Int? = null,

    @StringRes
    val confirmPasswordError: Int? = null,

    @StringRes
    val errorMessage: Int? = null,

    val isLoading: Boolean = false,
    val isRegistered: Boolean = false
)