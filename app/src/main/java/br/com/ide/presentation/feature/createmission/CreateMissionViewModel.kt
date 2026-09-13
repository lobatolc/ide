package br.com.ide.presentation.feature.createmission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.usecase.CreateMissionUseCase
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.presentation.components.snackbar.IdeSnackbarManager
import br.com.ide.presentation.components.snackbar.IdeSnackbarMessage
import br.com.ide.presentation.feature.createmission.actions.ActionsEventHandler
import br.com.ide.presentation.feature.createmission.actions.ActionsStepValidator
import br.com.ide.presentation.feature.createmission.general.GeneralEventHandler
import br.com.ide.presentation.feature.createmission.general.GeneralStepValidator
import br.com.ide.presentation.feature.createmission.materials.MaterialsEventHandler
import br.com.ide.presentation.feature.createmission.materials.MaterialsValidator
import br.com.ide.presentation.feature.createmission.participants.ParticipantsEventHandler
import br.com.ide.presentation.feature.createmission.participants.ParticipantsStepLoadOutcome
import br.com.ide.presentation.feature.createmission.participants.ParticipantsStepLoader
import br.com.ide.presentation.feature.createmission.participants.ParticipantsStepValidator
import br.com.ide.presentation.feature.createmission.summary.SummaryEventHandler
import br.com.ide.presentation.feature.createmission.survey.SurveyEventHandler
import br.com.ide.presentation.feature.createmission.survey.SurveyValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import br.com.ide.presentation.components.snackbar.IdeSnackbarType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class CreateMissionViewModel @Inject constructor(

    // -------------------------------------------------
    // Validators
    // -------------------------------------------------

    private val generalStepValidator:
    GeneralStepValidator,

    private val participantsStepValidator:
    ParticipantsStepValidator,

    private val actionsStepValidator:
    ActionsStepValidator,

    private val materialsValidator:
    MaterialsValidator,

    private val surveyValidator:
    SurveyValidator,

    // -------------------------------------------------
    // Loaders
    // -------------------------------------------------

    private val participantsStepLoader:
    ParticipantsStepLoader,

    // -------------------------------------------------
    // Event handlers
    // -------------------------------------------------

    private val generalEventHandler:
    GeneralEventHandler,

    private val participantsEventHandler:
    ParticipantsEventHandler,

    private val actionsEventHandler:
    ActionsEventHandler,

    private val materialsEventHandler:
    MaterialsEventHandler,

    private val surveyEventHandler:
    SurveyEventHandler,

    private val summaryEventHandler:
    SummaryEventHandler,

    private val navigationEventHandler:
    NavigationEventHandler,

    // -------------------------------------------------
    // Reducers de fluxo
    // -------------------------------------------------

    private val navigationReducer:
    CreateMissionNavigationReducer,

    private val subScreenReducer:
    CreateMissionSubScreenReducer,

    private val createMissionMapper:
    CreateMissionMapper,

    private val createMissionUseCase:
    CreateMissionUseCase,

    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase,
    private val snackbarManager:
    IdeSnackbarManager,


    ) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CreateMissionUiState()
        )

    val uiState:
            StateFlow<CreateMissionUiState> =
        _uiState.asStateFlow()

    private val _effects =
        MutableSharedFlow<CreateMissionEffect>()

    val effects:
            SharedFlow<CreateMissionEffect> =
        _effects.asSharedFlow()

    // =========================================================
    // Eventos
    // =========================================================

    fun onEvent(
        event: CreateMissionEvent
    ) {

        when (event) {

            // -------------------------------------------------
            // Geral
            // -------------------------------------------------

            is CreateMissionEvent.General -> {

                _uiState.update { state ->

                    generalEventHandler.handle(
                        state = state,
                        event = event
                    )
                }
            }

            // -------------------------------------------------
            // Participantes
            // -------------------------------------------------

            is CreateMissionEvent.Participants -> {

                _uiState.update { state ->

                    participantsEventHandler.handle(
                        state = state,
                        event = event
                    )
                }
            }

            // -------------------------------------------------
            // Ações
            // -------------------------------------------------

            is CreateMissionEvent.Actions -> {

                _uiState.update { state ->

                    actionsEventHandler.handle(
                        state = state,
                        event = event
                    )
                }
            }

            // -------------------------------------------------
            // Materiais
            // -------------------------------------------------

            is CreateMissionEvent.Materials -> {

                if (
                    event ==
                    CreateMissionEvent.SaveMaterials
                ) {

                    saveMaterials()
                    return
                }

                _uiState.update { state ->

                    materialsEventHandler.handle(
                        state = state,
                        event = event
                    )
                }
            }

            // -------------------------------------------------
            // Pesquisa
            // -------------------------------------------------

            is CreateMissionEvent.Survey -> {

                if (
                    event ==
                    CreateMissionEvent.SaveSurvey
                ) {

                    saveSurvey()
                    return
                }

                _uiState.update { state ->

                    surveyEventHandler.handle(
                        state = state,
                        event = event
                    )
                }
            }

            // -------------------------------------------------
            // Resumo
            // -------------------------------------------------

            is CreateMissionEvent.Summary -> {

                if (
                    event ==
                    CreateMissionEvent.CreateMission
                ) {

                    createMission()
                    return
                }

                _uiState.update { state ->

                    summaryEventHandler.handle(
                        state = state,
                        event = event
                    )
                }
            }

            // -------------------------------------------------
            // Navegação
            // -------------------------------------------------

            is CreateMissionEvent.Navigation -> {

                navigationEventHandler.handle(
                    event = event,
                    onPreviousStep =
                        ::previousStep,
                    onNextStep =
                        ::nextStep
                )
            }
        }
    }

    // =========================================================
    // Etapa 1 - Geral
    // =========================================================

    private fun validateGeneralStep() {

        val state =
            _uiState.value

        val validation =
            generalStepValidator.validate(
                state
            )

        _uiState.update {
            it.copy(
                nameError =
                    validation.nameError,

                dateError =
                    validation.dateError,

                timeError =
                    validation.timeError,

                movementError =
                    validation.movementError,

                customMovementNameError =
                    validation
                        .customMovementNameError
            )
        }

        if (
            !validation.isValid
        ) {
            return
        }

        when {

            /*
             * A etapa foi aberta a partir
             * da tela de Resumo.
             */
            state.editingFromSummary -> {

                _uiState.update { currentState ->

                    navigationReducer
                        .goToSummary(
                            currentState
                        )
                }
            }

            /*
             * Participantes já foram carregados.
             *
             * Não carregamos novamente para não
             * perder as seleções existentes.
             */
            state.creatorRole != null -> {

                _uiState.update { currentState ->

                    navigationReducer
                        .goToParticipants(
                            currentState
                        )
                }
            }

            /*
             * Primeira entrada na etapa
             * de Participantes.
             */
            else -> {

                loadParticipantsStep()
            }
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

            when (
                val outcome =
                    participantsStepLoader.load()
            ) {

                is ParticipantsStepLoadOutcome.Error -> {

                    _uiState.update {
                        it.copy(
                            participantsStepLoading =
                                false,

                            participantsError =
                                outcome.messageRes
                        )
                    }
                }

                is ParticipantsStepLoadOutcome.Success -> {

                    val result =
                        outcome.result

                    _uiState.update {
                        it.copy(
                            currentStep =
                                2,

                            creatorRole =
                                result.creatorRole,

                            creatorDistrictId =
                                result.creatorDistrictId,

                            creatorChurchId =
                                result.creatorChurchId,

                            creatorChurchName =
                                result.creatorChurchName,

                            districts =
                                result.districts,

                            churches =
                                result.churches,

                            selectedDistrictIds =
                                result.selectedDistrictIds,

                            selectedChurchIds =
                                result.selectedChurchIds,

                            participantsStepLoading =
                                false,

                            participantsError =
                                null
                        )
                    }
                }
            }
        }
    }

    private fun validateParticipantsStep() {

        val state =
            _uiState.value

        val validation =
            participantsStepValidator.validate(
                state
            )

        _uiState.update {
            it.copy(
                participantsError =
                    validation.participantsError
            )
        }

        if (
            !validation.isValid
        ) {
            return
        }

        _uiState.update { currentState ->

            if (
                state.editingFromSummary
            ) {

                navigationReducer
                    .goToSummary(
                        currentState
                    )

            } else {

                navigationReducer
                    .goToActions(
                        currentState
                    )
            }
        }
    }

    // =========================================================
    // Etapa 3 - Ações
    // =========================================================

    private fun validateActionsStep() {

        val state =
            _uiState.value

        val validation =
            actionsStepValidator.validate(
                state
            )

        _uiState.update {
            it.copy(
                activitiesError =
                    validation.activitiesError,

                customActivityNameError =
                    validation
                        .customActivityNameError,

                materialsError =
                    validation.materialsError,

                surveyError =
                    validation.surveyError
            )
        }

        if (
            !validation.isValid
        ) {
            return
        }

        _uiState.update { currentState ->

            navigationReducer
                .goToSummary(
                    currentState
                )
        }
    }

    // =========================================================
    // Materiais
    // =========================================================

    private fun saveMaterials() {

        val validation =
            materialsValidator.validate(
                _uiState.value
            )

        _uiState.update { state ->

            if (
                validation.isValid
            ) {

                subScreenReducer.close(
                    state.copy(
                        materialsError =
                            null,

                        customMaterialNameError =
                            null
                    )
                )

            } else {

                state.copy(
                    materialsError =
                        validation.materialsError,

                    customMaterialNameError =
                        validation
                            .customMaterialNameError
                )
            }
        }
    }

    // =========================================================
    // Pesquisa
    // =========================================================

    private fun saveSurvey() {

        val error =
            surveyValidator.validate(
                _uiState.value
                    .surveyQuestions
            )

        _uiState.update { state ->

            if (
                error == null
            ) {

                subScreenReducer.close(
                    state.copy(
                        surveyError =
                            null
                    )
                )

            } else {

                state.copy(
                    surveyError =
                        error
                )
            }
        }
    }

    // =========================================================
    // Navegação
    // =========================================================

    private fun previousStep() {

        /*
         * Se estivermos dentro de uma
         * subtela, voltar fecha apenas
         * essa subtela.
         */
        if (
            _uiState.value.subScreen !=
            CreateMissionSubScreen.NONE
        ) {

            _uiState.update { state ->

                subScreenReducer
                    .close(
                        state
                    )
            }

            return
        }

        /*
         * Caso contrário, navegação
         * normal entre etapas.
         */
        _uiState.update { state ->

            navigationReducer
                .previousStep(
                    state
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

    // =========================================================
// Criação da missão
// =========================================================

    private fun createMission() {

        if (
            _uiState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val userProfile =
                getCurrentUserProfileUseCase()
                    .getOrElse {

                        _uiState.update { state ->
                            state.copy(
                                isLoading = false
                            )
                        }

                        snackbarManager.show(
                            IdeSnackbarMessage(
                                messageRes =
                                    R.string.create_mission_user_error,
                                type =
                                    IdeSnackbarType.ERROR
                            )
                        )

                        return@launch
                    }

            val mission =
                createMissionMapper.map(
                    state = _uiState.value,
                    createdBy = userProfile.id
                )

            createMissionUseCase(
                mission
            )
                .onSuccess { missionId ->

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string.create_mission_success,
                            type =
                                IdeSnackbarType.SUCCESS
                        )
                    )

                    _effects.emit(
                        CreateMissionEffect.MissionCreated(
                            missionId = missionId
                        )
                    )
                }
                .onFailure {

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string.create_mission_error,
                            type =
                                IdeSnackbarType.ERROR
                        )
                    )
                }
        }
    }


}