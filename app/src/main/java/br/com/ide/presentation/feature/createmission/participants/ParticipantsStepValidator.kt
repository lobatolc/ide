package br.com.ide.presentation.feature.createmission.participants

import br.com.ide.R
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

data class ParticipantsStepValidationResult(
    val participantsError: Int? = null
) {

    val isValid: Boolean
        get() =
            participantsError == null
}

class ParticipantsStepValidator @Inject constructor() {

    fun validate(
        state: CreateMissionUiState
    ): ParticipantsStepValidationResult {

        val participantsError =
            if (
                state.selectedChurchIds
                    .isEmpty()
            ) {
                R.string
                    .create_mission_participants_required
            } else {
                null
            }

        return ParticipantsStepValidationResult(
            participantsError =
                participantsError
        )
    }
}