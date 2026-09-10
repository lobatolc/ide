package br.com.ide.presentation.feature.createmission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionMovement
import br.com.ide.domain.model.SurveyQuestionDraft
import br.com.ide.domain.model.SurveyQuestionType
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.usecase.GetChurchByIdUseCase
import br.com.ide.domain.usecase.GetChurchesByDistrictUseCase
import br.com.ide.domain.usecase.GetChurchesUseCase
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetDistrictsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateMissionViewModel @Inject constructor(
    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase,
    private val getDistrictsUseCase:
    GetDistrictsUseCase,
    private val getChurchesUseCase:
    GetChurchesUseCase,
    private val getChurchesByDistrictUseCase:
    GetChurchesByDistrictUseCase,
    private val getChurchByIdUseCase:
    GetChurchByIdUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CreateMissionUiState()
        )

    val uiState:
            StateFlow<CreateMissionUiState> =
        _uiState.asStateFlow()

    fun onEvent(
        event: CreateMissionEvent
    ) {
        when (event) {

            // -------------------------------------------------
            // Etapa 1 - Geral
            // -------------------------------------------------

            is CreateMissionEvent.NameChanged -> {
                _uiState.update {
                    it.copy(
                        name = event.value,
                        nameError = null
                    )
                }
            }

            is CreateMissionEvent.DateChanged -> {
                _uiState.update {
                    it.copy(
                        date = event.value,
                        dateError = null,
                        timeError = null
                    )
                }
            }

            is CreateMissionEvent.TimeChanged -> {
                _uiState.update {
                    it.copy(
                        time = event.value,
                        timeError = null
                    )
                }
            }

            is CreateMissionEvent.DescriptionChanged -> {
                _uiState.update {
                    it.copy(
                        description = event.value
                    )
                }
            }

            is CreateMissionEvent.MovementChanged -> {
                _uiState.update {
                    it.copy(
                        movement = event.movement,
                        movementError = null,

                        customMovementName =
                            if (
                                event.movement ==
                                MissionMovement.OTHER
                            ) {
                                it.customMovementName
                            } else {
                                ""
                            },

                        customMovementNameError = null
                    )
                }
            }

            is CreateMissionEvent.CustomMovementNameChanged -> {
                _uiState.update {
                    it.copy(
                        customMovementName =
                            event.value,
                        customMovementNameError =
                            null
                    )
                }
            }

            // -------------------------------------------------
            // Etapa 2 - Participantes
            // -------------------------------------------------

            is CreateMissionEvent.ChurchToggled -> {
                toggleChurch(
                    event.churchId
                )
            }

            is CreateMissionEvent.DistrictToggled -> {
                toggleDistrict(
                    event.districtId
                )
            }

            CreateMissionEvent.SelectAllChurches -> {
                selectAllChurches()
            }

            // -------------------------------------------------
            // Etapa 3 - Ações
            // -------------------------------------------------

            is CreateMissionEvent.ActivityToggled -> {
                toggleActivity(
                    event.activity
                )
            }

            is CreateMissionEvent.CustomActivityNameChanged -> {
                _uiState.update {
                    it.copy(
                        customActivityName =
                            event.value,
                        customActivityNameError =
                            null
                    )
                }
            }

            // -------------------------------------------------
            // Materiais
            // -------------------------------------------------

            CreateMissionEvent.OpenMaterials -> {
                openMaterials()
            }

            CreateMissionEvent.CloseMaterials -> {
                closeSubScreen()
            }

            is CreateMissionEvent.MaterialToggled -> {
                toggleMaterial(
                    event.material
                )
            }

            is CreateMissionEvent.CustomMaterialNameChanged -> {
                _uiState.update {
                    it.copy(
                        customMaterialName =
                            event.value,
                        customMaterialNameError =
                            null,
                        materialsError =
                            null
                    )
                }
            }

            CreateMissionEvent.SaveMaterials -> {
                saveMaterials()
            }

            // -------------------------------------------------
            // Pesquisa
            // -------------------------------------------------

            CreateMissionEvent.OpenSurvey -> {
                openSurvey()
            }

            CreateMissionEvent.CloseSurvey -> {
                closeSubScreen()
            }

            CreateMissionEvent.AddSurveyQuestion -> {
                addSurveyQuestion()
            }

            is CreateMissionEvent.RemoveSurveyQuestion -> {
                removeSurveyQuestion(
                    event.questionId
                )
            }

            is CreateMissionEvent.SurveyQuestionChanged -> {
                updateSurveyQuestion(
                    questionId =
                        event.questionId,
                    value =
                        event.value
                )
            }

            is CreateMissionEvent.SurveyQuestionTypeChanged -> {
                updateSurveyQuestionType(
                    questionId =
                        event.questionId,
                    type =
                        event.type
                )
            }

            is CreateMissionEvent.AddSurveyOption -> {
                addSurveyOption(
                    event.questionId
                )
            }

            is CreateMissionEvent.SurveyOptionChanged -> {
                updateSurveyOption(
                    questionId =
                        event.questionId,
                    optionIndex =
                        event.optionIndex,
                    value =
                        event.value
                )
            }

            is CreateMissionEvent.RemoveSurveyOption -> {
                removeSurveyOption(
                    questionId =
                        event.questionId,
                    optionIndex =
                        event.optionIndex
                )
            }

            CreateMissionEvent.SaveSurvey -> {
                saveSurvey()
            }

            // -------------------------------------------------
            // Navegação
            // -------------------------------------------------

            CreateMissionEvent.PreviousStep -> {
                previousStep()
            }

            CreateMissionEvent.Next -> {
                nextStep()
            }
        }
    }

    // =========================================================
    // Etapa 1 - Geral
    // =========================================================

    private fun validateGeneralStep() {

        val state =
            _uiState.value

        val nameError =
            if (
                state.name.isBlank()
            ) {
                R.string
                    .create_mission_name_required
            } else {
                null
            }

        val dateError =
            when {

                state.date == null -> {
                    R.string
                        .create_mission_date_required
                }

                state.date.isBefore(
                    LocalDate.now()
                ) -> {
                    R.string
                        .create_mission_date_in_past
                }

                else -> {
                    null
                }
            }

        val timeError =
            when {

                state.time == null -> {
                    R.string
                        .create_mission_time_required
                }

                state.date != null &&
                        LocalDateTime.of(
                            state.date,
                            state.time
                        ).isBefore(
                            LocalDateTime
                                .now()
                                .withSecond(0)
                                .withNano(0)
                        ) -> {
                    R.string
                        .create_mission_time_in_past
                }

                else -> {
                    null
                }
            }

        val movementError =
            if (
                state.movement == null
            ) {
                R.string
                    .create_mission_movement_required
            } else {
                null
            }

        val customMovementNameError =
            if (
                state.movement ==
                MissionMovement.OTHER &&
                state.customMovementName
                    .isBlank()
            ) {
                R.string
                    .create_mission_custom_movement_required
            } else {
                null
            }

        val isValid =
            nameError == null &&
                    dateError == null &&
                    timeError == null &&
                    movementError == null &&
                    customMovementNameError == null

        _uiState.update {
            it.copy(
                nameError =
                    nameError,
                dateError =
                    dateError,
                timeError =
                    timeError,
                movementError =
                    movementError,
                customMovementNameError =
                    customMovementNameError
            )
        }

        if (isValid) {
            loadParticipantsStep()
        }
    }

    // =========================================================
    // Etapa 2 - Participantes
    // =========================================================

    private fun loadParticipantsStep() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    participantsStepLoading =
                        true,
                    participantsError =
                        null
                )
            }

            val creator =
                getCurrentUserProfileUseCase()
                    .getOrElse {

                        _uiState.update {
                            it.copy(
                                participantsStepLoading =
                                    false,
                                participantsError =
                                    R.string
                                        .create_mission_participants_loading_error
                            )
                        }

                        return@launch
                    }

            when (creator.role) {

                UserRole.LEADER -> {

                    val churchId =
                        creator.churchId

                    val districtId =
                        creator.districtId

                    if (
                        churchId.isNullOrBlank() ||
                        districtId.isNullOrBlank()
                    ) {

                        _uiState.update {
                            it.copy(
                                participantsStepLoading =
                                    false,
                                participantsError =
                                    R.string
                                        .create_mission_creator_assignment_error
                            )
                        }

                        return@launch
                    }

                    val church =
                        getChurchByIdUseCase(
                            churchId
                        )
                            .getOrElse {

                                _uiState.update {
                                    it.copy(
                                        participantsStepLoading =
                                            false,
                                        participantsError =
                                            R.string
                                                .create_mission_participants_loading_error
                                    )
                                }

                                return@launch
                            }

                    _uiState.update {
                        it.copy(
                            currentStep = 2,

                            creatorRole =
                                creator.role,

                            creatorDistrictId =
                                districtId,

                            creatorChurchId =
                                churchId,

                            creatorChurchName =
                                church.name,

                            districts =
                                emptyList(),

                            churches =
                                emptyList(),

                            selectedDistrictIds =
                                setOf(
                                    districtId
                                ),

                            selectedChurchIds =
                                setOf(
                                    churchId
                                ),

                            participantsStepLoading =
                                false,

                            participantsError =
                                null
                        )
                    }
                }

                UserRole.PASTOR -> {

                    val districtId =
                        creator.districtId

                    if (
                        districtId.isNullOrBlank()
                    ) {

                        _uiState.update {
                            it.copy(
                                participantsStepLoading =
                                    false,
                                participantsError =
                                    R.string
                                        .create_mission_creator_assignment_error
                            )
                        }

                        return@launch
                    }

                    val churches =
                        getChurchesByDistrictUseCase(
                            districtId
                        )
                            .getOrElse {

                                _uiState.update {
                                    it.copy(
                                        participantsStepLoading =
                                            false,
                                        participantsError =
                                            R.string
                                                .create_mission_participants_loading_error
                                    )
                                }

                                return@launch
                            }

                    _uiState.update {
                        it.copy(
                            currentStep = 2,

                            creatorRole =
                                creator.role,

                            creatorDistrictId =
                                districtId,

                            creatorChurchId =
                                null,

                            creatorChurchName =
                                "",

                            districts =
                                emptyList(),

                            churches =
                                churches,

                            selectedDistrictIds =
                                setOf(
                                    districtId
                                ),

                            selectedChurchIds =
                                emptySet(),

                            participantsStepLoading =
                                false,

                            participantsError =
                                null
                        )
                    }
                }

                UserRole.ADMIN -> {

                    val districts =
                        getDistrictsUseCase()
                            .getOrElse {

                                _uiState.update {
                                    it.copy(
                                        participantsStepLoading =
                                            false,
                                        participantsError =
                                            R.string
                                                .create_mission_participants_loading_error
                                    )
                                }

                                return@launch
                            }

                    val churches =
                        getChurchesUseCase()
                            .getOrElse {

                                _uiState.update {
                                    it.copy(
                                        participantsStepLoading =
                                            false,
                                        participantsError =
                                            R.string
                                                .create_mission_participants_loading_error
                                    )
                                }

                                return@launch
                            }

                    _uiState.update {
                        it.copy(
                            currentStep = 2,

                            creatorRole =
                                creator.role,

                            creatorDistrictId =
                                null,

                            creatorChurchId =
                                null,

                            creatorChurchName =
                                "",

                            districts =
                                districts,

                            churches =
                                churches,

                            selectedDistrictIds =
                                emptySet(),

                            selectedChurchIds =
                                emptySet(),

                            participantsStepLoading =
                                false,

                            participantsError =
                                null
                        )
                    }
                }

                UserRole.MISSIONARY -> {

                    _uiState.update {
                        it.copy(
                            participantsStepLoading =
                                false,
                            participantsError =
                                R.string
                                    .create_mission_permission_error
                        )
                    }
                }
            }
        }
    }

    private fun toggleChurch(
        churchId: String
    ) {
        _uiState.update { state ->

            if (
                state.creatorRole ==
                UserRole.LEADER
            ) {
                return@update state
            }

            val church =
                state.churches
                    .firstOrNull {
                        it.id ==
                                churchId
                    }
                    ?: return@update state

            if (
                state.creatorRole ==
                UserRole.PASTOR &&
                church.districtId !=
                state.creatorDistrictId
            ) {
                return@update state
            }

            val updatedChurches =
                state.selectedChurchIds
                    .toMutableSet()

            if (
                churchId in
                updatedChurches
            ) {
                updatedChurches.remove(
                    churchId
                )
            } else {
                updatedChurches.add(
                    churchId
                )
            }

            var updatedDistricts =
                state.selectedDistrictIds

            if (
                state.creatorRole ==
                UserRole.ADMIN
            ) {

                val districtChurchIds =
                    state.churches
                        .filter {
                            it.districtId ==
                                    church.districtId
                        }
                        .map {
                            it.id
                        }
                        .toSet()

                updatedDistricts =
                    state.selectedDistrictIds
                        .toMutableSet()
                        .apply {

                            if (
                                districtChurchIds
                                    .isNotEmpty() &&
                                updatedChurches
                                    .containsAll(
                                        districtChurchIds
                                    )
                            ) {
                                add(
                                    church.districtId
                                )
                            } else {
                                remove(
                                    church.districtId
                                )
                            }
                        }
            }

            state.copy(
                selectedDistrictIds =
                    updatedDistricts,

                selectedChurchIds =
                    updatedChurches,

                participantsError =
                    null
            )
        }
    }

    private fun toggleDistrict(
        districtId: String
    ) {
        _uiState.update { state ->

            if (
                state.creatorRole !=
                UserRole.ADMIN
            ) {
                return@update state
            }

            val churchIds =
                state.churches
                    .filter {
                        it.districtId ==
                                districtId
                    }
                    .map {
                        it.id
                    }
                    .toSet()

            if (
                churchIds.isEmpty()
            ) {
                return@update state
            }

            val updatedDistricts =
                state.selectedDistrictIds
                    .toMutableSet()

            val updatedChurches =
                state.selectedChurchIds
                    .toMutableSet()

            val allSelected =
                updatedChurches
                    .containsAll(
                        churchIds
                    )

            if (allSelected) {

                updatedDistricts.remove(
                    districtId
                )

                updatedChurches.removeAll(
                    churchIds
                )

            } else {

                updatedDistricts.add(
                    districtId
                )

                updatedChurches.addAll(
                    churchIds
                )
            }

            state.copy(
                selectedDistrictIds =
                    updatedDistricts,

                selectedChurchIds =
                    updatedChurches,

                participantsError =
                    null
            )
        }
    }

    private fun selectAllChurches() {

        _uiState.update { state ->

            if (
                state.creatorRole ==
                UserRole.LEADER
            ) {
                return@update state
            }

            val selectableChurches =
                when (
                    state.creatorRole
                ) {

                    UserRole.PASTOR -> {
                        state.churches
                            .filter {
                                it.districtId ==
                                        state.creatorDistrictId
                            }
                    }

                    UserRole.ADMIN -> {
                        state.churches
                    }

                    else -> {
                        emptyList()
                    }
                }

            val allChurchIds =
                selectableChurches
                    .map {
                        it.id
                    }
                    .toSet()

            if (
                allChurchIds.isEmpty()
            ) {
                return@update state
            }

            val shouldSelectAll =
                !state.selectedChurchIds
                    .containsAll(
                        allChurchIds
                    )

            val selectedChurchIds =
                if (shouldSelectAll) {
                    allChurchIds
                } else {
                    emptySet()
                }

            val selectedDistrictIds =
                when (
                    state.creatorRole
                ) {

                    UserRole.PASTOR -> {
                        state.creatorDistrictId
                            ?.let {
                                setOf(it)
                            }
                            ?: emptySet()
                    }

                    UserRole.ADMIN -> {

                        if (
                            shouldSelectAll
                        ) {
                            state.districts
                                .map {
                                    it.id
                                }
                                .filter {
                                        districtId ->

                                    state.churches
                                        .any {
                                            it.districtId ==
                                                    districtId
                                        }
                                }
                                .toSet()
                        } else {
                            emptySet()
                        }
                    }

                    else -> {
                        state.selectedDistrictIds
                    }
                }

            state.copy(
                selectedDistrictIds =
                    selectedDistrictIds,

                selectedChurchIds =
                    selectedChurchIds,

                participantsError =
                    null
            )
        }
    }

    private fun validateParticipantsStep() {

        val state =
            _uiState.value

        if (
            state.selectedChurchIds
                .isEmpty()
        ) {

            _uiState.update {
                it.copy(
                    participantsError =
                        R.string
                            .create_mission_participants_required
                )
            }

            return
        }

        _uiState.update {
            it.copy(
                currentStep = 3,
                participantsError = null
            )
        }
    }

    // =========================================================
    // Etapa 3 - Ações
    // =========================================================

    private fun toggleActivity(
        activity: MissionActivityType
    ) {
        _uiState.update { state ->

            val updated =
                state.selectedActivities
                    .toMutableSet()

            if (
                activity in
                updated
            ) {
                updated.remove(
                    activity
                )
            } else {
                updated.add(
                    activity
                )
            }

            val customActivityName =
                if (
                    MissionActivityType.OTHER in
                    updated
                ) {
                    state.customActivityName
                } else {
                    ""
                }

            state.copy(
                selectedActivities =
                    updated,

                customActivityName =
                    customActivityName,

                activitiesError =
                    null,

                customActivityNameError =
                    null
            )
        }
    }

    private fun validateActionsStep() {

        val state =
            _uiState.value

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
                !isSurveyValid(
                    state.surveyQuestions
                )
            ) {
                R.string
                    .create_mission_survey_required
            } else {
                null
            }

        val isValid =
            activitiesError == null &&
                    customActivityNameError == null &&
                    materialsError == null &&
                    surveyError == null

        _uiState.update {
            it.copy(
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

        if (isValid) {

            /*
             * Etapa 3 válida.
             *
             * Quando criarmos a Etapa 4 - Locais:
             *
             * _uiState.update {
             *     it.copy(
             *         currentStep = 4
             *     )
             * }
             */
        }
    }

    // =========================================================
    // Materiais
    // =========================================================

    private fun openMaterials() {

        if (
            MissionActivityType.MATERIAL_DELIVERY !in
            _uiState.value.selectedActivities
        ) {
            return
        }

        _uiState.update {
            it.copy(
                subScreen =
                    CreateMissionSubScreen.MATERIALS,

                materialsError =
                    null,

                customMaterialNameError =
                    null
            )
        }
    }

    private fun toggleMaterial(
        material: MissionMaterialType
    ) {
        _uiState.update { state ->

            val updated =
                state.selectedMaterials
                    .toMutableSet()

            if (
                material in
                updated
            ) {
                updated.remove(
                    material
                )
            } else {
                updated.add(
                    material
                )
            }

            val customMaterialName =
                if (
                    MissionMaterialType.OTHER in
                    updated
                ) {
                    state.customMaterialName
                } else {
                    ""
                }

            state.copy(
                selectedMaterials =
                    updated,

                customMaterialName =
                    customMaterialName,

                materialsError =
                    null,

                customMaterialNameError =
                    null
            )
        }
    }

    private fun saveMaterials() {

        val state =
            _uiState.value

        val materialsError =
            if (
                state.selectedMaterials
                    .isEmpty()
            ) {
                R.string
                    .create_mission_materials_required
            } else {
                null
            }

        val customMaterialNameError =
            if (
                MissionMaterialType.OTHER in
                state.selectedMaterials &&
                state.customMaterialName
                    .isBlank()
            ) {
                R.string
                    .create_mission_custom_material_required
            } else {
                null
            }

        _uiState.update {
            it.copy(
                materialsError =
                    materialsError,

                customMaterialNameError =
                    customMaterialNameError
            )
        }

        if (
            materialsError == null &&
            customMaterialNameError == null
        ) {

            _uiState.update {
                it.copy(
                    subScreen =
                        CreateMissionSubScreen.NONE,

                    materialsError =
                        null,

                    customMaterialNameError =
                        null
                )
            }
        }
    }

    // =========================================================
    // Pesquisa
    // =========================================================

    private fun openSurvey() {

        if (
            MissionActivityType.OPINION_SURVEY !in
            _uiState.value.selectedActivities
        ) {
            return
        }

        _uiState.update { state ->

            val questions =
                if (
                    state.surveyQuestions
                        .isEmpty()
                ) {
                    listOf(
                        createEmptyQuestion()
                    )
                } else {
                    state.surveyQuestions
                }

            state.copy(
                subScreen =
                    CreateMissionSubScreen.SURVEY,

                surveyQuestions =
                    questions,

                surveyError =
                    null
            )
        }
    }

    private fun addSurveyQuestion() {

        _uiState.update { state ->

            state.copy(
                surveyQuestions =
                    state.surveyQuestions +
                            createEmptyQuestion(),

                surveyError =
                    null
            )
        }
    }

    private fun removeSurveyQuestion(
        questionId: String
    ) {
        _uiState.update { state ->

            state.copy(
                surveyQuestions =
                    state.surveyQuestions
                        .filterNot {
                            it.id ==
                                    questionId
                        },

                surveyError =
                    null
            )
        }
    }

    private fun updateSurveyQuestion(
        questionId: String,
        value: String
    ) {
        _uiState.update { state ->

            state.copy(
                surveyQuestions =
                    state.surveyQuestions
                        .map { question ->

                            if (
                                question.id ==
                                questionId
                            ) {
                                question.copy(
                                    question =
                                        value
                                )
                            } else {
                                question
                            }
                        },

                surveyError =
                    null
            )
        }
    }

    private fun updateSurveyQuestionType(
        questionId: String,
        type: SurveyQuestionType
    ) {
        _uiState.update { state ->

            state.copy(
                surveyQuestions =
                    state.surveyQuestions
                        .map { question ->

                            if (
                                question.id !=
                                questionId
                            ) {
                                question

                            } else {

                                when (type) {

                                    SurveyQuestionType.TEXT -> {
                                        question.copy(
                                            type =
                                                type,
                                            options =
                                                emptyList()
                                        )
                                    }

                                    SurveyQuestionType.SINGLE_CHOICE -> {

                                        val options =
                                            if (
                                                question.options
                                                    .size >= 2
                                            ) {
                                                question.options
                                            } else {
                                                listOf(
                                                    "",
                                                    ""
                                                )
                                            }

                                        question.copy(
                                            type =
                                                type,
                                            options =
                                                options
                                        )
                                    }
                                }
                            }
                        },

                surveyError =
                    null
            )
        }
    }

    private fun addSurveyOption(
        questionId: String
    ) {
        _uiState.update { state ->

            state.copy(
                surveyQuestions =
                    state.surveyQuestions
                        .map { question ->

                            if (
                                question.id ==
                                questionId &&
                                question.type ==
                                SurveyQuestionType
                                    .SINGLE_CHOICE
                            ) {
                                question.copy(
                                    options =
                                        question.options +
                                                ""
                                )
                            } else {
                                question
                            }
                        },

                surveyError =
                    null
            )
        }
    }

    private fun updateSurveyOption(
        questionId: String,
        optionIndex: Int,
        value: String
    ) {
        _uiState.update { state ->

            state.copy(
                surveyQuestions =
                    state.surveyQuestions
                        .map { question ->

                            if (
                                question.id !=
                                questionId
                            ) {
                                return@map question
                            }

                            if (
                                optionIndex !in
                                question.options.indices
                            ) {
                                return@map question
                            }

                            val updatedOptions =
                                question.options
                                    .toMutableList()

                            updatedOptions[
                                optionIndex
                            ] = value

                            question.copy(
                                options =
                                    updatedOptions
                            )
                        },

                surveyError =
                    null
            )
        }
    }

    private fun removeSurveyOption(
        questionId: String,
        optionIndex: Int
    ) {
        _uiState.update { state ->

            state.copy(
                surveyQuestions =
                    state.surveyQuestions
                        .map { question ->

                            if (
                                question.id !=
                                questionId
                            ) {
                                return@map question
                            }

                            if (
                                optionIndex !in
                                question.options.indices
                            ) {
                                return@map question
                            }

                            val updatedOptions =
                                question.options
                                    .toMutableList()

                            updatedOptions.removeAt(
                                optionIndex
                            )

                            question.copy(
                                options =
                                    updatedOptions
                            )
                        },

                surveyError =
                    null
            )
        }
    }

    private fun saveSurvey() {

        val questions =
            _uiState.value
                .surveyQuestions

        val error =
            when {

                questions.isEmpty() -> {
                    R.string
                        .create_mission_survey_required
                }

                questions.any {
                    it.question
                        .isBlank()
                } -> {
                    R.string
                        .create_mission_survey_question_required
                }

                questions.any {
                    it.type ==
                            SurveyQuestionType.SINGLE_CHOICE &&
                            it.options
                                .count { option ->
                                    option.isNotBlank()
                                } < 2
                } -> {
                    R.string
                        .create_mission_survey_options_required
                }

                else -> {
                    null
                }
            }

        _uiState.update {
            it.copy(
                surveyError =
                    error
            )
        }

        if (
            error == null
        ) {

            _uiState.update {
                it.copy(
                    subScreen =
                        CreateMissionSubScreen.NONE,

                    surveyError =
                        null
                )
            }
        }
    }

    private fun isSurveyValid(
        questions:
        List<SurveyQuestionDraft>
    ): Boolean {

        if (
            questions.isEmpty()
        ) {
            return false
        }

        if (
            questions.any {
                it.question
                    .isBlank()
            }
        ) {
            return false
        }

        if (
            questions.any {
                it.type ==
                        SurveyQuestionType.SINGLE_CHOICE &&
                        it.options
                            .count { option ->
                                option.isNotBlank()
                            } < 2
            }
        ) {
            return false
        }

        return true
    }

    private fun createEmptyQuestion():
            SurveyQuestionDraft {

        return SurveyQuestionDraft(
            id =
                UUID.randomUUID()
                    .toString()
        )
    }

    // =========================================================
    // Navegação
    // =========================================================

    private fun previousStep() {

        if (
            _uiState.value.subScreen !=
            CreateMissionSubScreen.NONE
        ) {
            closeSubScreen()
            return
        }

        _uiState.update { state ->
            state.copy(
                currentStep =
                    (state.currentStep - 1)
                        .coerceAtLeast(1),

                participantsError =
                    null,

                activitiesError =
                    null,

                customActivityNameError =
                    null,

                materialsError =
                    null,

                customMaterialNameError =
                    null,

                surveyError =
                    null
            )
        }
    }

    private fun nextStep() {

        when (
            _uiState.value.currentStep
        ) {

            1 -> {
                validateGeneralStep()
            }

            2 -> {
                validateParticipantsStep()
            }

            3 -> {
                validateActionsStep()
            }
        }
    }

    private fun closeSubScreen() {

        _uiState.update {
            it.copy(
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
}