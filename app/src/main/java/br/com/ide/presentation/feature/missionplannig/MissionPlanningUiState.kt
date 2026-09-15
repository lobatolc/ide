package br.com.ide.presentation.feature.missionplanning

data class MissionPlanningUiState(

    val missionId: String = "",
    val missionName: String = "",

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
    // Área de atuação
    // =========================================================

    val hasDefinedArea: Boolean = false,
    val areaPointCount: Int = 0,

    // =========================================================
    // Controle
    // =========================================================

    val isLoading: Boolean = false
)