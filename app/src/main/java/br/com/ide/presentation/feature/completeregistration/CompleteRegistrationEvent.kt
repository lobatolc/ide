package br.com.ide.presentation.feature.completeregistration

import br.com.ide.domain.model.SabbathSchoolClass

sealed interface CompleteRegistrationEvent {

    data class FirstNameChanged(
        val firstName: String
    ) : CompleteRegistrationEvent

    data class LastNameChanged(
        val lastName: String
    ) : CompleteRegistrationEvent

    data class SabbathSchoolClassChanged(
        val sabbathSchoolClass: SabbathSchoolClass
    ) : CompleteRegistrationEvent

    data object CompleteRegistration :
        CompleteRegistrationEvent
}