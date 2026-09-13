package br.com.ide.presentation.feature.createmission

import br.com.ide.domain.model.MissionActivityType
import javax.inject.Inject

class CreateMissionSubScreenReducer @Inject constructor() {

    fun openMaterials(
        state: CreateMissionUiState
    ): CreateMissionUiState {

        if (
            MissionActivityType.MATERIAL_DELIVERY !in
            state.selectedActivities
        ) {
            return state
        }

        return state.copy(
            subScreen =
                CreateMissionSubScreen.MATERIALS,

            materialsError =
                null,

            customMaterialNameError =
                null
        )
    }

    fun openSurvey(
        state: CreateMissionUiState
    ): CreateMissionUiState {

        if (
            MissionActivityType.OPINION_SURVEY !in
            state.selectedActivities
        ) {
            return state
        }

        return state.copy(
            subScreen =
                CreateMissionSubScreen.SURVEY,

            surveyError =
                null
        )
    }

    fun close(
        state: CreateMissionUiState
    ): CreateMissionUiState {

        return state.copy(
            subScreen =
                CreateMissionSubScreen.NONE,

            materialsError =
                null,

            customMaterialNameError =
                null,

            surveyError =
                null
        )
    }
}