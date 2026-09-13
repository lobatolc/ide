package br.com.ide.presentation.feature.createmission.participants

import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

class ParticipantsEventHandler @Inject constructor(
    private val participantsSelectionReducer:
    ParticipantsSelectionReducer
) {

    fun handle(
        state: CreateMissionUiState,
        event: CreateMissionEvent.Participants
    ): CreateMissionUiState {

        return when (event) {

            is CreateMissionEvent.ChurchToggled -> {

                participantsSelectionReducer
                    .toggleChurch(
                        state = state,
                        churchId = event.churchId
                    )
            }

            is CreateMissionEvent.DistrictToggled -> {

                participantsSelectionReducer
                    .toggleDistrict(
                        state = state,
                        districtId = event.districtId
                    )
            }

            CreateMissionEvent.SelectAllChurches -> {

                participantsSelectionReducer
                    .selectAllChurches(
                        state
                    )
            }
        }
    }
}