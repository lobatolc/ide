package br.com.ide.presentation.feature.createmission.actions

import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

class ActionsEventHandler @Inject constructor(
    private val actionsReducer:
    ActionsReducer
) {

    fun handle(
        state: CreateMissionUiState,
        event: CreateMissionEvent.Actions
    ): CreateMissionUiState {

        return when (event) {

            is CreateMissionEvent.ActivityToggled -> {

                actionsReducer
                    .toggleActivity(
                        state = state,
                        activity = event.activity
                    )
            }

            is CreateMissionEvent.CustomActivityNameChanged -> {

                actionsReducer
                    .updateCustomActivityName(
                        state = state,
                        value = event.value
                    )
            }
        }
    }
}