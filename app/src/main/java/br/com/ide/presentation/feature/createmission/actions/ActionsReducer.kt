package br.com.ide.presentation.feature.createmission.actions

import br.com.ide.domain.model.MissionActivityType
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

class ActionsReducer @Inject constructor() {

    fun toggleActivity(
        state: CreateMissionUiState,
        activity: MissionActivityType
    ): CreateMissionUiState {

        val updatedActivities =
            state.selectedActivities
                .toMutableSet()

        if (
            activity in updatedActivities
        ) {
            updatedActivities.remove(
                activity
            )
        } else {
            updatedActivities.add(
                activity
            )
        }

        val customActivityName =
            if (
                MissionActivityType.OTHER in
                updatedActivities
            ) {
                state.customActivityName
            } else {
                ""
            }

        return state.copy(
            selectedActivities =
                updatedActivities,

            customActivityName =
                customActivityName,

            activitiesError =
                null,

            customActivityNameError =
                null
        )
    }

    fun updateCustomActivityName(
        state: CreateMissionUiState,
        value: String
    ): CreateMissionUiState {

        return state.copy(
            customActivityName =
                value,

            customActivityNameError =
                null,

            activitiesError =
                null
        )
    }
}