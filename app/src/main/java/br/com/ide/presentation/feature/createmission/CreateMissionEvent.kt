package br.com.ide.presentation.feature.createmission

import br.com.ide.domain.model.MissionMovement
import java.time.LocalDate
import java.time.LocalTime

sealed interface CreateMissionEvent {

    data class NameChanged(
        val value: String
    ) : CreateMissionEvent

    data class DateChanged(
        val value: LocalDate
    ) : CreateMissionEvent

    data class TimeChanged(
        val value: LocalTime
    ) : CreateMissionEvent

    data class DescriptionChanged(
        val value: String
    ) : CreateMissionEvent

    data class MovementChanged(
        val movement: MissionMovement
    ) : CreateMissionEvent

    data class CustomMovementNameChanged(
        val value: String
    ) : CreateMissionEvent

    data object Next :
        CreateMissionEvent

    data object GeneralStepNavigationHandled :
        CreateMissionEvent
}