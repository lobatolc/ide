package br.com.ide.presentation.feature.editprofile

import br.com.ide.domain.model.SabbathSchoolClass

sealed interface EditProfileEvent {

    data class FirstNameChanged(
        val firstName: String
    ) : EditProfileEvent

    data class LastNameChanged(
        val lastName: String
    ) : EditProfileEvent

    data class SabbathSchoolClassChanged(
        val sabbathSchoolClass: SabbathSchoolClass
    ) : EditProfileEvent

    data object SaveChanges : EditProfileEvent
}