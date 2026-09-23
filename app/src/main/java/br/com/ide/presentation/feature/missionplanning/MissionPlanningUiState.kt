package br.com.ide.presentation.feature.missionplanning

import br.com.ide.domain.model.MissionStatus

data class MissionPlanningUiState(

    val missionId: String = "",

    val missionName: String = "",

    val missionStatus: MissionStatus? = null,

    // =========================================================
    // Locais
    // =========================================================

    val hasDepartureLocation: Boolean = false,

    val hasReturnLocation: Boolean = false,

    // =========================================================
    // Grupos
    // =========================================================

    val groupCount: Int = 0,

    val groupedParticipantCount: Int = 0,

    val unassignedParticipantCount: Int = 0,

    // =========================================================
    // Área
    // =========================================================

    val hasDefinedArea: Boolean = false,

    val areaPointCount: Int = 0,

    // =========================================================
    // Controle
    // =========================================================

    val isLoading: Boolean = false,

    val isScheduling: Boolean = false,

    val isStarting: Boolean = false
) {

    val canSchedule:
            Boolean
        get() =
            missionStatus ==
                    MissionStatus.PLANNING &&
                    hasDepartureLocation &&
                    !isLoading &&
                    !isScheduling &&
                    !isStarting

    val canStart:
            Boolean
        get() =
            missionStatus ==
                    MissionStatus.SCHEDULED &&
                    hasDepartureLocation &&
                    !isLoading &&
                    !isScheduling &&
                    !isStarting

    val isScheduled:
            Boolean
        get() =
            missionStatus ==
                    MissionStatus.SCHEDULED

    val isInProgress:
            Boolean
        get() =
            missionStatus ==
                    MissionStatus.IN_PROGRESS
}
