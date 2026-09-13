package br.com.ide.presentation.feature.createmission.summary

import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionNavigationReducer
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

class SummaryEventHandler @Inject constructor(
    private val navigationReducer:
    CreateMissionNavigationReducer
) {

    fun handle(
        state: CreateMissionUiState,
        event: CreateMissionEvent.Summary
    ): CreateMissionUiState {

        return when (event) {

            is CreateMissionEvent.GoToStep -> {

                navigationReducer
                    .goToStepFromSummary(
                        state = state,
                        step = event.step
                    )
            }

            CreateMissionEvent.ReturnToSummary -> {

                navigationReducer
                    .goToSummary(
                        state
                    )
            }

            CreateMissionEvent.CreateMission -> {
                state
            }
        }
    }
}