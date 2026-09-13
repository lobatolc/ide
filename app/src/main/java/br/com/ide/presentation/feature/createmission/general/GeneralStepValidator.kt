package br.com.ide.presentation.feature.createmission.general

import br.com.ide.R
import br.com.ide.domain.model.MissionMovement
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class GeneralStepValidationResult(
    val nameError: Int? = null,
    val dateError: Int? = null,
    val timeError: Int? = null,
    val movementError: Int? = null,
    val customMovementNameError: Int? = null
) {

    val isValid: Boolean
        get() =
            nameError == null &&
                    dateError == null &&
                    timeError == null &&
                    movementError == null &&
                    customMovementNameError == null
}

class GeneralStepValidator @Inject constructor() {

    fun validate(
        state: CreateMissionUiState
    ): GeneralStepValidationResult {

        val nameError =
            if (
                state.name.isBlank()
            ) {
                R.string
                    .create_mission_name_required
            } else {
                null
            }

        val dateError =
            when {

                state.date == null -> {
                    R.string
                        .create_mission_date_required
                }

                state.date.isBefore(
                    LocalDate.now()
                ) -> {
                    R.string
                        .create_mission_date_in_past
                }

                else -> {
                    null
                }
            }

        val timeError =
            when {

                state.time == null -> {
                    R.string
                        .create_mission_time_required
                }

                state.date != null &&
                        LocalDateTime.of(
                            state.date,
                            state.time
                        ).isBefore(
                            LocalDateTime
                                .now()
                                .withSecond(0)
                                .withNano(0)
                        ) -> {
                    R.string
                        .create_mission_time_in_past
                }

                else -> {
                    null
                }
            }

        val movementError =
            if (
                state.movement == null
            ) {
                R.string
                    .create_mission_movement_required
            } else {
                null
            }

        val customMovementNameError =
            if (
                state.movement ==
                MissionMovement.OTHER &&
                state.customMovementName
                    .isBlank()
            ) {
                R.string
                    .create_mission_custom_movement_required
            } else {
                null
            }

        return GeneralStepValidationResult(
            nameError =
                nameError,
            dateError =
                dateError,
            timeError =
                timeError,
            movementError =
                movementError,
            customMovementNameError =
                customMovementNameError
        )
    }
}