package br.com.ide.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetMissionParticipantsUseCase
import br.com.ide.domain.usecase.GetMissionsUseCase
import br.com.ide.presentation.components.snackbar.IdeSnackbarManager
import br.com.ide.presentation.components.snackbar.IdeSnackbarMessage
import br.com.ide.presentation.components.snackbar.IdeSnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMissionsUseCase:
    GetMissionsUseCase,

    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase,

    private val getMissionParticipantsUseCase:
    GetMissionParticipantsUseCase,

    private val snackbarManager:
    IdeSnackbarManager
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            HomeUiState()
        )

    val uiState:
            StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    private val _effects =
        MutableSharedFlow<HomeEffect>()

    val effects:
            SharedFlow<HomeEffect> =
        _effects.asSharedFlow()

    private var allMissions:
            List<Mission> =
        emptyList()

    private var currentUserId:
            String? =
        null

    private var isCheckingMissionAccess =
        false

    init {

        loadUserProfile()

        onEvent(
            HomeEvent.LoadMissions
        )
    }

    fun onEvent(
        event: HomeEvent
    ) {

        when (
            event
        ) {

            HomeEvent.LoadMissions -> {

                loadMissions(
                    showLoading =
                        true
                )
            }

            HomeEvent.Refresh -> {

                loadMissions(
                    showLoading =
                        allMissions.isEmpty()
                )
            }

            is HomeEvent.SearchChanged -> {

                _uiState.update {
                    it.copy(
                        searchQuery =
                            event.query
                    )
                }

                applyFilters()
            }

            is HomeEvent.StatusSelected -> {

                _uiState.update {
                    it.copy(
                        selectedStatus =
                            event.status
                    )
                }

                applyFilters()
            }

            is HomeEvent.MissionClicked -> {

                openMission(
                    missionId =
                        event.missionId,
                    missionStatus =
                        event.missionStatus
                )
            }

            HomeEvent.Retry -> {

                loadMissions(
                    showLoading =
                        true
                )
            }
        }
    }

    private fun openMission(
        missionId: String,
        missionStatus: MissionStatus
    ) {

        /*
         * Missões concluídas não voltam para o fluxo operacional.
         *
         * Os resultados históricos ficarão disponíveis na aba
         * Métricas da tela principal.
         */
        if (
            missionStatus ==
            MissionStatus.COMPLETED
        ) {
            viewModelScope.launch {
                snackbarManager.show(
                    IdeSnackbarMessage(
                        messageRes =
                            R.string
                                .home_mission_already_completed,
                        type =
                            IdeSnackbarType.WARNING
                    )
                )
            }

            return
        }

        if (
            isCheckingMissionAccess
        ) {
            return
        }

        if (
            missionStatus ==
            MissionStatus.IN_PROGRESS
        ) {
            isCheckingMissionAccess =
                true
        }

        viewModelScope.launch {

            if (
                missionStatus !=
                MissionStatus.IN_PROGRESS
            ) {
                _effects.emit(
                    HomeEffect.OpenMission(
                        missionId =
                            missionId,
                        missionStatus =
                            missionStatus
                    )
                )

                return@launch
            }

            try {

                val userId =
                    currentUserId
                        ?: getCurrentUserProfileUseCase()
                            .getOrNull()
                            ?.id
                            ?.also {
                                currentUserId =
                                    it
                            }

                if (
                    userId == null
                ) {
                    showMissionAccessCheckError()
                    return@launch
                }

                val participants =
                    getMissionParticipantsUseCase(
                        missionId =
                            missionId
                    )
                        .getOrElse {
                            showMissionAccessCheckError()
                            return@launch
                        }

                val participationFinished =
                    participants
                        .any { participant ->
                            participant.userId ==
                                    userId &&
                                    participant.status ==
                                    MissionParticipantStatus.FINISHED
                        }

                if (
                    participationFinished
                ) {
                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .home_participation_finished,
                            type =
                                IdeSnackbarType.WARNING
                        )
                    )

                    return@launch
                }

                _effects.emit(
                    HomeEffect.OpenMission(
                        missionId =
                            missionId,
                        missionStatus =
                            missionStatus
                    )
                )

            } finally {
                isCheckingMissionAccess =
                    false
            }
        }
    }

    private suspend fun showMissionAccessCheckError() {
        snackbarManager.show(
            IdeSnackbarMessage(
                messageRes =
                    R.string
                        .home_mission_access_check_error,
                type =
                    IdeSnackbarType.ERROR
            )
        )
    }

    private fun loadMissions(
        showLoading: Boolean
    ) {

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading =
                        showLoading,
                    errorMessage =
                        null
                )
            }

            try {

                val missions =
                    getMissionsUseCase()

                allMissions =
                    missions

                _uiState.update {
                    it.copy(
                        isLoading =
                            false
                    )
                }

                applyFilters()

            } catch (
                exception: Exception
            ) {

                _uiState.update {
                    it.copy(
                        isLoading =
                            false,
                        errorMessage =
                            R.string
                                .home_loading_error
                    )
                }
            }
        }
    }

    private fun applyFilters() {

        val state =
            _uiState.value

        val query =
            state.searchQuery
                .trim()

        val filteredMissions =
            allMissions
                .filter { mission ->

                    val matchesSearch =
                        query.isBlank() ||
                                mission.name.contains(
                                    query,
                                    ignoreCase =
                                        true
                                ) ||
                                mission.description.contains(
                                    query,
                                    ignoreCase =
                                        true
                                ) ||
                                mission.customMovementName
                                    ?.contains(
                                        query,
                                        ignoreCase =
                                            true
                                    ) ==
                                true ||
                                mission.customActivityName
                                    ?.contains(
                                        query,
                                        ignoreCase =
                                            true
                                    ) ==
                                true ||
                                mission.customMaterialName
                                    ?.contains(
                                        query,
                                        ignoreCase =
                                            true
                                    ) ==
                                true

                    val matchesStatus =
                        state.selectedStatus ==
                                null ||
                                mission.status ==
                                state.selectedStatus

                    matchesSearch &&
                            matchesStatus
                }
                .sortedBy { mission ->
                    mission.scheduledAt
                }

        _uiState.update {
            it.copy(
                missions =
                    filteredMissions
            )
        }
    }

    private fun loadUserProfile() {

        viewModelScope.launch {

            getCurrentUserProfileUseCase()
                .onSuccess { user ->

                    currentUserId =
                        user.id

                    _uiState.update {
                        it.copy(
                            userName =
                                user.firstName,
                            userRole =
                                user.role
                        )
                    }
                }
        }
    }
}
