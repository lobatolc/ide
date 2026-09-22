package br.com.ide.presentation.feature.metrics

import androidx.annotation.StringRes
import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionParticipantState
import java.time.LocalDateTime

const val METRICS_MAX_SELECTED_MISSIONS = 10

enum class MetricsPeriodFilter {
    ALL_TIME,
    CURRENT_YEAR,
    LAST_30_DAYS,
    LAST_90_DAYS
}

data class MetricsMissionUiModel(
    val id: String,
    val name: String,
    val date: LocalDateTime
)

data class MetricsSummary(
    val missionCount: Int = 0,
    val uniqueMissionaryCount: Int = 0,
    val participationCount: Int = 0,
    val groupCount: Int = 0,
    val encounterCount: Int = 0,
    val visitCount: Int = 0,
    val prayerCount: Int = 0,
    val customActivityCount: Int = 0,
    val offeredBibleStudyCount: Int = 0,
    val acceptedBibleStudyCount: Int = 0,
    val deliveredMaterialCount: Int = 0,
    val completedSurveyCount: Int = 0,
    val distanceMeters: Double = 0.0,
    val participationSeconds: Long = 0L
)

data class MetricsUiState(
    val completedMissions: List<MetricsMissionUiModel> = emptyList(),
    val visibleMissions: List<MetricsMissionUiModel> = emptyList(),

    val selectedMissionIds: Set<String> = emptySet(),
    val draftSelectedMissionIds: Set<String> = emptySet(),

    val appliedPeriod: MetricsPeriodFilter = MetricsPeriodFilter.ALL_TIME,
    val draftPeriod: MetricsPeriodFilter = MetricsPeriodFilter.ALL_TIME,
    val searchQuery: String = "",
    val isSelectorOpen: Boolean = false,

    val summary: MetricsSummary = MetricsSummary(),
    val selectedStartDate: LocalDateTime? = null,
    val selectedEndDate: LocalDateTime? = null,

    val totalCompletedMissionCount: Int = 0,
    val periodMissionCount: Int = 0,

    val isLoading: Boolean = true,
    val isCalculating: Boolean = false,

    @StringRes
    val errorMessage: Int? = null
)

data class MetricsMissionExportData(
    val mission: Mission,
    val participants: List<MissionParticipantState>,
    val encounters: List<MissionEncounter>
)

data class MetricsExportSnapshot(
    val summary: MetricsSummary,
    val missions: List<MetricsMissionExportData>,
    val userNames: Map<String, String>
)
