package br.com.ide.presentation.feature.createmission

import javax.inject.Inject

class CreateMissionNavigationReducer @Inject constructor() {

    fun goToStepFromSummary(
        state: CreateMissionUiState,
        step: Int
    ): CreateMissionUiState {

        if (
            step !in 1..3
        ) {
            return state
        }

        return state.copy(
            currentStep = step,
            editingFromSummary = true
        )
    }

    fun goToParticipants(
        state: CreateMissionUiState
    ): CreateMissionUiState {

        return state.copy(
            currentStep = 2,
            editingFromSummary = false
        )
    }

    fun goToActions(
        state: CreateMissionUiState
    ): CreateMissionUiState {

        return state.copy(
            currentStep = 3,
            editingFromSummary = false,
            participantsError = null
        )
    }

    fun goToSummary(
        state: CreateMissionUiState
    ): CreateMissionUiState {

        return state.copy(
            currentStep = 4,
            editingFromSummary = false
        )
    }

    fun previousStep(
        state: CreateMissionUiState
    ): CreateMissionUiState {

        if (
            state.editingFromSummary
        ) {

            return goToSummary(
                state
            )
                .clearNavigationErrors()
        }

        return state.copy(
            currentStep =
                (state.currentStep - 1)
                    .coerceAtLeast(1),

            editingFromSummary = false
        )
            .clearNavigationErrors()
    }

    private fun CreateMissionUiState
            .clearNavigationErrors():
            CreateMissionUiState {

        return copy(
            participantsError = null,
            activitiesError = null,
            customActivityNameError = null,
            materialsError = null,
            customMaterialNameError = null,
            surveyError = null
        )
    }
}