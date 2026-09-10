package br.com.ide.presentation.feature.createmission

import androidx.annotation.StringRes
import br.com.ide.domain.model.MissionMovement
import java.time.LocalDate
import java.time.LocalTime

data class CreateMissionUiState(
    val name: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val description: String = "",

    val movement: MissionMovement? = null,
    val customMovementName: String = "",

    @StringRes
    val nameError: Int? = null,

    @StringRes
    val dateError: Int? = null,

    @StringRes
    val timeError: Int? = null,

    @StringRes
    val movementError: Int? = null,

    @StringRes
    val customMovementNameError: Int? = null,

    @StringRes
    val errorMessage: Int? = null,

    val isLoading: Boolean = false,

    val generalStepCompleted: Boolean = false
)