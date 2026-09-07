package br.com.ide.presentation.feature.forgotpassword

sealed interface ForgotPasswordEvent {

    data class EmailChanged(
        val email: String
    ) : ForgotPasswordEvent

    data object SendResetEmail : ForgotPasswordEvent
}