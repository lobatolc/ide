package br.com.ide.presentation.feature.login

import androidx.annotation.StringRes
import br.com.ide.domain.model.GoogleUser

data class LoginUiState(
    val email: String = "",
    val password: String = "",

    @StringRes
    val emailError: Int? = null,

    @StringRes
    val passwordError: Int? = null,

    @StringRes
    val errorMessage: Int? = null,

    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val googleUserToComplete: GoogleUser? = null
)