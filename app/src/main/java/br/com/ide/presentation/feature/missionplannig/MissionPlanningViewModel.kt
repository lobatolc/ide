package br.com.ide.presentation.feature.missionplanning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.usecase.GetMissionByIdUseCase
import br.com.ide.domain.usecase.GetMissionGroupParticipantsUseCase
import br.com.ide.domain.usecase.StartMissionUseCase
import br.com.ide.domain.usecase.UpdateMissionStatusUseCase
import br.com.ide.presentation.components.snackbar.IdeSnackbarManager
import br.com.ide.presentation.components.snackbar.IdeSnackbarMessage
import br.com.ide.presentation.components.snackbar.IdeSnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissionPlanningViewModel @Inject constructor(

    private val getMissionByIdUseCase:
    GetMissionByIdUseCase,

    private val getMissionGroupParticipantsUseCase:
    GetMissionGroupParticipantsUseCase,

    private val updateMissionStatusUseCase:
    UpdateMissionStatusUseCase,

    private val startMissionUseCase:
    StartMissionUseCase,

    private val snackbarManager:
    IdeSnackbarManager

) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MissionPlanningUiState()
        )

    val uiState:
            StateFlow<MissionPlanningUiState> =
        _uiState.asStateFlow()

    private var loadedMissionId:
            String? =
        null

    // =========================================================
    // Carregamento
    // =========================================================

    fun load(
        missionId: String,
        force: Boolean = false
    ) {

        if (
            !force &&
            loadedMissionId ==
            missionId
        ) {
            return
        }

        loadedMissionId =
            missionId

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading =
                        true
                )
            }

            try {

                val mission =
                    getMissionByIdUseCase(
                        missionId
                    )

                if (
                    mission == null
                ) {

                    _uiState.update {
                        it.copy(
                            isLoading =
                                false
                        )
                    }

                    return@launch
                }

                // =================================================
                // Participantes elegíveis
                // =================================================

                val eligibleParticipants =
                    getMissionGroupParticipantsUseCase(
                        participatingChurchIds =
                            mission
                                .participatingChurchIds
                    )
                        .getOrElse {
                            emptyList()
                        }

                val eligibleParticipantIds =
                    eligibleParticipants
                        .map {
                            it.id
                        }
                        .toSet()

                // =================================================
                // Participantes já distribuídos
                // =================================================

                val groupedParticipantIds =
                    mission
                        .groups
                        .flatMap {
                            it.participantIds
                        }
                        .filter {
                            it in
                                    eligibleParticipantIds
                        }
                        .toSet()

                val groupedParticipantCount =
                    groupedParticipantIds
                        .size

                val unassignedParticipantCount =
                    eligibleParticipants
                        .count { participant ->

                            participant.id !in
                                    groupedParticipantIds
                        }

                // =================================================
                // Área
                // =================================================

                val area =
                    mission.area

                // =================================================
                // Estado
                // =================================================

                _uiState.update {
                    it.copy(

                        missionId =
                            mission.id,

                        missionName =
                            mission.name,

                        missionStatus =
                            mission.status,

                        // -----------------------------
                        // Locais
                        // -----------------------------

                        hasDepartureLocation =
                            mission
                                .departureLocation !=
                                    null,

                        hasReturnLocation =
                            mission
                                .returnLocation !=
                                    null,

                        // -----------------------------
                        // Grupos
                        // -----------------------------

                        groupCount =
                            mission.groups.size,

                        groupedParticipantCount =
                            groupedParticipantCount,

                        unassignedParticipantCount =
                            unassignedParticipantCount,

                        // -----------------------------
                        // Área
                        // -----------------------------

                        hasDefinedArea =
                            area != null,

                        areaPointCount =
                            area
                                ?.polygonPoints
                                ?.size
                                ?: 0,

                        // -----------------------------
                        // Controle
                        // -----------------------------

                        isLoading =
                            false
                    )
                }

            } catch (
                exception: Exception
            ) {

                _uiState.update {
                    it.copy(
                        isLoading =
                            false
                    )
                }
            }
        }
    }

    // =========================================================
    // Atualizar ao retornar de uma configuração
    // =========================================================

    fun refresh(
        missionId: String
    ) {

        load(
            missionId =
                missionId,

            force =
                true
        )
    }

    // =========================================================
    // Agendar missão
    // PLANNING -> SCHEDULED
    // =========================================================

    fun scheduleMission() {

        val currentState =
            _uiState.value

        if (
            currentState.isLoading ||
            currentState.isScheduling ||
            currentState.isStarting ||
            currentState.missionId.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isScheduling =
                        true
                )
            }

            try {

                /*
                 * Recarregamos a missão antes de alterar o status.
                 * Assim evitamos regredir o status caso ele tenha
                 * sido alterado em outro ponto do app/dispositivo.
                 */
                val mission =
                    getMissionByIdUseCase(
                        currentState.missionId
                    )

                if (
                    mission == null
                ) {

                    _uiState.update {
                        it.copy(
                            isScheduling =
                                false
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .mission_planning_schedule_error,
                            type =
                                IdeSnackbarType.ERROR
                        )
                    )

                    return@launch
                }

                // -------------------------------------------------
                // Status atual
                // -------------------------------------------------

                if (
                    mission.status !=
                    MissionStatus.PLANNING
                ) {

                    _uiState.update {
                        it.copy(
                            missionStatus =
                                mission.status,
                            isScheduling =
                                false
                        )
                    }

                    return@launch
                }

                // -------------------------------------------------
                // Validação obrigatória
                // -------------------------------------------------

                if (
                    mission.departureLocation ==
                    null
                ) {

                    _uiState.update {
                        it.copy(
                            hasDepartureLocation =
                                false,
                            isScheduling =
                                false
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .mission_planning_departure_required,
                            type =
                                IdeSnackbarType.WARNING
                        )
                    )

                    return@launch
                }

                // -------------------------------------------------
                // Atualização
                // -------------------------------------------------

                val result =
                    updateMissionStatusUseCase(
                        missionId =
                            mission.id,
                        status =
                            MissionStatus.SCHEDULED
                    )

                result
                    .onSuccess {

                        _uiState.update {
                            it.copy(
                                missionStatus =
                                    MissionStatus.SCHEDULED,
                                hasDepartureLocation =
                                    true,
                                isScheduling =
                                    false
                            )
                        }

                        snackbarManager.show(
                            IdeSnackbarMessage(
                                messageRes =
                                    R.string
                                        .mission_planning_schedule_success,
                                type =
                                    IdeSnackbarType.SUCCESS
                            )
                        )
                    }
                    .onFailure {

                        _uiState.update {
                            it.copy(
                                isScheduling =
                                    false
                            )
                        }

                        snackbarManager.show(
                            IdeSnackbarMessage(
                                messageRes =
                                    R.string
                                        .mission_planning_schedule_error,
                                type =
                                    IdeSnackbarType.ERROR
                            )
                        )
                    }

            } catch (
                exception: Exception
            ) {

                _uiState.update {
                    it.copy(
                        isScheduling =
                            false
                    )
                }

                snackbarManager.show(
                    IdeSnackbarMessage(
                        messageRes =
                            R.string
                                .mission_planning_schedule_error,
                        type =
                            IdeSnackbarType.ERROR
                    )
                )
            }
        }
    }
    // =========================================================
    // Iniciar missão
    // SCHEDULED -> IN_PROGRESS
    // =========================================================

    fun startMission() {

        val currentState =
            _uiState.value

        if (
            currentState.isLoading ||
            currentState.isScheduling ||
            currentState.isStarting ||
            currentState.missionId.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isStarting =
                        true
                )
            }

            try {

                /*
                 * Recarregamos a missão antes de iniciar.
                 * Isso evita sobrescrever um status que tenha sido
                 * alterado em outro ponto do app/dispositivo.
                 */
                val mission =
                    getMissionByIdUseCase(
                        currentState.missionId
                    )

                if (
                    mission == null
                ) {

                    _uiState.update {
                        it.copy(
                            isStarting =
                                false
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .mission_planning_start_error,
                            type =
                                IdeSnackbarType.ERROR
                        )
                    )

                    return@launch
                }

                // -------------------------------------------------
                // Status atual
                // -------------------------------------------------

                if (
                    mission.status !=
                    MissionStatus.SCHEDULED
                ) {

                    _uiState.update {
                        it.copy(
                            missionStatus =
                                mission.status,
                            isStarting =
                                false
                        )
                    }

                    return@launch
                }

                // -------------------------------------------------
                // Validação obrigatória
                // -------------------------------------------------

                if (
                    mission.departureLocation ==
                    null
                ) {

                    _uiState.update {
                        it.copy(
                            hasDepartureLocation =
                                false,
                            isStarting =
                                false
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .mission_planning_start_departure_required,
                            type =
                                IdeSnackbarType.WARNING
                        )
                    )

                    return@launch
                }

                // -------------------------------------------------
                // Atualização
                // -------------------------------------------------

                val result =
                    startMissionUseCase(
                        missionId =
                            mission.id
                    )

                result
                    .onSuccess {

                        _uiState.update {
                            it.copy(
                                missionStatus =
                                    MissionStatus.IN_PROGRESS,
                                hasDepartureLocation =
                                    true,
                                isStarting =
                                    false
                            )
                        }

                        snackbarManager.show(
                            IdeSnackbarMessage(
                                messageRes =
                                    R.string
                                        .mission_planning_start_success,
                                type =
                                    IdeSnackbarType.SUCCESS
                            )
                        )
                    }
                    .onFailure {

                        _uiState.update {
                            it.copy(
                                isStarting =
                                    false
                            )
                        }

                        snackbarManager.show(
                            IdeSnackbarMessage(
                                messageRes =
                                    R.string
                                        .mission_planning_start_error,
                                type =
                                    IdeSnackbarType.ERROR
                            )
                        )
                    }

            } catch (
                exception: Exception
            ) {

                _uiState.update {
                    it.copy(
                        isStarting =
                            false
                    )
                }

                snackbarManager.show(
                    IdeSnackbarMessage(
                        messageRes =
                            R.string
                                .mission_planning_start_error,
                        type =
                            IdeSnackbarType.ERROR
                    )
                )
            }
        }
    }

}
