package br.com.ide.presentation.feature.missionexecution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.location.MissionLocationServiceController
import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionCoordinate
import br.com.ide.domain.model.MissionEncounterMarker
import br.com.ide.domain.model.MissionGeneralMetrics
import br.com.ide.domain.model.MissionPersonalMetrics
import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.model.MissionTrackPoint
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.usecase.CalculateMissionGeneralMetricsUseCase
import br.com.ide.domain.usecase.CalculateMissionPersonalMetricsUseCase
import br.com.ide.domain.usecase.FinishMissionUseCase
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetMissionByIdUseCase
import br.com.ide.domain.usecase.GetVisibleMissionTrackUserIdsUseCase
import br.com.ide.domain.usecase.GetUserByIdUseCase
import br.com.ide.domain.usecase.ObserveMissionEncountersUseCase
import br.com.ide.domain.usecase.ObserveMissionParticipantsUseCase
import br.com.ide.domain.usecase.ObserveMissionTrackUseCase
import br.com.ide.domain.usecase.UpdateMissionParticipantStatusUseCase
import br.com.ide.presentation.components.map.MissionParticipantMarker
import br.com.ide.presentation.components.map.MissionTrackLine
import br.com.ide.presentation.components.snackbar.IdeSnackbarManager
import br.com.ide.presentation.components.snackbar.IdeSnackbarMessage
import br.com.ide.presentation.components.snackbar.IdeSnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class MissionExecutionViewModel @Inject constructor(

    private val getMissionByIdUseCase:
    GetMissionByIdUseCase,

    private val calculateMissionPersonalMetricsUseCase:
    CalculateMissionPersonalMetricsUseCase,

    private val calculateMissionGeneralMetricsUseCase:
    CalculateMissionGeneralMetricsUseCase,

    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase,

    private val observeMissionParticipantsUseCase:
    ObserveMissionParticipantsUseCase,

    private val observeMissionTrackUseCase:
    ObserveMissionTrackUseCase,

    private val getVisibleMissionTrackUserIdsUseCase:
    GetVisibleMissionTrackUserIdsUseCase,

    private val getUserByIdUseCase:
    GetUserByIdUseCase,

    private val updateMissionParticipantStatusUseCase:
    UpdateMissionParticipantStatusUseCase,

    private val finishMissionUseCase:
    FinishMissionUseCase,

    private val snackbarManager:
    IdeSnackbarManager,

    private val observeMissionEncountersUseCase:
    ObserveMissionEncountersUseCase,

    private val missionLocationServiceController:
    MissionLocationServiceController

) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MissionExecutionUiState()
        )

    val uiState:
            StateFlow<MissionExecutionUiState> =
        _uiState.asStateFlow()

    private var loadedMissionId:
            String? =
        null

    private var loadedMission:
            Mission? =
        null

    private var timerJob:
            Job? =
        null

    private var participantsJob:
            Job? =
        null

    private var encountersJob:
            Job? =
        null

    private val trackJobs =
        mutableMapOf<String, Job>()

    private val trackPointsByUser =
        mutableMapOf<
                String,
                List<MissionTrackPoint>
                >()

    private var currentUserProfile:
            UserProfile? =
        null

    private var userProfilesById =
        emptyMap<
                String,
                UserProfile
                >()

    private var visibilitySignature:
            String? =
        null


    private var currentUserId:
            String? =
        null

    /*
     * Diferencia uma saída solicitada nesta execução de uma
     * tentativa futura de entrar com status FINISHED.
     *
     * Não deve ser consumido junto com participationEnded, pois o
     * listener do Firestore ainda pode emitir uma última atualização
     * enquanto a tela está sendo removida da pilha de navegação.
     */
    private var participationEndRequestedInThisSession =
        false

    private var missionCompletionHandled =
        false

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

        if (
            loadedMissionId !=
            missionId
        ) {
            stopParticipantsObservation()
            stopEncounterObservation()
            stopTrackObservations()

            visibilitySignature =
                null

            loadedMission =
                null

            participationEndRequestedInThisSession =
                false

            missionCompletionHandled =
                false

            _uiState.update {
                it.copy(
                    participants =
                        emptyList(),
                    participantMarkers =
                        emptyList(),
                    encounterMarkers =
                        emptyList(),
                    encounters =
                        emptyList(),
                    participantTracks =
                        emptyList(),
                    mapGroupFilters =
                        emptyList(),
                    selectedMapGroupIds =
                        emptySet(),
                    includeUngroupedOnMap =
                        false,
                    isMapGroupFilterActive =
                        false,
                    encounterCount =
                        0,
                    personalMetrics =
                        MissionPersonalMetrics(),
                    generalMetrics =
                        MissionGeneralMetrics(),
                    canViewGeneralMetrics =
                        false,
                    canFinishMission =
                        false,
                    isSupportRequested =
                        false,
                    isCurrentUserSupport =
                        false,
                    isUpdatingSupportStatus =
                        false,
                    isEndingParticipation =
                        false,
                    participationEnded =
                        false,
                    participationAccessDenied =
                        false,
                    isFinishing =
                        false,
                    missionFinished =
                        false,
                    endParticipationErrorMessage =
                        null,
                    usesGeneralGroup =
                        false,
                    currentGroupId =
                        null,
                    currentGroupName =
                        null,
                    currentGroupColorHex =
                        null,
                    currentGroupMembers =
                        emptyList(),
                    customActivityName =
                        null,
                    availableActivities =
                        emptySet()
                )
            }
        }

        loadedMissionId =
            missionId

        viewModelScope.launch {

            /*
             * Mantém as permissões já conhecidas enquanto atualizamos
             * a mesma missão. Zerá-las aqui fazia o menu superior
             * desaparecer por alguns instantes a cada ON_RESUME.
             * Ao trocar de missão elas já são limpas no bloco acima.
             */
            _uiState.update {
                it.copy(
                    isLoading =
                        true,
                    errorMessage =
                        null
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

                    stopTimer()
                    stopParticipantsObservation()
                    stopEncounterObservation()
                    stopTrackObservations()
                    stopLocationTracking()

                    loadedMission =
                        null

                    _uiState.update {
                        it.copy(
                            isLoading =
                                false,
                            errorMessage =
                                R.string
                                    .mission_execution_loading_error
                        )
                    }

                    return@launch
                }

                if (
                    mission.status !=
                    MissionStatus.IN_PROGRESS
                ) {

                    stopTimer()
                    stopParticipantsObservation()
                    stopEncounterObservation()
                    stopTrackObservations()
                    stopLocationTracking()

                    loadedMission =
                        null

                    _uiState.update {
                        it.copy(
                            missionId =
                                mission.id,
                            missionName =
                                mission.name,
                            customActivityName =
                                mission.customActivityName,
                            availableActivities =
                                mission.activities.toSet(),
                            participants =
                                emptyList(),
                            participantMarkers =
                                emptyList(),
                            encounterMarkers =
                                emptyList(),
                            encounters =
                                emptyList(),
                            encounterCount =
                                0,
                            personalMetrics =
                                MissionPersonalMetrics(),
                            generalMetrics =
                                MissionGeneralMetrics(),
                            isLoading =
                                false,
                            errorMessage =
                                R.string
                                    .mission_execution_not_in_progress
                        )
                    }

                    return@launch
                }

                loadedMission =
                    mission

                val currentUser =
                    getCurrentUserProfileUseCase()
                        .getOrNull()

                currentUserId =
                    currentUser?.id

                currentUserProfile =
                    currentUser

                /*
                 * Mantém os perfis já carregados em cache.
                 *
                 * Antes, cada novo load() substituía o cache inteiro
                 * por um mapa contendo apenas o usuário atual. Isso fazia
                 * os nomes dos demais participantes sumirem temporariamente
                 * até serem buscados novamente.
                 */
                if (
                    currentUser != null
                ) {

                    userProfilesById =
                        userProfilesById +
                                (
                                        currentUser.id to
                                                currentUser
                                        )
                }

                val canManageMission =
                    currentUser
                        ?.role
                        ?.hasAtLeast(
                            UserRole.LEADER
                        )
                        ?: false

                _uiState.update { state ->
                    state.copy(

                        missionId =
                            mission.id,

                        missionName =
                            mission.name,

                        customActivityName =
                            mission.customActivityName,

                        availableActivities =
                            mission.activities.toSet(),

                        departureLatitude =
                            mission
                                .departureLocation
                                ?.latitude,

                        departureLongitude =
                            mission
                                .departureLocation
                                ?.longitude,

                        areaPoints =
                            mission
                                .area
                                ?.polygonPoints
                                ?: emptyList(),

                        usesGeneralGroup =
                            state.participants
                                .any {
                                    it.groupId == null
                                },

                        groupCount =
                            effectiveGroupCount(
                                mission =
                                    mission,
                                participants =
                                    state.participants
                            ),

                        mapGroupFilters =
                            buildMapGroupFilters(
                                mission =
                                    mission,
                                participants =
                                    state.participants
                            ),

                        selectedMapGroupIds =
                            state
                                .selectedMapGroupIds
                                .intersect(
                                    buildMapGroupFilters(
                                        mission =
                                            mission,
                                        participants =
                                            state.participants
                                    )
                                        .map {
                                            it.id
                                        }
                                        .toSet()
                                ),

                        canViewGeneralMetrics =
                            canManageMission,

                        canFinishMission =
                            canManageMission,

                        /*
                         * Caso o listener já tenha emitido
                         * participantes durante um refresh,
                         * recalculamos os marcadores com os
                         * grupos recém-carregados.
                         */
                        participantMarkers =
                            buildParticipantMarkers(
                                mission =
                                    mission,
                                participants =
                                    state.participants
                            ),

                        currentGroupId =
                            resolveCurrentGroupId(
                                mission =
                                    mission,
                                participants =
                                    state.participants
                            ),

                        currentGroupName =
                            resolveCurrentGroupName(
                                mission =
                                    mission,
                                participants =
                                    state.participants
                            ),

                        currentGroupColorHex =
                            resolveCurrentGroupColorHex(
                                mission =
                                    mission,
                                participants =
                                    state.participants
                            ),

                        currentGroupMembers =
                            buildCurrentGroupMembers(
                                mission =
                                    mission,
                                participants =
                                    state.participants
                            ),

                        isLoading =
                            false,

                        errorMessage =
                            null
                    )
                }

                refreshPersonalMetrics()
                refreshGeneralMetrics()

                startTimer(
                    startedAt =
                        mission.startedAt
                )

                startParticipantsObservation(
                    missionId =
                        mission.id
                )

                startEncountersObservation(
                    missionId =
                        mission.id
                )

            } catch (
                exception: Exception
            ) {

                stopTimer()
                stopParticipantsObservation()
                stopEncounterObservation()
                stopTrackObservations()
                stopLocationTracking()

                loadedMission =
                    null

                _uiState.update {
                    it.copy(
                        isLoading =
                            false,
                        errorMessage =
                            R.string
                                .mission_execution_loading_error
                    )
                }
            }
        }
    }

    // =========================================================
    // Atualização
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
    // Filtro de grupos no mapa
    // =========================================================

    fun applyMapGroupFilter(
        selectedGroupIds: Set<String>,
        includeUngrouped: Boolean
    ) {

        _uiState.update { state ->

            val availableGroupIds =
                state
                    .mapGroupFilters
                    .map {
                        it.id
                    }
                    .toSet()

            val normalizedSelection =
                selectedGroupIds
                    .intersect(
                        availableGroupIds
                    )

            /*
             * groupId == null nunca é tratado como "sem grupo" na UI:
             * esses participantes pertencem ao Grupo Geral lógico.
             */
            val normalizedIncludeUngrouped =
                false

            val showsEverything =
                normalizedSelection ==
                        availableGroupIds

            state.copy(
                selectedMapGroupIds =
                    if (
                        showsEverything
                    ) {
                        emptySet()
                    } else {
                        normalizedSelection
                    },
                includeUngroupedOnMap =
                    normalizedIncludeUngrouped,
                isMapGroupFilterActive =
                    !showsEverything
            )
        }
    }

    fun clearMapGroupFilter() {

        _uiState.update {
            it.copy(
                selectedMapGroupIds =
                    emptySet(),
                includeUngroupedOnMap =
                    false,
                isMapGroupFilterActive =
                    false
            )
        }
    }

    // =========================================================
    // Participantes em tempo real
    // =========================================================

    private fun startParticipantsObservation(
        missionId: String
    ) {

        stopParticipantsObservation()

        participantsJob =
            viewModelScope.launch {

                observeMissionParticipantsUseCase(
                    missionId =
                        missionId
                )
                    .catch {

                        _uiState.update {
                            it.copy(
                                errorMessage =
                                    R.string
                                        .mission_execution_loading_error
                            )
                        }
                    }
                    .collect { participants ->

                        loadParticipantProfiles(
                            userIds =
                                participants
                                    .map {
                                        it.userId
                                    }
                                    .toSet()
                        )

                        val mission =
                            loadedMission

                        val currentParticipant =
                            currentUserId
                                ?.let { userId ->
                                    participants
                                        .firstOrNull {
                                            it.userId ==
                                                    userId
                                        }
                                }

                        val finishedByMission =
                            currentParticipant
                                ?.status ==
                                    MissionParticipantStatus.FINISHED &&
                                    currentParticipant
                                        ?.endedByMission ==
                                    true

                        if (
                            finishedByMission
                        ) {
                            handleMissionFinished()
                        }

                        val accessDenied =
                            currentParticipant
                                ?.status ==
                                    MissionParticipantStatus.FINISHED &&
                                    currentParticipant
                                        ?.endedByMission !=
                                    true &&
                                    !_uiState.value.isEndingParticipation &&
                                    !_uiState.value.participationEnded &&
                                    !participationEndRequestedInThisSession

                        if (
                            accessDenied &&
                            !_uiState.value.participationAccessDenied
                        ) {
                            missionLocationServiceController.stop()

                            snackbarManager.show(
                                IdeSnackbarMessage(
                                    messageRes =
                                        R.string
                                            .home_participation_finished,
                                    type =
                                        IdeSnackbarType.WARNING
                                )
                            )
                        }

                        _uiState.update {
                            it.copy(
                                participants =
                                    participants,

                                currentLatitude =
                                    currentParticipant
                                        ?.latitude,

                                currentLongitude =
                                    currentParticipant
                                        ?.longitude,

                                isSupportRequested =
                                    currentParticipant
                                        ?.status ==
                                            MissionParticipantStatus
                                                .NEEDS_SUPPORT,

                                isCurrentUserSupport =
                                    currentParticipant
                                        ?.isSupport ==
                                            true,

                                participationAccessDenied =
                                    accessDenied,

                                participantMarkers =
                                    if (
                                        mission != null
                                    ) {
                                        buildParticipantMarkers(
                                            mission =
                                                mission,
                                            participants =
                                                participants
                                        )
                                    } else {
                                        emptyList()
                                    },

                                mapGroupFilters =
                                    if (
                                        mission != null
                                    ) {
                                        buildMapGroupFilters(
                                            mission =
                                                mission,
                                            participants =
                                                participants
                                        )
                                    } else {
                                        emptyList()
                                    },

                                usesGeneralGroup =
                                    participants
                                        .any {
                                            it.groupId == null
                                        },

                                groupCount =
                                    if (
                                        mission != null
                                    ) {
                                        effectiveGroupCount(
                                            mission =
                                                mission,
                                            participants =
                                                participants
                                        )
                                    } else {
                                        0
                                    },

                                currentGroupId =
                                    if (
                                        mission != null
                                    ) {
                                        resolveCurrentGroupId(
                                            mission =
                                                mission,
                                            participants =
                                                participants
                                        )
                                    } else {
                                        null
                                    },

                                currentGroupName =
                                    if (
                                        mission != null
                                    ) {
                                        resolveCurrentGroupName(
                                            mission =
                                                mission,
                                            participants =
                                                participants
                                        )
                                    } else {
                                        null
                                    },

                                currentGroupColorHex =
                                    if (
                                        mission != null
                                    ) {
                                        resolveCurrentGroupColorHex(
                                            mission =
                                                mission,
                                            participants =
                                                participants
                                        )
                                    } else {
                                        null
                                    },

                                currentGroupMembers =
                                    if (
                                        mission != null
                                    ) {
                                        buildCurrentGroupMembers(
                                            mission =
                                                mission,
                                            participants =
                                                participants
                                        )
                                    } else {
                                        emptyList()
                                    },

                                errorMessage =
                                    null
                            )
                        }

                        refreshPersonalMetrics()
                        refreshGeneralMetrics()

                        if (
                            mission != null
                        ) {
                            updateTrackVisibility(
                                mission =
                                    mission,
                                participants =
                                    participants
                            )
                        }
                    }
            }
    }

    private fun stopParticipantsObservation() {

        participantsJob
            ?.cancel()

        participantsJob =
            null
    }

    // =========================================================
    // Encontros em tempo real
    // =========================================================

    private fun startEncountersObservation(
        missionId: String
    ) {

        stopEncounterObservation()

        encountersJob =
            viewModelScope.launch {

                observeMissionEncountersUseCase(
                    missionId =
                        missionId
                )
                    .catch {
                        /*
                         * Um erro no listener de encontros não deve
                         * derrubar a execução da missão nem apagar
                         * os demais elementos do mapa.
                         */
                    }
                    .collect { encounters ->

                        val markers =
                            buildEncounterMarkers(
                                mission =
                                    loadedMission,
                                encounters =
                                    encounters
                            )

                        _uiState.update {
                            it.copy(
                                encounterMarkers =
                                    markers,
                                encounters =
                                    encounters,
                                encounterCount =
                                    encounters.size
                            )
                        }

                        refreshPersonalMetrics()
                        refreshGeneralMetrics()
                    }
            }
    }

    private fun stopEncounterObservation() {

        encountersJob
            ?.cancel()

        encountersJob =
            null
    }

    private fun buildEncounterMarkers(
        mission: Mission?,
        encounters: List<MissionEncounter>
    ): List<MissionEncounterMarker> {

        val groupsById =
            mission
                ?.groups
                ?.associateBy {
                    it.id
                }
                .orEmpty()

        return encounters
            .asSequence()
            .mapNotNull { encounter ->

                val latitude =
                    encounter.latitude

                val longitude =
                    encounter.longitude

                if (
                    latitude == null ||
                    longitude == null
                ) {
                    return@mapNotNull null
                }

                val belongsToGeneralGroup =
                    encounter.groupId == null

                val colorHex =
                    if (
                        belongsToGeneralGroup
                    ) {
                        GENERAL_GROUP_COLOR
                    } else {
                        encounter.groupColorHex
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: encounter.groupId
                                ?.let(
                                    groupsById::get
                                )
                                ?.colorHex
                            ?: UNGROUPED_PARTICIPANT_COLOR
                    }

                MissionEncounterMarker(
                    encounterId =
                        encounter.id,
                    registeredByUserId =
                        encounter.registeredByUserId,
                    groupId =
                        encounter.groupId
                            ?: GENERAL_GROUP_ID,
                    colorHex =
                        colorHex,
                    latitude =
                        latitude,
                    longitude =
                        longitude,
                    personName =
                        encounter.personName
                )
            }
            .sortedBy {
                it.encounterId
            }
            .toList()
    }

    /*
     * Carrega somente os perfis realmente presentes na missão.
     *
     * Isso evita depender de GetUsersUseCase, que pode retornar
     * uma lista vazia dependendo do escopo/permissão do usuário.
     * Os resultados ficam em cache durante a execução.
     */
    private suspend fun loadParticipantProfiles(
        userIds: Set<String>
    ) {

        val missingUserIds =
            userIds
                .filterNot {
                    userProfilesById
                        .containsKey(
                            it
                        )
                }

        if (
            missingUserIds.isEmpty()
        ) {
            return
        }

        val loadedProfiles =
            mutableMapOf<
                    String,
                    UserProfile
                    >()

        missingUserIds
            .forEach { userId ->

                val result =
                    getUserByIdUseCase(
                        userId =
                            userId
                    )

                result
                    .getOrNull()
                    ?.let { profile ->

                        loadedProfiles[
                            profile.id
                        ] =
                            profile
                    }
            }

        if (
            loadedProfiles.isNotEmpty()
        ) {

            userProfilesById =
                userProfilesById +
                        loadedProfiles
        }
    }

    // =========================================================
    // Participantes -> marcadores do mapa
    // =========================================================

    private fun buildParticipantMarkers(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>
    ): List<MissionParticipantMarker> {

        val groupsById =
            mission
                .groups
                .associateBy {
                    it.id
                }

        return participants
            .asSequence()
            .filter {
                it.status !=
                        MissionParticipantStatus.FINISHED
            }
            .mapNotNull { participant ->

                val latitude =
                    participant.latitude

                val longitude =
                    participant.longitude

                if (
                    latitude == null ||
                    longitude == null
                ) {
                    return@mapNotNull null
                }

                val group =
                    participant.groupId
                        ?.let(
                            groupsById::get
                        )

                MissionParticipantMarker(
                    userId =
                        participant.userId,
                    latitude =
                        latitude,
                    longitude =
                        longitude,
                    groupId =
                        participant.groupId
                            ?: GENERAL_GROUP_ID,
                    groupName =
                        if (
                            participant.groupId == null
                        ) {
                            GENERAL_GROUP_NAME
                        } else {
                            group?.name
                        },
                    colorHex =
                        if (
                            participant.groupId == null
                        ) {
                            GENERAL_GROUP_COLOR
                        } else {
                            group?.colorHex
                                ?: UNGROUPED_PARTICIPANT_COLOR
                        },
                    status =
                        participant.status,
                    isSupport =
                        participant.isSupport,
                    isCurrentUser =
                        participant.userId ==
                                currentUserId,
                    displayName =
                        userProfilesById[
                            participant.userId
                        ]
                            ?.let { profile ->
                                listOf(
                                    profile.firstName,
                                    profile.lastName
                                )
                                    .filter {
                                        it.isNotBlank()
                                    }
                                    .joinToString(
                                        separator =
                                            " "
                                    )
                            }
                            .orEmpty(),
                    role =
                        userProfilesById[
                            participant.userId
                        ]
                            ?.role
                )
            }
            .toList()
    }

    private fun buildMapGroupFilters(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>
    ): List<MissionMapGroupFilterUiModel> {

        val activeParticipants =
            participants
                .filter {
                    it.status !=
                            MissionParticipantStatus.FINISHED
                }

        val activeParticipantCountByGroup =
            activeParticipants
                .asSequence()
                .mapNotNull {
                    it.groupId
                }
                .groupingBy {
                    it
                }
                .eachCount()

        val configuredGroups =
            mission
                .groups
                .map { group ->
                    MissionMapGroupFilterUiModel(
                        id =
                            group.id,
                        name =
                            group.name,
                        colorHex =
                            group.colorHex,
                        participantCount =
                            activeParticipantCountByGroup[
                                group.id
                            ] ?: 0
                    )
                }
                .sortedBy {
                    it.name.lowercase()
                }

        val hasGeneralGroup =
            participants
                .any {
                    it.groupId == null
                }

        if (
            !hasGeneralGroup
        ) {
            return configuredGroups
        }

        val generalGroup =
            MissionMapGroupFilterUiModel(
                id =
                    GENERAL_GROUP_ID,
                name =
                    GENERAL_GROUP_NAME,
                colorHex =
                    GENERAL_GROUP_COLOR,
                participantCount =
                    activeParticipants
                        .count {
                            it.groupId == null
                        }
            )

        return configuredGroups +
                generalGroup
    }

    private fun buildCurrentGroupMembers(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>
    ): List<MissionGroupMemberUiModel> {

        val userId =
            currentUserId
                ?: return emptyList()

        val currentParticipant =
            participants
                .firstOrNull {
                    it.userId ==
                            userId
                }
                ?: return emptyList()

        val participantsById =
            participants
                .associateBy {
                    it.userId
                }

        val missionGroup =
            currentParticipant
                .groupId
                ?.let { groupId ->
                    mission.groups
                        .firstOrNull {
                            it.id ==
                                    groupId
                        }
                }

        val orderedUserIds =
            if (
                currentParticipant.groupId == null
            ) {
                /*
                 * Todo participante com groupId == null pertence ao mesmo
                 * Grupo Geral, exista ou não outro grupo configurado.
                 */
                participants
                    .asSequence()
                    .filter {
                        it.groupId == null
                    }
                    .map {
                        it.userId
                    }
                    .distinct()
                    .toList()
            } else {
                val group =
                    missionGroup
                        ?: return emptyList()

                buildList {

                    group.participantIds
                        .forEach { participantId ->

                            if (
                                participantId !in this
                            ) {
                                add(
                                    participantId
                                )
                            }
                        }

                    group.supportUserId
                        ?.let { supportUserId ->

                            if (
                                supportUserId !in this
                            ) {
                                add(
                                    supportUserId
                                )
                            }
                        }

                    participants
                        .filter {
                            it.groupId ==
                                    group.id
                        }
                        .forEach { participant ->

                            if (
                                participant.userId !in this
                            ) {
                                add(
                                    participant.userId
                                )
                            }
                        }
                }
            }

        return orderedUserIds
            .mapNotNull { participantId ->

                val participant =
                    participantsById[
                        participantId
                    ]
                        ?: return@mapNotNull null

                val profile =
                    userProfilesById[
                        participantId
                    ]

                MissionGroupMemberUiModel(
                    userId =
                        participantId,
                    displayName =
                        profile
                            ?.let {
                                listOf(
                                    it.firstName,
                                    it.lastName
                                )
                                    .filter(
                                        String::isNotBlank
                                    )
                                    .joinToString(
                                        separator =
                                            " "
                                    )
                            }
                            .orEmpty(),
                    role =
                        profile?.role,
                    status =
                        participant.status,
                    isSupport =
                        participant.isSupport ||
                                missionGroup
                                    ?.supportUserId ==
                                participantId,
                    isCurrentUser =
                        participantId ==
                                currentUserId,
                    hasMapPosition =
                        participant.latitude != null &&
                                participant.longitude != null &&
                                participant.status !=
                                MissionParticipantStatus.FINISHED
                )
            }
            .sortedWith(
                compareByDescending<MissionGroupMemberUiModel> {
                    it.isCurrentUser
                }
                    .thenByDescending {
                        it.isSupport
                    }
                    .thenBy {
                        it.displayName
                            .lowercase()
                    }
            )
    }

    private fun effectiveGroupCount(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>
    ): Int {

        val hasGeneralGroup =
            participants
                .any {
                    it.groupId == null
                }

        return mission.groups.size +
                if (
                    hasGeneralGroup
                ) {
                    1
                } else {
                    0
                }
    }

    private fun resolveCurrentGroupId(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>
    ): String? {

        val participant =
            currentUserId
                ?.let { userId ->
                    participants
                        .firstOrNull {
                            it.userId ==
                                    userId
                        }
                }
                ?: return null

        return participant.groupId
            ?: GENERAL_GROUP_ID
    }

    private fun resolveCurrentGroupName(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>
    ): String? {

        val groupId =
            resolveCurrentGroupId(
                mission =
                    mission,
                participants =
                    participants
            )
                ?: return null

        return if (
            groupId ==
            GENERAL_GROUP_ID
        ) {
            GENERAL_GROUP_NAME
        } else {
            mission.groups
                .firstOrNull {
                    it.id ==
                            groupId
                }
                ?.name
        }
    }

    private fun resolveCurrentGroupColorHex(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>
    ): String? {

        val groupId =
            resolveCurrentGroupId(
                mission =
                    mission,
                participants =
                    participants
            )
                ?: return null

        return if (
            groupId ==
            GENERAL_GROUP_ID
        ) {
            GENERAL_GROUP_COLOR
        } else {
            mission.groups
                .firstOrNull {
                    it.id ==
                            groupId
                }
                ?.colorHex
        }
    }

    // =========================================================
    // Pedido de apoio
    // =========================================================

    fun requestSupport() {

        updateCurrentParticipantStatus(
            status =
                MissionParticipantStatus
                    .NEEDS_SUPPORT
        )
    }

    fun confirmSupportReceived() {

        updateCurrentParticipantStatus(
            status =
                MissionParticipantStatus
                    .ACTIVE
        )
    }

    // =========================================================
    // Encerramento da própria participação
    // =========================================================

    fun endParticipation() {

        val missionId =
            loadedMissionId
                ?: return

        val userId =
            currentUserId
                ?: return

        val state =
            _uiState.value

        if (
            state.isEndingParticipation ||
            state.isUpdatingSupportStatus
        ) {
            return
        }

        participationEndRequestedInThisSession =
            true

        val alreadyFinished =
            state
                .participants
                .firstOrNull {
                    it.userId ==
                            userId
                }
                ?.status ==
                    MissionParticipantStatus.FINISHED

        if (
            alreadyFinished
        ) {
            stopLocationTracking()
            stopTimer()

            _uiState.update {
                it.copy(
                    participationEnded =
                        true
                )
            }

            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isEndingParticipation =
                        true,
                    endParticipationErrorMessage =
                        null
                )
            }

            val result =
                updateMissionParticipantStatusUseCase(
                    missionId =
                        missionId,
                    userId =
                        userId,
                    status =
                        MissionParticipantStatus.FINISHED
                )

            if (
                result.isSuccess
            ) {
                stopLocationTracking()
                stopTimer()

                _uiState.update {
                    it.copy(
                        participationEnded =
                            true
                    )
                }
            } else {
                participationEndRequestedInThisSession =
                    false

                _uiState.update {
                    it.copy(
                        isEndingParticipation =
                            false,
                        endParticipationErrorMessage =
                            R.string
                                .mission_end_participation_error
                    )
                }
            }
        }
    }

    fun consumeParticipationEnded() {
        _uiState.update {
            it.copy(
                participationEnded =
                    false
            )
        }
    }

    fun consumeParticipationAccessDenied() {
        _uiState.update {
            it.copy(
                participationAccessDenied =
                    false
            )
        }
    }

    fun consumeEndParticipationError() {
        _uiState.update {
            it.copy(
                endParticipationErrorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Finalização da missão
    // =========================================================

    fun finishMission() {

        val missionId =
            loadedMissionId
                ?: return

        val state =
            _uiState.value

        if (
            !state.canFinishMission ||
            state.isFinishing ||
            missionCompletionHandled
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isFinishing =
                        true
                )
            }

            val result =
                finishMissionUseCase(
                    missionId =
                        missionId
                )

            if (
                result.isSuccess
            ) {
                handleMissionFinished()
            } else {
                _uiState.update {
                    it.copy(
                        isFinishing =
                            false
                    )
                }

                snackbarManager.show(
                    IdeSnackbarMessage(
                        messageRes =
                            R.string
                                .mission_finish_error,
                        type =
                            IdeSnackbarType.ERROR
                    )
                )
            }
        }
    }

    private suspend fun handleMissionFinished() {

        if (
            missionCompletionHandled
        ) {
            return
        }

        missionCompletionHandled =
            true

        stopLocationTracking()
        stopTimer()
        stopEncounterObservation()
        stopTrackObservations()

        /*
         * Publica a navegação antes da snackbar. O show() global é
         * suspenso e não pode impedir missionFinished de chegar à tela.
         */
        _uiState.update {
            it.copy(
                isFinishing =
                    true,
                missionFinished =
                    true
            )
        }

        snackbarManager.show(
            IdeSnackbarMessage(
                messageRes =
                    R.string
                        .mission_finish_success,
                type =
                    IdeSnackbarType.SUCCESS
            )
        )
    }

    fun consumeMissionFinished() {
        _uiState.update {
            it.copy(
                missionFinished =
                    false
            )
        }
    }

    private fun updateCurrentParticipantStatus(
        status: MissionParticipantStatus
    ) {

        val missionId =
            loadedMissionId
                ?: return

        val userId =
            currentUserId
                ?: return

        if (
            _uiState
                .value
                .let {
                    it.isUpdatingSupportStatus ||
                            it.isEndingParticipation
                }
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isUpdatingSupportStatus =
                        true
                )
            }

            val result =
                updateMissionParticipantStatusUseCase(
                    missionId =
                        missionId,
                    userId =
                        userId,
                    status =
                        status
                )

            _uiState.update {
                it.copy(
                    isUpdatingSupportStatus =
                        false
                )
            }

            /*
             * O listener em tempo real de participants
             * atualiza isSupportRequested e o marcador.
             *
             * Em caso de falha, mantemos o estado visual
             * atual em vez de antecipar uma alteração local.
             */
            if (
                result.isFailure
            ) {
                return@launch
            }
        }
    }

    // =========================================================
    // Serviço de localização da missão
    // =========================================================

    fun startLocationTracking() {

        val missionId =
            loadedMissionId
                ?: return

        val userId =
            currentUserId
                ?: return

        val participationFinished =
            _uiState
                .value
                .participants
                .firstOrNull {
                    it.userId ==
                            userId
                }
                ?.status ==
                    MissionParticipantStatus.FINISHED

        if (
            participationFinished
        ) {
            return
        }

        missionLocationServiceController.start(
            missionId = missionId,
            userId = userId
        )
    }

    fun stopLocationTracking() {
        missionLocationServiceController.stop()
    }

    // =========================================================
    // Trajetos visíveis em tempo real
    // =========================================================

    private suspend fun updateTrackVisibility(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>
    ) {

        val currentUser =
            currentUserProfile
                ?: return

        /*
         * Latitude/longitude mudam a cada poucos segundos,
         * mas isso não altera quem pode ver quem.
         *
         * A assinatura usa apenas usuário + grupo, evitando
         * refazer consultas de igreja/distrito a cada update.
         */
        val signature =
            "configuredGroups=${mission.groups.isNotEmpty()}|" +
                    participants
                        .sortedBy {
                            it.userId
                        }
                        .joinToString(
                            separator =
                                "|"
                        ) {
                            "${it.userId}:${it.groupId.orEmpty()}"
                        }

        if (
            visibilitySignature ==
            signature
        ) {

            publishTrackLines(
                mission =
                    mission,
                participants =
                    participants
            )

            return
        }

        visibilitySignature =
            signature

        val visibleUserIds =
            getVisibleMissionTrackUserIdsUseCase(
                currentUser =
                    currentUser,
                participants =
                    participants
            )

        reconcileTrackObservers(
            mission =
                mission,
            participants =
                participants,
            visibleUserIds =
                visibleUserIds
        )
    }

    private fun reconcileTrackObservers(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>,
        visibleUserIds: Set<String>
    ) {

        val idsToRemove =
            trackJobs
                .keys
                .filter {
                    it !in
                            visibleUserIds
                }

        idsToRemove
            .forEach { userId ->

                trackJobs
                    .remove(
                        userId
                    )
                    ?.cancel()

                trackPointsByUser
                    .remove(
                        userId
                    )
            }

        visibleUserIds
            .filter {
                it !in
                        trackJobs
            }
            .forEach { userId ->

                trackJobs[
                    userId
                ] =
                    viewModelScope.launch {

                        observeMissionTrackUseCase(
                            missionId =
                                mission.id,
                            userId =
                                userId
                        )
                            .catch {
                                /*
                                 * Um erro em um percurso não
                                 * derruba os demais listeners.
                                 */
                            }
                            .collect { points ->

                                trackPointsByUser[
                                    userId
                                ] =
                                    points

                                publishTrackLines(
                                    mission =
                                        mission,
                                    participants =
                                        _uiState
                                            .value
                                            .participants
                                )

                                refreshPersonalMetrics()
                                refreshGeneralMetrics()
                            }
                    }
            }

        publishTrackLines(
            mission =
                mission,
            participants =
                participants
        )

        refreshGeneralMetrics()
    }

    private fun publishTrackLines(
        mission: Mission,
        participants:
        List<br.com.ide.domain.model.MissionParticipantState>
    ) {

        val groupsById =
            mission
                .groups
                .associateBy {
                    it.id
                }

        val participantsById =
            participants
                .associateBy {
                    it.userId
                }

        val lines =
            trackPointsByUser
                .mapNotNull {
                        (
                            userId,
                            points
                        ) ->

                    if (
                        points.size <
                        2
                    ) {
                        return@mapNotNull null
                    }

                    val participant =
                        participantsById[
                            userId
                        ]

                    val storedGroupId =
                        participant
                            ?.groupId

                    val group =
                        storedGroupId
                            ?.let(
                                groupsById::get
                            )

                    MissionTrackLine(
                        userId =
                            userId,
                        groupId =
                            storedGroupId
                                ?: GENERAL_GROUP_ID,
                        colorHex =
                            if (
                                storedGroupId == null
                            ) {
                                GENERAL_GROUP_COLOR
                            } else {
                                group?.colorHex
                                    ?: UNGROUPED_PARTICIPANT_COLOR
                            },
                        points =
                            points
                                .map { point ->

                                    br.com.ide.domain.model
                                        .MissionCoordinate(
                                            latitude =
                                                point.latitude,
                                            longitude =
                                                point.longitude
                                        )
                                }
                    )
                }
                .sortedBy {
                    it.userId
                }

        _uiState.update {
            it.copy(
                participantTracks =
                    lines
            )
        }
    }

    private fun stopTrackObservations() {

        trackJobs
            .values
            .forEach(
                Job::cancel
            )

        trackJobs.clear()
        trackPointsByUser.clear()

        _uiState.update {
            it.copy(
                participantTracks =
                    emptyList()
            )
        }
    }

    // =========================================================
    // Métricas pessoais
    // =========================================================

    private fun refreshPersonalMetrics(
        now: LocalDateTime =
            LocalDateTime.now()
    ) {

        val userId =
            currentUserId
                ?: run {

                    _uiState.update {
                        it.copy(
                            personalMetrics =
                                MissionPersonalMetrics()
                        )
                    }

                    return
                }

        val state =
            _uiState.value

        val currentParticipant =
            state
                .participants
                .firstOrNull {
                    it.userId ==
                            userId
                }

        val currentUserTrack =
            trackPointsByUser[
                userId
            ]
                .orEmpty()
                .map { point ->
                    MissionCoordinate(
                        latitude =
                            point.latitude,
                        longitude =
                            point.longitude
                    )
                }

        val metrics =
            calculateMissionPersonalMetricsUseCase(
                currentUserId =
                    userId,
                encounters =
                    state.encounters,
                participant =
                    currentParticipant,
                trackPoints =
                    currentUserTrack,
                now =
                    now
            )

        _uiState.update {
            it.copy(
                personalMetrics =
                    metrics
            )
        }
    }

    // =========================================================
    // Métricas gerais
    // =========================================================

    private fun refreshGeneralMetrics(
        now: LocalDateTime =
            LocalDateTime.now()
    ) {

        val state =
            _uiState.value

        /*
         * Somente líder, pastor e administrador podem receber
         * os totais consolidados da missão. Missionários mantêm
         * apenas suas métricas pessoais no estado da tela.
         */
        if (
            !state.canViewGeneralMetrics
        ) {

            if (
                state.generalMetrics !=
                MissionGeneralMetrics()
            ) {
                _uiState.update {
                    it.copy(
                        generalMetrics =
                            MissionGeneralMetrics()
                    )
                }
            }

            return
        }

        val metrics =
            calculateMissionGeneralMetricsUseCase(
                participants =
                    state.participants,
                encounters =
                    state.encounters,
                trackPointsByUser =
                    trackPointsByUser
                        .mapValues {
                                (
                                    _,
                                    points
                                ) ->

                            points.toList()
                        },
                groupCount =
                    state.groupCount,
                now =
                    now
            )

        _uiState.update {
            it.copy(
                generalMetrics =
                    metrics
            )
        }
    }

    // =========================================================
    // Cronômetro
    // =========================================================

    private fun startTimer(
        startedAt: LocalDateTime?
    ) {

        stopTimer()

        if (
            startedAt == null
        ) {

            _uiState.update {
                it.copy(
                    elapsedSeconds =
                        0L
                )
            }

            return
        }

        timerJob =
            viewModelScope.launch {

                while (
                    isActive
                ) {

                    val elapsedSeconds =
                        Duration
                            .between(
                                startedAt,
                                LocalDateTime.now()
                            )
                            .seconds
                            .coerceAtLeast(
                                0L
                            )

                    _uiState.update {
                        it.copy(
                            elapsedSeconds =
                                elapsedSeconds
                        )
                    }

                    val now =
                        LocalDateTime.now()

                    refreshPersonalMetrics(
                        now =
                            now
                    )

                    refreshGeneralMetrics(
                        now =
                            now
                    )

                    delay(
                        1.seconds
                    )
                }
            }
    }

    private fun stopTimer() {

        timerJob
            ?.cancel()

        timerJob =
            null
    }

    override fun onCleared() {

        stopTimer()
        stopParticipantsObservation()
        stopEncounterObservation()
        stopTrackObservations()
    }

    private companion object {

        const val GENERAL_GROUP_ID =
            "__GENERAL__"

        const val GENERAL_GROUP_NAME =
            "Grupo Geral"

        /*
         * Cor lógica do Grupo Geral. Não é persistida como grupo real.
         */
        const val GENERAL_GROUP_COLOR =
            "#2A9D8F"

        /*
         * Cor de segurança para referências a grupos configurados que não
         * possam ser resolvidas. groupId == null usa sempre Grupo Geral.
         */
        const val UNGROUPED_PARTICIPANT_COLOR =
            "#8B8D98"
    }
}
