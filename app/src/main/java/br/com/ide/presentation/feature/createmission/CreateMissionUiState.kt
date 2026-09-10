package br.com.ide.presentation.feature.createmission

import androidx.annotation.StringRes
import br.com.ide.domain.model.Church
import br.com.ide.domain.model.District
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionMovement
import br.com.ide.domain.model.SurveyQuestionDraft
import br.com.ide.domain.model.UserRole
import java.time.LocalDate
import java.time.LocalTime

data class CreateMissionUiState(

    // Etapa 1 - Geral

    val name: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val description: String = "",

    val movement: MissionMovement? = null,
    val customMovementName: String = "",

    @StringRes
    val nameError: Int? = null,

    @StringRes
    val dateError: Int? = null,

    @StringRes
    val timeError: Int? = null,

    @StringRes
    val movementError: Int? = null,

    @StringRes
    val customMovementNameError: Int? = null,

    // Wizard

    val currentStep: Int = 1,

    val subScreen:
    CreateMissionSubScreen =
        CreateMissionSubScreen.NONE,

    // Etapa 2 - Participantes

    val creatorRole: UserRole? = null,

    val creatorDistrictId: String? = null,
    val creatorChurchId: String? = null,
    val creatorChurchName: String = "",

    val districts: List<District> = emptyList(),
    val churches: List<Church> = emptyList(),

    val selectedDistrictIds: Set<String> = emptySet(),
    val selectedChurchIds: Set<String> = emptySet(),

    val participantsStepLoading: Boolean = false,

    @StringRes
    val participantsError: Int? = null,

    // Etapa 3 - Ações

    val selectedActivities:
    Set<MissionActivityType> = emptySet(),

    val customActivityName:
    String = "",

    @StringRes
    val activitiesError: Int? = null,

    @StringRes
    val customActivityNameError: Int? = null,

    // Materiais

    val selectedMaterials:
    Set<MissionMaterialType> = emptySet(),

    val customMaterialName:
    String = "",

    @StringRes
    val materialsError: Int? = null,

    @StringRes
    val customMaterialNameError: Int? = null,

    // Pesquisa

    val surveyQuestions:
    List<SurveyQuestionDraft> = emptyList(),

    @StringRes
    val surveyError: Int? = null,

    // Geral

    @StringRes
    val errorMessage: Int? = null,

    val isLoading: Boolean = false
)