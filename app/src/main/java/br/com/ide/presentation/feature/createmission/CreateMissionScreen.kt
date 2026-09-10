package br.com.ide.presentation.feature.createmission

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.domain.model.MissionMovement
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdeDatePickerDialog
import br.com.ide.presentation.components.IdeDropdownField
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.components.IdeTextField
import br.com.ide.presentation.components.IdeTimePickerDialog
import br.com.ide.presentation.mapper.toStringRes
import java.time.format.DateTimeFormatter

@Composable
fun CreateMissionScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    viewModel: CreateMissionViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(
        uiState.generalStepCompleted
    ) {
        if (uiState.generalStepCompleted) {

            onNextClick()

            viewModel.onEvent(
                CreateMissionEvent
                    .GeneralStepNavigationHandled
            )
        }
    }

    CreateMissionGeneralContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick
    )
}

@Composable
private fun CreateMissionGeneralContent(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var showDatePicker by
    remember {
        mutableStateOf(false)
    }

    var showTimePicker by
    remember {
        mutableStateOf(false)
    }

    val dateFormatter =
        DateTimeFormatter.ofPattern(
            "dd/MM/yyyy"
        )

    val timeFormatter =
        DateTimeFormatter.ofPattern(
            "HH:mm"
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .statusBarsPadding()
            .padding(
                start = 20.dp,
                end = 12.dp,
                top = 20.dp,
                bottom = 24.dp
            ),
        verticalArrangement =
            Arrangement.Top
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IdeBackButton(
                onClick =
                    onBackClick,
                contentDescription =
                    stringResource(
                        R.string.create_mission_back
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            IdeScreenTitle(
                text = stringResource(
                    R.string.create_mission_title
                )
            )
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Text(
            text = stringResource(
                R.string.create_mission_step,
                1,
                4
            ),
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            color =
                MaterialTheme
                    .colorScheme
                    .primary,
            fontWeight =
                FontWeight.SemiBold
        )

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        IdeScreenTitle(
            text = stringResource(
                R.string.create_mission_general_title
            )
        )

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        IdeScreenSubtitle(
            text = stringResource(
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
                            .CustomMovementNameChanged(
                                it
                            )
                    )
                },

                label =
                    stringResource(
                        R.string
                            .create_mission_custom_movement
                    ),

                errorRes =
                    uiState
                        .customMovementNameError,

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

        IdeTimePickerDialog(
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

@Composable
private fun MissionDateTimeField(
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    hasError: Boolean,
    errorText: String?,
    onClick: () -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text =
                label,
            style =
                MaterialTheme
                    .typography
                    .labelMedium,
            color =
                if (hasError) {
                    MaterialTheme
                        .colorScheme
                        .error
                } else {
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                }
        )

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            shape =
                RoundedCornerShape(16.dp),
            color =
                MaterialTheme
                    .colorScheme
                    .surface,
            border =
                BorderStroke(
                    width =
                        1.dp,
                    color =
                        if (hasError) {
                            MaterialTheme
                                .colorScheme
                                .error
                        } else {
                            MaterialTheme
                                .colorScheme
                                .outline
                        }
                )
        ) {

            Row(
                modifier =
                    Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 16.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                CompositionLocalProvider(
                    LocalContentColor provides
                            MaterialTheme
                                .colorScheme
                                .primary
                ) {
                    icon()
                }

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Text(
                    text =
                        value,
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )
            }
        }

        if (
            hasError &&
            errorText != null
        ) {

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    errorText,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .error,
                modifier =
                    Modifier.padding(
                        start = 16.dp
                    )
            )
        }
    }
}