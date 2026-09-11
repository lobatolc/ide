package br.com.ide.presentation.feature.createmission.general

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.domain.model.MissionMovement
import br.com.ide.presentation.components.IdeDatePickerDialog
import br.com.ide.presentation.components.IdeDropdownField
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeTextField
import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import br.com.ide.presentation.feature.createmission.components.CreateMissionHeader
import br.com.ide.presentation.feature.createmission.components.CreateMissionStepHeader
import br.com.ide.presentation.feature.createmission.components.MissionDateTimeField
import br.com.ide.presentation.feature.createmission.components.MissionScreenContainer
import br.com.ide.presentation.mapper.toStringRes
import java.time.format.DateTimeFormatter

@Composable
fun CreateMissionGeneralScreen(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
    }

    val dateFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val timeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")

    MissionScreenContainer {

        CreateMissionHeader(
            title =
                stringResource(
                    R.string.create_mission_title
                ),
            backContentDescription =
                stringResource(
                    R.string.create_mission_back
                ),
            onBackClick =
                onBackClick
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        CreateMissionStepHeader(
            stepText =
                stringResource(
                    R.string.create_mission_step,
                    1,
                    4
                ),
            title =
                stringResource(
                    R.string.create_mission_general_title
                ),
            subtitle =
                stringResource(
                    R.string.create_mission_general_subtitle
                )
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        IdeTextField(
            value =
                uiState.name,
            onValueChange = {
                onEvent(
                    CreateMissionEvent
                        .NameChanged(it)
                )
            },
            label =
                stringResource(
                    R.string.create_mission_name
                ),
            errorRes =
                uiState.nameError,
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        MissionDateTimeField(
            label =
                stringResource(
                    R.string.create_mission_date
                ),
            value =
                uiState.date
                    ?.format(dateFormatter)
                    ?: stringResource(
                        R.string.create_mission_select_date
                    ),
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.CalendarMonth,
                    contentDescription =
                        null
                )
            },
            hasError =
                uiState.dateError != null,
            errorText =
                uiState.dateError
                    ?.let {
                        stringResource(it)
                    },
            onClick = {
                showDatePicker = true
            }
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        MissionDateTimeField(
            label =
                stringResource(
                    R.string.create_mission_time
                ),
            value =
                uiState.time
                    ?.format(timeFormatter)
                    ?: stringResource(
                        R.string.create_mission_select_time
                    ),
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.Schedule,
                    contentDescription =
                        null
                )
            },
            hasError =
                uiState.timeError != null,
            errorText =
                uiState.timeError
                    ?.let {
                        stringResource(it)
                    },
            onClick = {
                showTimePicker = true
            }
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        OutlinedTextField(
            value =
                uiState.description,
            onValueChange = {
                onEvent(
                    CreateMissionEvent
                        .DescriptionChanged(it)
                )
            },
            label = {
                Text(
                    text =
                        stringResource(
                            R.string
                                .create_mission_description_optional
                        )
                )
            },
            minLines = 3,
            maxLines = 5,
            shape =
                RoundedCornerShape(16.dp),
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        IdeDropdownField(
            selectedValue =
                uiState.movement,
            options =
                MissionMovement.entries,
            label =
                stringResource(
                    R.string.create_mission_movement
                ),
            optionText = {
                stringResource(
                    it.toStringRes()
                )
            },
            onOptionSelected = {
                onEvent(
                    CreateMissionEvent
                        .MovementChanged(it)
                )
            },
            errorRes =
                uiState.movementError,
            modifier =
                Modifier.fillMaxWidth()
        )

        if (
            uiState.movement ==
            MissionMovement.OTHER
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            IdeTextField(
                value =
                    uiState.customMovementName,
                onValueChange = {
                    onEvent(
                        CreateMissionEvent
                            .CustomMovementNameChanged(it)
                    )
                },
                label =
                    stringResource(
                        R.string.create_mission_custom_movement
                    ),
                errorRes =
                    uiState.customMovementNameError,
                modifier =
                    Modifier.fillMaxWidth()
            )
        }

        Spacer(
            modifier =
                Modifier.height(28.dp)
        )

        IdePrimaryButton(
            text =
                stringResource(
                    R.string.create_mission_next
                ),
            onClick = {
                onEvent(
                    CreateMissionEvent.Next
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        )
    }

    if (showDatePicker) {

        IdeDatePickerDialog(
            selectedDate =
                uiState.date,
            onDateSelected = { date ->
                onEvent(
                    CreateMissionEvent
                        .DateChanged(date)
                )
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {

        br.com.ide.presentation.components.IdeTimePickerDialog(
            selectedTime =
                uiState.time,
            onTimeSelected = { time ->
                onEvent(
                    CreateMissionEvent
                        .TimeChanged(time)
                )
            },
            onDismiss = {
                showTimePicker = false
            }
        )
    }
}