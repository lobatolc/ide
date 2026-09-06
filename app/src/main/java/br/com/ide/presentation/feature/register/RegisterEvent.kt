package br.com.ide.presentation.feature.register

import br.com.ide.domain.model.SabbathSchoolClass

sealed interface RegisterEvent {

    data class FirstNameChanged(
        val firstName: String
    ) : RegisterEvent

    data class LastNameChanged(
        val lastName: String
    ) : RegisterEvent

    data class EmailChanged(
        val email: String
    ) : RegisterEvent

    data class SabbathSchoolClassChanged(
        val sabbathSchoolClass: SabbathSchoolClass
    ) : RegisterEvent

    data class PasswordChanged(
        val password: String
    ) : RegisterEvent

    data class ConfirmPasswordChanged(
        val confirmPassword: String
    ) : RegisterEvent

    data object Register : RegisterEvent
}