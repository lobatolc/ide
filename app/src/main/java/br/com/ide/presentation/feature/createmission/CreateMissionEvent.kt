package br.com.ide.presentation.feature.createmission

import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionMovement
import br.com.ide.domain.model.SurveyQuestionType
import java.time.LocalDate
import java.time.LocalTime

sealed interface CreateMissionEvent {

    // =========================================================
    // Categorias
    // =========================================================

    sealed interface General :
        CreateMissionEvent

    sealed interface Participants :
        CreateMissionEvent

    sealed interface Actions :
        CreateMissionEvent

    sealed interface Materials :
        CreateMissionEvent

    sealed interface Survey :
        CreateMissionEvent

    sealed interface Summary :
        CreateMissionEvent

    sealed interface Navigation :
        CreateMissionEvent

    // =========================================================
    // Etapa 1 - Geral
    // =========================================================

    data class NameChanged(
        val value: String
    ) : General

    data class DateChanged(
        val value: LocalDate
    ) : General

    data class TimeChanged(
        val value: LocalTime
    ) : General

    data class DescriptionChanged(
        val value: String
    ) : General

    data class MovementChanged(
        val movement: MissionMovement
    ) : General

    data class CustomMovementNameChanged(
        val value: String
    ) : General

    // =========================================================
    // Etapa 2 - Participantes
    // =========================================================

    data class DistrictToggled(
        val districtId: String
    ) : Participants

    data class ChurchToggled(
        val churchId: String
    ) : Participants

    data object SelectAllChurches :
        Participants

    // =========================================================
    // Etapa 3 - Ações
    // =========================================================

    data class ActivityToggled(
        val activity: MissionActivityType
    ) : Actions

    data class CustomActivityNameChanged(
        val value: String
    ) : Actions

    // =========================================================
    // Materiais
    // =========================================================

    data object OpenMaterials :
        Materials

    data object CloseMaterials :
        Materials

    data class MaterialToggled(
        val material: MissionMaterialType
    ) : Materials

    data class CustomMaterialNameChanged(
        val value: String
    ) : Materials

    data object SaveMaterials :
        Materials

    // =========================================================
    // Pesquisa
    // =========================================================

    data object OpenSurvey :
        Survey

    data object CloseSurvey :
        Survey

    data object AddSurveyQuestion :
        Survey

    data class RemoveSurveyQuestion(
        val questionId: String
    ) : Survey

    data class SurveyQuestionChanged(
        val questionId: String,
        val value: String
    ) : Survey

    data class SurveyQuestionTypeChanged(
        val questionId: String,
        val type: SurveyQuestionType
    ) : Survey

    data class AddSurveyOption(
        val questionId: String
    ) : Survey

    data class SurveyOptionChanged(
        val questionId: String,
        val optionIndex: Int,
        val value: String
    ) : Survey

    data class RemoveSurveyOption(
        val questionId: String,
        val optionIndex: Int
    ) : Survey

    data object SaveSurvey :
        Survey

    // =========================================================
    // Resumo
    // =========================================================

    data class GoToStep(
        val step: Int
    ) : Summary

    data object ReturnToSummary :
        Summary

    // =========================================================
    // Navegação
    // =========================================================

    data object PreviousStep :
        Navigation

    data object Next :
        Navigation
}