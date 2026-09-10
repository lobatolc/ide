package br.com.ide.presentation.feature.createmission

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.domain.model.Church
import br.com.ide.domain.model.District
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionMovement
import br.com.ide.domain.model.SurveyQuestionDraft
import br.com.ide.domain.model.SurveyQuestionType
import br.com.ide.domain.model.UserRole
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
import androidx.compose.foundation.layout.ColumnScope

@Composable
fun CreateMissionScreen(
    onBackClick: () -> Unit,
    viewModel: CreateMissionViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    when (uiState.subScreen) {

        CreateMissionSubScreen.MATERIALS -> {
            CreateMissionMaterialsContent(
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }

        CreateMissionSubScreen.SURVEY -> {
            CreateMissionSurveyContent(
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }

        CreateMissionSubScreen.NONE -> {

            when (uiState.currentStep) {

                1 -> {
                    CreateMissionGeneralContent(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onBackClick = onBackClick
                    )
                }

                2 -> {
                    CreateMissionParticipantsContent(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onBackClick = {
                            viewModel.onEvent(
                                CreateMissionEvent.PreviousStep
                            )
                        }
                    )
                }

                3 -> {
                    CreateMissionActionsContent(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        onBackClick = {
                            viewModel.onEvent(
                                CreateMissionEvent.PreviousStep
                            )
                        }
                    )
                }
            }
        }
    }
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

    MissionScreenContainer {

        CreateMissionHeader(
            title =
                stringResource(
                    R.string.create_mission_title
                ),
            onBackClick =
                onBackClick
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        CreateMissionStepHeader(
            currentStep = 1,
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
private fun CreateMissionParticipantsContent(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit,
    onBackClick: () -> Unit
) {
    MissionScreenContainer {

        CreateMissionHeader(
            title =
                stringResource(
                    R.string.create_mission_title
                ),
            onBackClick =
                onBackClick
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        CreateMissionStepHeader(
            currentStep = 2,
            title =
                stringResource(
                    R.string.create_mission_participants_title
                ),
            subtitle =
                stringResource(
                    R.string.create_mission_participants_subtitle
                )
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        if (
            uiState.participantsStepLoading
        ) {

            Column(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }

            return@MissionScreenContainer
        }

        when (uiState.creatorRole) {

            UserRole.LEADER -> {
                LeaderParticipantsContent(
                    uiState = uiState
                )
            }

            UserRole.PASTOR -> {

                ParticipantsSelectionSummary(
                    selectedCount =
                        uiState.selectedChurchIds.size
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                PastorParticipantsContent(
                    uiState = uiState,
                    onEvent = onEvent
                )
            }

            UserRole.ADMIN -> {

                ParticipantsSelectionSummary(
                    selectedCount =
                        uiState.selectedChurchIds.size
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                AdminParticipantsContent(
                    uiState = uiState,
                    onEvent = onEvent
                )
            }

            else -> Unit
        }

        uiState.participantsError
            ?.let { errorRes ->

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                MissionFieldError(
                    errorRes =
                        errorRes
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
}

@Composable
private fun CreateMissionActionsContent(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit,
    onBackClick: () -> Unit
) {
    MissionScreenContainer {

        CreateMissionHeader(
            title =
                stringResource(
                    R.string.create_mission_title
                ),
            onBackClick =
                onBackClick
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        CreateMissionStepHeader(
            currentStep = 3,
            title =
                stringResource(
                    R.string.create_mission_actions_title
                ),
            subtitle =
                stringResource(
                    R.string.create_mission_actions_subtitle
                )
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(16.dp),
            color =
                MaterialTheme.colorScheme.surface,
            border =
                BorderStroke(
                    width =
                        1.dp,
                    color =
                        if (
                            uiState.activitiesError != null
                        ) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                )
        ) {

            Column {

                MissionActivityType.entries
                    .forEachIndexed {
                            index,
                            activity ->

                        ActionSelectionRow(
                            title =
                                stringResource(
                                    activity.toStringRes()
                                ),
                            checked =
                                activity in
                                        uiState.selectedActivities,
                            onCheckedChange = {
                                onEvent(
                                    CreateMissionEvent
                                        .ActivityToggled(activity)
                                )
                            }
                        )

                        if (
                            index <
                            MissionActivityType.entries.lastIndex
                        ) {
                            HorizontalDivider(
                                modifier =
                                    Modifier.padding(
                                        start = 16.dp
                                    )
                            )
                        }
                    }
            }
        }

        uiState.activitiesError
            ?.let {

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                MissionFieldError(
                    errorRes =
                        it
                )
            }

        if (
            MissionActivityType.OTHER in
            uiState.selectedActivities
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            IdeTextField(
                value =
                    uiState.customActivityName,
                onValueChange = {
                    onEvent(
                        CreateMissionEvent
                            .CustomActivityNameChanged(it)
                    )
                },
                label =
                    stringResource(
                        R.string.create_mission_custom_activity
                    ),
                errorRes =
                    uiState.customActivityNameError,
                modifier =
                    Modifier.fillMaxWidth()
            )
        }

        if (
            MissionActivityType.MATERIAL_DELIVERY in
            uiState.selectedActivities
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            ConfigurationRow(
                title =
                    stringResource(
                        R.string.create_mission_configure_materials
                    ),
                subtitle =
                    if (
                        uiState.selectedMaterials.isEmpty()
                    ) {
                        null
                    } else {
                        stringResource(
                            R.string
                                .create_mission_materials_configured,
                            uiState.selectedMaterials.size
                        )
                    },
                hasError =
                    uiState.materialsError != null,
                onClick = {
                    onEvent(
                        CreateMissionEvent.OpenMaterials
                    )
                }
            )

            uiState.materialsError
                ?.let {

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    MissionFieldError(
                        errorRes =
                            it
                    )
                }
        }

        if (
            MissionActivityType.OPINION_SURVEY in
            uiState.selectedActivities
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            ConfigurationRow(
                title =
                    stringResource(
                        R.string.create_mission_configure_survey
                    ),
                subtitle =
                    if (
                        uiState.surveyQuestions.isEmpty()
                    ) {
                        null
                    } else {
                        stringResource(
                            R.string
                                .create_mission_survey_configured,
                            uiState.surveyQuestions.size
                        )
                    },
                hasError =
                    uiState.surveyError != null,
                onClick = {
                    onEvent(
                        CreateMissionEvent.OpenSurvey
                    )
                }
            )

            uiState.surveyError
                ?.let {

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    MissionFieldError(
                        errorRes =
                            it
                    )
                }
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
}

@Composable
private fun CreateMissionMaterialsContent(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit
) {
    MissionScreenContainer {

        CreateMissionHeader(
            title =
                stringResource(
                    R.string.create_mission_configure_materials
                ),
            onBackClick = {
                onEvent(
                    CreateMissionEvent.CloseMaterials
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        IdeScreenSubtitle(
            text =
                stringResource(
                    R.string.create_mission_materials_subtitle
                )
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(16.dp),
            color =
                MaterialTheme.colorScheme.surface,
            border =
                BorderStroke(
                    width =
                        1.dp,
                    color =
                        if (
                            uiState.materialsError != null
                        ) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                )
        ) {

            Column {

                MissionMaterialType.entries
                    .forEachIndexed {
                            index,
                            material ->

                        ActionSelectionRow(
                            title =
                                stringResource(
                                    material.toStringRes()
                                ),
                            checked =
                                material in
                                        uiState.selectedMaterials,
                            onCheckedChange = {
                                onEvent(
                                    CreateMissionEvent
                                        .MaterialToggled(material)
                                )
                            }
                        )

                        if (
                            index <
                            MissionMaterialType.entries.lastIndex
                        ) {
                            HorizontalDivider(
                                modifier =
                                    Modifier.padding(
                                        start = 16.dp
                                    )
                            )
                        }
                    }
            }
        }

        uiState.materialsError
            ?.let {

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                MissionFieldError(
                    errorRes =
                        it
                )
            }

        if (
            MissionMaterialType.OTHER in
            uiState.selectedMaterials
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            IdeTextField(
                value =
                    uiState.customMaterialName,
                onValueChange = {
                    onEvent(
                        CreateMissionEvent
                            .CustomMaterialNameChanged(it)
                    )
                },
                label =
                    stringResource(
                        R.string.create_mission_custom_material
                    ),
                errorRes =
                    uiState.customMaterialNameError,
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
                    R.string.create_mission_save_materials
                ),
            onClick = {
                onEvent(
                    CreateMissionEvent.SaveMaterials
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CreateMissionSurveyContent(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit
) {
    MissionScreenContainer {

        CreateMissionHeader(
            title =
                stringResource(
                    R.string.create_mission_survey_title
                ),
            onBackClick = {
                onEvent(
                    CreateMissionEvent.CloseSurvey
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        IdeScreenSubtitle(
            text =
                stringResource(
                    R.string.create_mission_survey_subtitle
                )
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        uiState.surveyQuestions
            .forEachIndexed {
                    index,
                    question ->

                SurveyQuestionCard(
                    number =
                        index + 1,
                    question =
                        question,
                    canRemove =
                        uiState.surveyQuestions.size > 1,
                    onEvent =
                        onEvent
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )
            }

        uiState.surveyError
            ?.let {

                MissionFieldError(
                    errorRes =
                        it
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )
            }

        TextButton(
            onClick = {
                onEvent(
                    CreateMissionEvent.AddSurveyQuestion
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text(
                text =
                    "+ " +
                            stringResource(
                                R.string.create_mission_add_question
                            )
            )
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        IdePrimaryButton(
            text =
                stringResource(
                    R.string.create_mission_save_survey
                ),
            onClick = {
                onEvent(
                    CreateMissionEvent.SaveSurvey
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SurveyQuestionCard(
    number: Int,
    question: SurveyQuestionDraft,
    canRemove: Boolean,
    onEvent: (CreateMissionEvent) -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(16.dp),
        color =
            MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                width = 1.dp,
                color =
                    MaterialTheme.colorScheme.outline
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "${stringResource(R.string.create_mission_survey_question)} $number",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    modifier =
                        Modifier.weight(1f)
                )

                if (canRemove) {

                    IconButton(
                        onClick = {
                            onEvent(
                                CreateMissionEvent
                                    .RemoveSurveyQuestion(
                                        question.id
                                    )
                            )
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.Delete,
                            contentDescription =
                                stringResource(
                                    R.string
                                        .create_mission_remove_question
                                )
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            IdeTextField(
                value =
                    question.question,
                onValueChange = {
                    onEvent(
                        CreateMissionEvent
                            .SurveyQuestionChanged(
                                questionId =
                                    question.id,
                                value =
                                    it
                            )
                    )
                },
                label =
                    stringResource(
                        R.string.create_mission_survey_question
                    ),
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            IdeDropdownField(
                selectedValue =
                    question.type,
                options =
                    SurveyQuestionType.entries,
                label =
                    stringResource(
                        R.string.create_mission_survey_question_type
                    ),
                optionText = {
                    stringResource(
                        it.toStringRes()
                    )
                },
                onOptionSelected = {
                    onEvent(
                        CreateMissionEvent
                            .SurveyQuestionTypeChanged(
                                questionId =
                                    question.id,
                                type =
                                    it
                            )
                    )
                },
                modifier =
                    Modifier.fillMaxWidth()
            )

            if (
                question.type ==
                SurveyQuestionType.SINGLE_CHOICE
            ) {

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                question.options
                    .forEachIndexed {
                            optionIndex,
                            option ->

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            IdeTextField(
                                value =
                                    option,
                                onValueChange = {
                                    onEvent(
                                        CreateMissionEvent
                                            .SurveyOptionChanged(
                                                questionId =
                                                    question.id,
                                                optionIndex =
                                                    optionIndex,
                                                value =
                                                    it
                                            )
                                    )
                                },
                                label =
                                    "${stringResource(R.string.create_mission_survey_option)} ${optionIndex + 1}",
                                modifier =
                                    Modifier.weight(1f)
                            )

                            if (
                                question.options.size > 2
                            ) {

                                Spacer(
                                    modifier =
                                        Modifier.width(4.dp)
                                )

                                IconButton(
                                    onClick = {
                                        onEvent(
                                            CreateMissionEvent
                                                .RemoveSurveyOption(
                                                    questionId =
                                                        question.id,
                                                    optionIndex =
                                                        optionIndex
                                                )
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Outlined.Delete,
                                        contentDescription =
                                            null
                                    )
                                }
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )
                    }

                TextButton(
                    onClick = {
                        onEvent(
                            CreateMissionEvent
                                .AddSurveyOption(
                                    question.id
                                )
                        )
                    }
                ) {
                    Text(
                        text =
                            "+ " +
                                    stringResource(
                                        R.string.create_mission_add_option
                                    )
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigurationRow(
    title: String,
    subtitle: String?,
    hasError: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
        shape =
            RoundedCornerShape(16.dp),
        color =
            MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                width = 1.dp,
                color =
                    if (hasError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
            )
    ) {

        Row(
            modifier =
                Modifier.padding(16.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        title,
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme.colorScheme.onSurface
                )

                if (
                    !subtitle.isNullOrBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            subtitle,
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector =
                    Icons.Outlined.ChevronRight,
                contentDescription =
                    null,
                tint =
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ActionSelectionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    SelectionRow(
        title =
            title,
        checked =
            checked,
        onCheckedChange =
            onCheckedChange
    )
}

@Composable
private fun LeaderParticipantsContent(
    uiState: CreateMissionUiState
) {
    Text(
        text =
            stringResource(
                R.string.create_mission_leader_participants_info
            ),
        style =
            MaterialTheme.typography.bodyLarge,
        color =
            MaterialTheme.colorScheme.onBackground
    )

    Spacer(
        modifier =
            Modifier.height(16.dp)
    )

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(16.dp),
        color =
            MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                width = 1.dp,
                color =
                    MaterialTheme.colorScheme.outline
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text =
                    stringResource(
                        R.string.create_mission_selected_church
                    ),
                style =
                    MaterialTheme.typography.labelLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text =
                    uiState.creatorChurchName
                        .ifBlank { "-" },
                style =
                    MaterialTheme.typography.bodyLarge,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ParticipantsSelectionSummary(
    selectedCount: Int
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(16.dp),
        color =
            MaterialTheme.colorScheme.surfaceVariant
    ) {

        Text(
            text =
                stringResource(
                    R.string.create_mission_selected_church_count,
                    selectedCount
                ),
            modifier =
                Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            style =
                MaterialTheme.typography.bodyMedium,
            fontWeight =
                FontWeight.SemiBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun PastorParticipantsContent(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit
) {
    val allSelected =
        uiState.churches.isNotEmpty() &&
                uiState.selectedChurchIds
                    .containsAll(
                        uiState.churches.map {
                            it.id
                        }
                    )

    SelectionRow(
        title =
            stringResource(
                R.string.create_mission_select_all_churches
            ),
        checked =
            allSelected,
        onCheckedChange = {
            onEvent(
                CreateMissionEvent.SelectAllChurches
            )
        }
    )

    Spacer(
        modifier =
            Modifier.height(12.dp)
    )

    uiState.churches
        .forEachIndexed {
                index,
                church ->

            ChurchSelectionRow(
                church =
                    church,
                checked =
                    church.id in
                            uiState.selectedChurchIds,
                onCheckedChange = {
                    onEvent(
                        CreateMissionEvent
                            .ChurchToggled(
                                church.id
                            )
                    )
                }
            )

            if (
                index <
                uiState.churches.lastIndex
            ) {
                HorizontalDivider()
            }
        }
}

@Composable
private fun AdminParticipantsContent(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit
) {
    val allChurchIds =
        uiState.churches.map {
            it.id
        }

    val allSelected =
        allChurchIds.isNotEmpty() &&
                uiState.selectedChurchIds
                    .containsAll(
                        allChurchIds
                    )

    SelectionRow(
        title =
            stringResource(
                R.string.create_mission_select_all_churches
            ),
        checked =
            allSelected,
        onCheckedChange = {
            onEvent(
                CreateMissionEvent.SelectAllChurches
            )
        }
    )

    Spacer(
        modifier =
            Modifier.height(16.dp)
    )

    uiState.districts
        .forEach { district ->

            DistrictSelectionBlock(
                district =
                    district,
                churches =
                    uiState.churches
                        .filter {
                            it.districtId ==
                                    district.id
                        },
                selectedChurchIds =
                    uiState.selectedChurchIds,
                onDistrictToggle = {
                    onEvent(
                        CreateMissionEvent
                            .DistrictToggled(
                                district.id
                            )
                    )
                },
                onChurchToggle = {
                        churchId ->

                    onEvent(
                        CreateMissionEvent
                            .ChurchToggled(
                                churchId
                            )
                    )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }
}

@Composable
private fun DistrictSelectionBlock(
    district: District,
    churches: List<Church>,
    selectedChurchIds: Set<String>,
    onDistrictToggle: () -> Unit,
    onChurchToggle: (String) -> Unit
) {
    val selectedCount =
        churches.count {
            it.id in selectedChurchIds
        }

    val toggleState =
        when {

            selectedCount == 0 -> {
                ToggleableState.Off
            }

            selectedCount ==
                    churches.size &&
                    churches.isNotEmpty() -> {
                ToggleableState.On
            }

            else -> {
                ToggleableState.Indeterminate
            }
        }

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(16.dp),
        color =
            MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                width = 1.dp,
                color =
                    MaterialTheme.colorScheme.outline
            )
    ) {

        Column {

            DistrictSelectionRow(
                title =
                    district.name,
                state =
                    toggleState,
                onClick =
                    onDistrictToggle
            )

            churches.forEach { church ->

                HorizontalDivider(
                    modifier =
                        Modifier.padding(
                            start = 16.dp
                        )
                )

                ChurchSelectionRow(
                    church =
                        church,
                    checked =
                        church.id in
                                selectedChurchIds,
                    onCheckedChange = {
                        onChurchToggle(
                            church.id
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DistrictSelectionRow(
    title: String,
    state: ToggleableState,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(
                    vertical = 8.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        TriStateCheckbox(
            state =
                state,
            onClick =
                onClick
        )

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Text(
            text =
                title,
            style =
                MaterialTheme.typography.bodyLarge,
            fontWeight =
                FontWeight.SemiBold,
            color =
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ChurchSelectionRow(
    church: Church,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    SelectionRow(
        title =
            church.name,
        checked =
            checked,
        onCheckedChange =
            onCheckedChange
    )
}

@Composable
private fun SelectionRow(
    title: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onCheckedChange()
                }
                .padding(
                    vertical = 8.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Checkbox(
            checked =
                checked,
            onCheckedChange = {
                onCheckedChange()
            }
        )

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Text(
            text =
                title,
            style =
                MaterialTheme.typography.bodyLarge,
            color =
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MissionScreenContainer(
    content:
    @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                )
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
        content =
            content
    )
}

@Composable
private fun CreateMissionHeader(
    title: String,
    onBackClick: () -> Unit
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
            text =
                title
        )
    }
}

@Composable
private fun CreateMissionStepHeader(
    currentStep: Int,
    title: String,
    subtitle: String
) {
    Text(
        text =
            stringResource(
                R.string.create_mission_step,
                currentStep,
                4
            ),
        style =
            MaterialTheme.typography.labelLarge,
        color =
            MaterialTheme.colorScheme.primary,
        fontWeight =
            FontWeight.SemiBold
    )

    Spacer(
        modifier =
            Modifier.height(4.dp)
    )

    IdeScreenTitle(
        text =
            title
    )

    Spacer(
        modifier =
            Modifier.height(4.dp)
    )

    IdeScreenSubtitle(
        text =
            subtitle
    )
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
                MaterialTheme.typography.labelMedium,
            color =
                if (hasError) {
                    MaterialTheme.colorScheme.error
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClick()
                    },
            shape =
                RoundedCornerShape(16.dp),
            color =
                MaterialTheme.colorScheme.surface,
            border =
                BorderStroke(
                    width = 1.dp,
                    color =
                        if (hasError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
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
                            MaterialTheme.colorScheme.primary
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
                        MaterialTheme.typography.bodyLarge,
                    color =
                        MaterialTheme.colorScheme.onSurface
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
                    MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.error,
                modifier =
                    Modifier.padding(
                        start = 16.dp
                    )
            )
        }
    }
}

@Composable
private fun MissionFieldError(
    errorRes: Int
) {
    Text(
        text =
            stringResource(
                errorRes
            ),
        style =
            MaterialTheme.typography.bodySmall,
        color =
            MaterialTheme.colorScheme.error,
        modifier =
            Modifier.padding(
                start = 16.dp
            )
    )
}