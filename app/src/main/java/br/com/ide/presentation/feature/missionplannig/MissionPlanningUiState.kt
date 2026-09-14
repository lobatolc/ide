package br.com.ide.presentation.feature.missionplanning

data class MissionPlanningUiState(

    val missionId: String = "",
    val missionName: String = "",

    val hasDepartureLocation: Boolean = false,
    val hasReturnLocation: Boolean = false,

    val isLoading: Boolean = false
)