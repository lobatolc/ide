package br.com.ide.presentation.feature.missionplanning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.domain.usecase.GetMissionByIdUseCase
import br.com.ide.domain.usecase.GetMissionGroupParticipantsUseCase
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
    GetMissionGroupParticipantsUseCase

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
}