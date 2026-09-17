package br.com.ide.presentation.feature.missionexecution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.location.MissionLocationServiceController
import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.model.MissionTrackPoint
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetMissionByIdUseCase
import br.com.ide.domain.usecase.GetVisibleMissionTrackUserIdsUseCase
import br.com.ide.domain.usecase.GetUserByIdUseCase
import br.com.ide.domain.usecase.ObserveMissionParticipantsUseCase
import br.com.ide.domain.usecase.ObserveMissionTrackUseCase
import br.com.ide.domain.usecase.UpdateMissionParticipantStatusUseCase
import br.com.ide.presentation.components.map.MissionParticipantMarker
import br.com.ide.presentation.components.map.MissionTrackLine
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
            stopTrackObservations()

            visibilitySignature =
                null

            loadedMission =
                null

            _uiState.update {
                it.copy(
                    participants =
                        emptyList(),
                    participantMarkers =
                        emptyList(),
                    participantTracks =
                        emptyList(),
                    isSupportRequested =
                        false,
                    isCurrentUserSupport =
                        false,
                    isUpdatingSupportStatus =
                        false
                )
            }
        }

        loadedMissionId =
            missionId

        viewModelScope.launch {

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
                            participants =
                                emptyList(),
                            participantMarkers =
                                emptyList(),
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

                val canFinishMission =
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

                        groupCount =
                            mission
                                .groups
                                .size,

                        canFinishMission =
                            canFinishMission,

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

                        isLoading =
                            false,

                        errorMessage =
                            null
                    )
                }

                startTimer(
                    startedAt =
                        mission.startedAt
                )

                startParticipantsObservation(
                    missionId =
                        mission.id
                )

            } catch (
                exception: Exception
            ) {

                stopTimer()
                stopParticipantsObservation()
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

                                errorMessage =
                                    null
                            )
                        }

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

        val markers =
            participants
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
                            participant.groupId,

                        groupName =
                            group?.name,

                        colorHex =
                            group?.colorHex
                                ?: UNGROUPED_PARTICIPANT_COLOR,

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

        return markers
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
                .isUpdatingSupportStatus
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
                            }
                    }
            }

        publishTrackLines(
            mission =
                mission,
            participants =
                participants
        )
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

                    val groupId =
                        participant
                            ?.groupId

                    val group =
                        groupId
                            ?.let(
                                groupsById::get
                            )

                    MissionTrackLine(
                        userId =
                            userId,
                        groupId =
                            groupId,
                        colorHex =
                            group?.colorHex
                                ?: UNGROUPED_PARTICIPANT_COLOR,
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
        stopTrackObservations()
    }

    private companion object {

        /*
         * Cor reservada exclusivamente para participantes
         * que ainda não pertencem a nenhum grupo.
         *
         * Não deve ser oferecida na seleção de cores
         * dos grupos.
         */
        const val UNGROUPED_PARTICIPANT_COLOR =
            "#8B8D98"
    }
}
