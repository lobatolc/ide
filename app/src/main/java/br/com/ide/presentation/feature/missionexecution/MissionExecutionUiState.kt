package br.com.ide.presentation.feature.missionexecution

import androidx.annotation.StringRes
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionCoordinate
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionEncounterMarker
import br.com.ide.domain.model.MissionGeneralMetrics
import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.model.MissionPersonalMetrics
import br.com.ide.domain.model.UserRole
import br.com.ide.presentation.components.map.MissionParticipantMarker
import br.com.ide.presentation.components.map.MissionTrackLine

data class MissionExecutionUiState(

    val missionId: String = "",

    val missionName: String = "",

    val customActivityName: String? = null,

    val availableActivities:
    Set<MissionActivityType> =
        emptySet(),

    // =========================================================
    // Mapa
    // =========================================================

    val departureLatitude: Double? = null,

    val departureLongitude: Double? = null,

    val currentLatitude: Double? = null,

    val currentLongitude: Double? = null,

    val areaPoints: List<MissionCoordinate> =
        emptyList(),

    val participantMarkers:
    List<MissionParticipantMarker> =
        emptyList(),

    val encounterMarkers:
    List<MissionEncounterMarker> =
        emptyList(),

    val encounters:
    List<MissionEncounter> =
        emptyList(),

    val participantTracks:
    List<MissionTrackLine> =
        emptyList(),

    // =========================================================
    // Filtro de grupos no mapa
    // =========================================================

    val mapGroupFilters:
    List<MissionMapGroupFilterUiModel> =
        emptyList(),

    val selectedMapGroupIds:
    Set<String> =
        emptySet(),

    val includeUngroupedOnMap: Boolean = false,

    val isMapGroupFilterActive: Boolean = false,

    // =========================================================
    // Participantes em tempo real
    // =========================================================

    val participants:
    List<MissionParticipantState> =
        emptyList(),

    // =========================================================
    // Meu grupo / Grupo Geral
    // =========================================================

    val usesGeneralGroup: Boolean = false,

    val currentGroupId: String? = null,

    val currentGroupName: String? = null,

    val currentGroupColorHex: String? = null,

    val currentGroupMembers:
    List<MissionGroupMemberUiModel> =
        emptyList(),

    // =========================================================
    // Operação
    // =========================================================

    val elapsedSeconds: Long = 0L,

    val encounterCount: Int = 0,

    val personalMetrics:
    MissionPersonalMetrics =
        MissionPersonalMetrics(),

    val generalMetrics:
    MissionGeneralMetrics =
        MissionGeneralMetrics(),

    val groupCount: Int = 0,

    // =========================================================
    // Permissões / controle
    // =========================================================

    val canViewGeneralMetrics: Boolean = false,

    val canFinishMission: Boolean = false,

    val isSupportRequested: Boolean = false,

    val isCurrentUserSupport: Boolean = false,

    val isUpdatingSupportStatus: Boolean = false,

    val isEndingParticipation: Boolean = false,

    val participationEnded: Boolean = false,

    val participationAccessDenied: Boolean = false,

    @StringRes
    val endParticipationErrorMessage: Int? = null,

    val isLoading: Boolean = false,

    val isFinishing: Boolean = false,

    val missionFinished: Boolean = false,

    @StringRes
    val errorMessage: Int? = null
)

data class MissionGroupMemberUiModel(
    val userId: String,
    val displayName: String,
    val role: UserRole? = null,
    val status: MissionParticipantStatus =
        MissionParticipantStatus.ACTIVE,
    val isSupport: Boolean = false,
    val isCurrentUser: Boolean = false,
    val hasMapPosition: Boolean = false
)

data class MissionMapGroupFilterUiModel(
    val id: String,
    val name: String,
    val colorHex: String,
    val participantCount: Int = 0
)
