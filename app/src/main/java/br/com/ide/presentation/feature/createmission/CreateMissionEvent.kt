package br.com.ide.presentation.feature.createmission

import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionMovement
import br.com.ide.domain.model.SurveyQuestionType
import java.time.LocalDate
import java.time.LocalTime

sealed interface CreateMissionEvent {

    // Etapa 1

    data class NameChanged(
        val value: String
    ) : CreateMissionEvent

    data class DateChanged(
        val value: LocalDate
    ) : CreateMissionEvent

    data class TimeChanged(
        val value: LocalTime
    ) : CreateMissionEvent

    data class DescriptionChanged(
        val value: String
    ) : CreateMissionEvent

    data class MovementChanged(
        val movement: MissionMovement
    ) : CreateMissionEvent

    data class CustomMovementNameChanged(
        val value: String
    ) : CreateMissionEvent

    // Etapa 2

    data class DistrictToggled(
        val districtId: String
    ) : CreateMissionEvent

    data class ChurchToggled(
        val churchId: String
    ) : CreateMissionEvent

    data object SelectAllChurches :
        CreateMissionEvent

    // Etapa 3

    data class ActivityToggled(
        val activity: MissionActivityType
    ) : CreateMissionEvent

    data class CustomActivityNameChanged(
        val value: String
    ) : CreateMissionEvent

    // Materiais

    data object OpenMaterials :
        CreateMissionEvent

    data object CloseMaterials :
        CreateMissionEvent

    data class MaterialToggled(
        val material: MissionMaterialType
    ) : CreateMissionEvent

    data class CustomMaterialNameChanged(
        val value: String
    ) : CreateMissionEvent

    data object SaveMaterials :
        CreateMissionEvent

    // Pesquisa

    data object OpenSurvey :
        CreateMissionEvent

    data object CloseSurvey :
        CreateMissionEvent

    data object AddSurveyQuestion :
        CreateMissionEvent

    data class RemoveSurveyQuestion(
        val questionId: String
    ) : CreateMissionEvent

    data class SurveyQuestionChanged(
        val questionId: String,
        val value: String
    ) : CreateMissionEvent

    data class SurveyQuestionTypeChanged(
        val questionId: String,
        val type: SurveyQuestionType
    ) : CreateMissionEvent

    data class AddSurveyOption(
        val questionId: String
    ) : CreateMissionEvent

    data class SurveyOptionChanged(
        val questionId: String,
        val optionIndex: Int,
        val value: String
    ) : CreateMissionEvent

    data class RemoveSurveyOption(
        val questionId: String,
        val optionIndex: Int
    ) : CreateMissionEvent

    data object SaveSurvey :
        CreateMissionEvent

    // Navegação

    data object PreviousStep :
        CreateMissionEvent

    data object Next :
        CreateMissionEvent
}