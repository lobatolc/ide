package br.com.ide.presentation.feature.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionGeneralMetrics
import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.usecase.CalculateMissionGeneralMetricsUseCase
import br.com.ide.domain.usecase.GetMissionParticipantsUseCase
import br.com.ide.domain.usecase.GetMissionsUseCase
import br.com.ide.domain.usecase.GetUserByIdUseCase
import br.com.ide.domain.usecase.ObserveMissionEncountersUseCase
import br.com.ide.domain.usecase.ObserveMissionTrackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MetricsViewModel @Inject constructor(
    private val getMissionsUseCase: GetMissionsUseCase,
    private val getMissionParticipantsUseCase: GetMissionParticipantsUseCase,
    private val observeMissionEncountersUseCase: ObserveMissionEncountersUseCase,
    private val observeMissionTrackUseCase: ObserveMissionTrackUseCase,
    private val calculateMissionGeneralMetricsUseCase: CalculateMissionGeneralMetricsUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricsUiState())
    val uiState: StateFlow<MetricsUiState> = _uiState.asStateFlow()

    private var completedMissions: List<Mission> = emptyList()

    private data class MissionMetricsSnapshot(
        val participants: List<MissionParticipantState>,
        val encounters: List<MissionEncounter>,
        val metrics: MissionGeneralMetrics
    )

    private val missionSnapshotCache =
        mutableMapOf<String, MissionMetricsSnapshot>()

    private val userNameCache =
        mutableMapOf<String, String>()

    private var metricsJob: Job? = null

    init {
        load()
    }

    fun onEvent(event: MetricsEvent) {
        when (event) {
            MetricsEvent.Retry -> load()
            MetricsEvent.OpenSelector -> openSelector()
            MetricsEvent.DismissSelector -> dismissSelector()
            is MetricsEvent.SearchChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                refreshVisibleMissions()
            }
            is MetricsEvent.PeriodChanged -> changeDraftPeriod(event.period)
            is MetricsEvent.MissionToggled -> toggleMission(event.missionId)
            MetricsEvent.ApplySelection -> applySelection()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isCalculating = false,
                    errorMessage = null
                )
            }

            try {
                completedMissions =
                    getMissionsUseCase()
                        .filter { it.status == MissionStatus.COMPLETED }
                        .sortedByDescending(::missionReferenceDate)

                val models = completedMissions.map(::toUiModel)

                _uiState.update {
                    it.copy(
                        completedMissions = models,
                        visibleMissions = models,
                        selectedMissionIds = emptySet(),
                        draftSelectedMissionIds = emptySet(),
                        appliedPeriod = MetricsPeriodFilter.ALL_TIME,
                        draftPeriod = MetricsPeriodFilter.ALL_TIME,
                        searchQuery = "",
                        totalCompletedMissionCount = models.size,
                        periodMissionCount = models.size,
                        isLoading = false,
                        errorMessage = null
                    )
                }

            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCalculating = false,
                        errorMessage = R.string.metrics_loading_error
                    )
                }
            }
        }
    }

    private fun openSelector() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isSelectorOpen = true,
                draftSelectedMissionIds = state.selectedMissionIds,
                draftPeriod = state.appliedPeriod,
                searchQuery = ""
            )
        }
        refreshVisibleMissions()
    }

    private fun dismissSelector() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isSelectorOpen = false,
                draftSelectedMissionIds = state.selectedMissionIds,
                draftPeriod = state.appliedPeriod,
                searchQuery = ""
            )
        }
    }

    private fun changeDraftPeriod(period: MetricsPeriodFilter) {
        val periodIds = missionsForPeriod(period)
            .map { it.id }
            .toSet()

        _uiState.update { state ->
            state.copy(
                draftPeriod = period,
                draftSelectedMissionIds =
                    state.draftSelectedMissionIds
                        .filterTo(linkedSetOf()) { it in periodIds }
                        .take(METRICS_MAX_SELECTED_MISSIONS)
                        .toSet(),
                searchQuery = "",
                periodMissionCount = periodIds.size
            )
        }

        refreshVisibleMissions()
    }

    private fun toggleMission(missionId: String) {
        _uiState.update { state ->
            val selected =
                state.draftSelectedMissionIds.toMutableSet()

            if (missionId in selected) {
                selected.remove(missionId)
            } else if (
                selected.size <
                METRICS_MAX_SELECTED_MISSIONS
            ) {
                selected.add(missionId)
            }

            state.copy(
                draftSelectedMissionIds = selected
            )
        }
    }

    private fun applySelection() {
        val state = _uiState.value
        if (state.draftSelectedMissionIds.isEmpty()) {
            return
        }

        val selectedIds =
            state.draftSelectedMissionIds
                .take(METRICS_MAX_SELECTED_MISSIONS)
                .toSet()

        _uiState.update {
            it.copy(
                selectedMissionIds = selectedIds,
                appliedPeriod = state.draftPeriod,
                isSelectorOpen = false,
                searchQuery = ""
            )
        }

        recalculateSelectedMetrics()
    }

    private fun refreshVisibleMissions() {
        val state = _uiState.value
        val query = state.searchQuery.trim()
        val periodMissions = missionsForPeriod(state.draftPeriod)

        val visible = periodMissions
            .filter { mission ->
                query.isBlank() ||
                        mission.name.contains(query, ignoreCase = true)
            }
            .map(::toUiModel)

        _uiState.update {
            it.copy(
                visibleMissions = visible,
                periodMissionCount = periodMissions.size
            )
        }
    }

    private fun missionsForPeriod(period: MetricsPeriodFilter): List<Mission> {
        val today = LocalDate.now()

        return completedMissions.filter { mission ->
            val date = missionReferenceDate(mission).toLocalDate()

            when (period) {
                MetricsPeriodFilter.ALL_TIME -> true
                MetricsPeriodFilter.CURRENT_YEAR -> date.year == today.year
                MetricsPeriodFilter.LAST_30_DAYS ->
                    !date.isBefore(today.minusDays(29)) && !date.isAfter(today)
                MetricsPeriodFilter.LAST_90_DAYS ->
                    !date.isBefore(today.minusDays(89)) && !date.isAfter(today)
            }
        }
    }

    private fun recalculateSelectedMetrics() {
        val selectedIds = _uiState.value.selectedMissionIds

        metricsJob?.cancel()
        metricsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isCalculating = true, errorMessage = null)
            }

            try {
                val selectedMissions = completedMissions
                    .filter { it.id in selectedIds }

                val snapshots = selectedMissions.map { mission ->
                    mission to getOrLoadSnapshot(mission)
                }

                val summary = aggregate(snapshots)
                val dates = selectedMissions.map(::missionReferenceDate)

                _uiState.update {
                    it.copy(
                        summary = summary,
                        selectedStartDate = dates.minOrNull(),
                        selectedEndDate = dates.maxOrNull(),
                        isCalculating = false,
                        errorMessage = null
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isCalculating = false,
                        errorMessage = R.string.metrics_loading_error
                    )
                }
            }
        }
    }

    private suspend fun getOrLoadSnapshot(
        mission: Mission
    ): MissionMetricsSnapshot {
        missionSnapshotCache[mission.id]?.let { return it }

        val participants = getMissionParticipantsUseCase(
            missionId = mission.id
        ).getOrThrow()

        val encounters = observeMissionEncountersUseCase(
            missionId = mission.id
        ).first()

        val tracks = linkedMapOf<String, List<br.com.ide.domain.model.MissionTrackPoint>>()

        for (participant in participants) {
            tracks[participant.userId] = observeMissionTrackUseCase(
                missionId = mission.id,
                userId = participant.userId
            ).first()
        }

        val operationalGroupCount =
            if (mission.groups.isEmpty()) {
                if (participants.isEmpty()) 0 else 1
            } else {
                mission.groups.size
            }

        val metrics = calculateMissionGeneralMetricsUseCase(
            participants = participants,
            encounters = encounters,
            trackPointsByUser = tracks,
            groupCount = operationalGroupCount,
            now = mission.endedAt ?: missionReferenceDate(mission)
        )

        return MissionMetricsSnapshot(
            participants = participants,
            encounters = encounters,
            metrics = metrics
        ).also {
            missionSnapshotCache[mission.id] = it
        }
    }

    private fun aggregate(
        snapshots: List<Pair<Mission, MissionMetricsSnapshot>>
    ): MetricsSummary {
        val uniqueMissionaryIds = linkedSetOf<String>()
        var participationCount = 0
        var groupCount = 0
        var encounterCount = 0
        var visitCount = 0
        var prayerCount = 0
        var customActivityCount = 0
        var offeredBibleStudyCount = 0
        var acceptedBibleStudyCount = 0
        var deliveredMaterialCount = 0
        var completedSurveyCount = 0
        var distanceMeters = 0.0
        var participationSeconds = 0L

        snapshots.forEach { (_, snapshot) ->
            snapshot.participants.forEach { uniqueMissionaryIds += it.userId }
            participationCount += snapshot.participants.size

            val metrics = snapshot.metrics
            groupCount += metrics.groupCount
            encounterCount += metrics.encounterCount
            visitCount += metrics.visitCount
            prayerCount += metrics.prayerCount
            customActivityCount += metrics.customActivityCount
            offeredBibleStudyCount += metrics.offeredBibleStudyCount
            acceptedBibleStudyCount += metrics.acceptedBibleStudyCount
            deliveredMaterialCount += metrics.deliveredMaterialCount
            completedSurveyCount += metrics.completedSurveyCount
            distanceMeters += metrics.distanceMeters
            participationSeconds += metrics.participationSeconds
        }

        return MetricsSummary(
            missionCount = snapshots.size,
            uniqueMissionaryCount = uniqueMissionaryIds.size,
            participationCount = participationCount,
            groupCount = groupCount,
            encounterCount = encounterCount,
            visitCount = visitCount,
            prayerCount = prayerCount,
            customActivityCount = customActivityCount,
            offeredBibleStudyCount = offeredBibleStudyCount,
            acceptedBibleStudyCount = acceptedBibleStudyCount,
            deliveredMaterialCount = deliveredMaterialCount,
            completedSurveyCount = completedSurveyCount,
            distanceMeters = distanceMeters,
            participationSeconds = participationSeconds
        )
    }

    suspend fun buildExportSnapshot(): MetricsExportSnapshot {
        val selectedMissions = completedMissions
            .filter { it.id in _uiState.value.selectedMissionIds }

        val data = selectedMissions.map { mission ->
            val snapshot = getOrLoadSnapshot(mission)
            MetricsMissionExportData(
                mission = mission,
                participants = snapshot.participants,
                encounters = snapshot.encounters
            )
        }

        val userIds = buildSet {
            data.forEach { missionData ->
                missionData.participants.forEach { add(it.userId) }
                missionData.encounters.forEach { add(it.registeredByUserId) }
            }
        }

        for (userId in userIds) {
            if (userId in userNameCache) continue

            val profile = getUserByIdUseCase(userId = userId)
                .getOrNull()

            userNameCache[userId] = profile
                ?.let {
                    listOf(it.firstName, it.lastName)
                        .filter(String::isNotBlank)
                        .joinToString(" ")
                }
                .orEmpty()
        }

        return MetricsExportSnapshot(
            summary = _uiState.value.summary,
            missions = data,
            userNames = userNameCache.toMap()
        )
    }

    private fun toUiModel(mission: Mission) =
        MetricsMissionUiModel(
            id = mission.id,
            name = mission.name,
            date = missionReferenceDate(mission)
        )

    private fun missionReferenceDate(mission: Mission): LocalDateTime =
        mission.endedAt ?: mission.scheduledAt
}
