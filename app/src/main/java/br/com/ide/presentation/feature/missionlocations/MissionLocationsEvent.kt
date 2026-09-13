package br.com.ide.presentation.feature.missionlocations

sealed interface MissionLocationsEvent {

    data class SelectLocationType(
        val type: MissionLocationType
    ) : MissionLocationsEvent

    data class LocationSelected(
        val latitude: Double,
        val longitude: Double
    ) : MissionLocationsEvent

    data object RemoveReturnLocation :
        MissionLocationsEvent

    data object Save :
        MissionLocationsEvent
}