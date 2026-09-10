package br.com.ide.presentation.feature.createmission

import androidx.lifecycle.ViewModel
import br.com.ide.R
import br.com.ide.domain.model.MissionMovement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CreateMissionViewModel @Inject constructor() :
    ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CreateMissionUiState()
        )

    val uiState:
            StateFlow<CreateMissionUiState> =
        _uiState.asStateFlow()

    fun onEvent(
        event: CreateMissionEvent
    ) {
        when (event) {

            is CreateMissionEvent.NameChanged -> {
                _uiState.update {
                    it.copy(
                        name = event.value,
                        nameError = null
                    )
                }
            }

            is CreateMissionEvent.DateChanged -> {
                _uiState.update {
                    it.copy(
                        date = event.value,
                        dateError = null
                    )
                }
            }

            is CreateMissionEvent.TimeChanged -> {
                _uiState.update {
                    it.copy(
                        time = event.value,
                        timeError = null
                    )
                }
            }

            is CreateMissionEvent.DescriptionChanged -> {
                _uiState.update {
                    it.copy(
                        description = event.value
                    )
                }
            }

            is CreateMissionEvent.MovementChanged -> {
                _uiState.update {
                    it.copy(
                        movement = event.movement,
                        movementError = null,

                        customMovementName =
                            if (
                                event.movement ==
                                MissionMovement.OTHER
                            ) {
                                it.customMovementName
                            } else {
                                ""
                            },

                        customMovementNameError =
                            null
                    )
                }
            }

            is CreateMissionEvent
            .CustomMovementNameChanged -> {

                _uiState.update {
                    it.copy(
                        customMovementName =
                            event.value,
                        customMovementNameError =
                            null
                    )
                }
            }

            CreateMissionEvent.Next -> {
                validateGeneralStep()
            }

            CreateMissionEvent.GeneralStepNavigationHandled -> {
                _uiState.update {
                    it.copy(
                        generalStepCompleted = false
                    )
                }
            }
        }
    }

    private fun validateGeneralStep() {

        val state =
            _uiState.value

        val nameError =
            if (state.name.isBlank()) {
                R.string.create_mission_name_required
            } else {
                null
            }

        val dateError =
            if (state.date == null) {
                R.string.create_mission_date_required
            } else {
                null
            }

        val timeError =
            if (state.time == null) {
                R.string.create_mission_time_required
            } else {
                null
            }

        val movementError =
            if (state.movement == null) {
                R.string.create_mission_movement_required
            } else {
                null
            }

        val customMovementNameError =
            if (
                state.movement ==
                MissionMovement.OTHER &&
                state.customMovementName.isBlank()
            ) {
                R.string
                    .create_mission_custom_movement_required
            } else {
                null
            }

        val isValid =
            nameError == null &&
                    dateError == null &&
                    timeError == null &&
                    movementError == null &&
                    customMovementNameError == null

        _uiState.update {
            it.copy(
                nameError = nameError,
                dateError = dateError,
                timeError = timeError,
                movementError = movementError,
                customMovementNameError =
                    customMovementNameError,
                generalStepCompleted =
                    isValid
            )
        }
    }
}