package br.com.ide.presentation.feature.login

sealed interface LoginEvent {

    data class EmailChanged(
        val email: String
    ) : LoginEvent

    data class PasswordChanged(
        val password: String
    ) : LoginEvent

    data class GoogleLogin(
        val idToken: String
    ) : LoginEvent

    data object GoogleLoginError : LoginEvent

    data object Login : LoginEvent
}