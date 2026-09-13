package br.com.ide.presentation.feature.createmission.general

import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

class GeneralEventHandler @Inject constructor(
    private val generalReducer:
    GeneralReducer
) {

    fun handle(
        state: CreateMissionUiState,
        event: CreateMissionEvent.General
    ): CreateMissionUiState {

        return when (event) {

            is CreateMissionEvent.NameChanged -> {

                generalReducer.updateName(
                    state = state,
                    value = event.value
                )
            }

            is CreateMissionEvent.DateChanged -> {

                generalReducer.updateDate(
                    state = state,
                    value = event.value
                )
            }

            is CreateMissionEvent.TimeChanged -> {

                generalReducer.updateTime(
                    state = state,
                    value = event.value
                )
            }

            is CreateMissionEvent.DescriptionChanged -> {

                generalReducer.updateDescription(
                    state = state,
                    value = event.value
                )
            }

            is CreateMissionEvent.MovementChanged -> {

                generalReducer.updateMovement(
                    state = state,
                    movement = event.movement
                )
            }

            is CreateMissionEvent.CustomMovementNameChanged -> {

                generalReducer.updateCustomMovementName(
                    state = state,
                    value = event.value
                )
            }
        }
    }
}