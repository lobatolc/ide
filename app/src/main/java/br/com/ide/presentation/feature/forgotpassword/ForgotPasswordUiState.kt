package br.com.ide.presentation.feature.forgotpassword

import androidx.annotation.StringRes

data class ForgotPasswordUiState(
    val email: String = "",

    @StringRes
    val emailError: Int? = null,

    @StringRes
    val errorMessage: Int? = null,

    @StringRes
    val successMessage: Int? = null,

    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    val resendSecondsRemaining: Int = 0
)