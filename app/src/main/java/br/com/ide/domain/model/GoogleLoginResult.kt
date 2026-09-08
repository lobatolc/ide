package br.com.ide.domain.model

sealed interface GoogleLoginResult {

    data object ExistingUser : GoogleLoginResult

    data class NewUser(
        val user: GoogleUser
    ) : GoogleLoginResult
}