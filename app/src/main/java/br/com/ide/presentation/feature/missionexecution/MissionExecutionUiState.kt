package br.com.ide.presentation.feature.missionexecution

import androidx.annotation.StringRes
import br.com.ide.domain.model.MissionCoordinate
import br.com.ide.domain.model.MissionParticipantState
import br.com.ide.presentation.components.map.MissionParticipantMarker
import br.com.ide.presentation.components.map.MissionTrackLine

data class MissionExecutionUiState(

    val missionId: String = "",

    val missionName: String = "",

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

    val participantTracks:
    List<MissionTrackLine> =
        emptyList(),

    // =========================================================
    // Participantes em tempo real
    // =========================================================

    val participants:
    List<MissionParticipantState> =
        emptyList(),

    // =========================================================
    // Operação
    // =========================================================

    val elapsedSeconds: Long = 0L,

    val encounterCount: Int = 0,

    val groupCount: Int = 0,

    // =========================================================
    // Permissões / controle
    // =========================================================

    val canFinishMission: Boolean = false,

    val isSupportRequested: Boolean = false,

    val isCurrentUserSupport: Boolean = false,

    val isUpdatingSupportStatus: Boolean = false,

    val isLoading: Boolean = false,

    val isFinishing: Boolean = false,

    @StringRes
    val errorMessage: Int? = null
)
