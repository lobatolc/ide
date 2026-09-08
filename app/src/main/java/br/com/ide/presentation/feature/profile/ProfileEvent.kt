package br.com.ide.presentation.feature.profile

import br.com.ide.domain.model.SabbathSchoolClass

sealed interface ProfileEvent {

    data object Refresh : ProfileEvent

    data class ProfileUpdated(
        val firstName: String,
        val lastName: String,
        val sabbathSchoolClass: SabbathSchoolClass
    ) : ProfileEvent
}