package br.com.ide.presentation.feature.missionarea

sealed interface MissionAreaEvent {

    data class SelectMode(
        val mode: MissionAreaMode
    ) : MissionAreaEvent

    data class AddPoint(
        val latitude: Double,
        val longitude: Double
    ) : MissionAreaEvent

    data object UndoLastPoint :
        MissionAreaEvent

    data object ClearArea :
        MissionAreaEvent

    data object Save :
        MissionAreaEvent
}