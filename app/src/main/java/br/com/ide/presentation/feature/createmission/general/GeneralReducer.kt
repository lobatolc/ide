package br.com.ide.presentation.feature.createmission.general

import br.com.ide.domain.model.MissionMovement
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class GeneralReducer @Inject constructor() {

    fun updateName(
        state: CreateMissionUiState,
        value: String
    ): CreateMissionUiState {

        return state.copy(
            name = value,
            nameError = null
        )
    }

    fun updateDate(
        state: CreateMissionUiState,
        value: LocalDate
    ): CreateMissionUiState {

        return state.copy(
            date = value,
            dateError = null,
            timeError = null
        )
    }

    fun updateTime(
        state: CreateMissionUiState,
        value: LocalTime
    ): CreateMissionUiState {

        return state.copy(
            time = value,
            timeError = null
        )
    }

    fun updateDescription(
        state: CreateMissionUiState,
        value: String
    ): CreateMissionUiState {

        return state.copy(
            description = value
        )
    }

    fun updateMovement(
        state: CreateMissionUiState,
        movement: MissionMovement
    ): CreateMissionUiState {

        return state.copy(
            movement = movement,
            movementError = null,

            customMovementName =
                if (
                    movement ==
                    MissionMovement.OTHER
                ) {
                    state.customMovementName
                } else {
                    ""
                },

            customMovementNameError = null
        )
    }

    fun updateCustomMovementName(
        state: CreateMissionUiState,
        value: String
    ): CreateMissionUiState {

        return state.copy(
            customMovementName = value,
            customMovementNameError = null
        )
    }
}