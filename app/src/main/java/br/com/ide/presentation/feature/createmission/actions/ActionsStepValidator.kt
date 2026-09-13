package br.com.ide.presentation.feature.createmission.actions

import br.com.ide.R
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import br.com.ide.presentation.feature.createmission.survey.SurveyValidator
import javax.inject.Inject

data class ActionsStepValidationResult(
    val activitiesError: Int? = null,
    val customActivityNameError: Int? = null,
    val materialsError: Int? = null,
    val surveyError: Int? = null
) {

    val isValid: Boolean
        get() =
            activitiesError == null &&
                    customActivityNameError == null &&
                    materialsError == null &&
                    surveyError == null
}

class ActionsStepValidator @Inject constructor(
    private val surveyValidator:
    SurveyValidator
) {

    fun validate(
        state: CreateMissionUiState
    ): ActionsStepValidationResult {

        val activitiesError =
            if (
                state.selectedActivities
                    .isEmpty()
            ) {
                R.string
                    .create_mission_activities_required
            } else {
                null
            }

        val customActivityNameError =
            if (
                MissionActivityType.OTHER in
                state.selectedActivities &&
                state.customActivityName
                    .isBlank()
            ) {
                R.string
                    .create_mission_custom_activity_required
            } else {
                null
            }

        val materialsError =
            if (
                MissionActivityType.MATERIAL_DELIVERY in
                state.selectedActivities &&
                state.selectedMaterials
                    .isEmpty()
            ) {
                R.string
                    .create_mission_materials_required
            } else {
                null
            }

        val surveyError =
            if (
                MissionActivityType.OPINION_SURVEY in
                state.selectedActivities &&
                !surveyValidator.isValid(
                    state.surveyQuestions
                )
            ) {
                R.string
                    .create_mission_survey_required
            } else {
                null
            }

        return ActionsStepValidationResult(
            activitiesError =
                activitiesError,
            customActivityNameError =
                customActivityNameError,
            materialsError =
                materialsError,
            surveyError =
                surveyError
        )
    }
}