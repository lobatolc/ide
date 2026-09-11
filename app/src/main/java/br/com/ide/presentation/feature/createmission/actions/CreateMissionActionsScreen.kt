package br.com.ide.presentation.feature.createmission.actions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeTextField
import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import br.com.ide.presentation.feature.createmission.components.CreateMissionHeader
import br.com.ide.presentation.feature.createmission.components.CreateMissionStepHeader
import br.com.ide.presentation.feature.createmission.components.MissionFieldError
import br.com.ide.presentation.feature.createmission.components.MissionScreenContainer
import br.com.ide.presentation.mapper.toStringRes

@Composable
fun CreateMissionActionsScreen(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit
) {
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
            onBackClick = {
                onEvent(
                    CreateMissionEvent.PreviousStep
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        CreateMissionStepHeader(
            stepText =
                stringResource(
                    R.string.create_mission_step,
                    3,
                    4
                ),
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
                    width = 1.dp,
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
                                        .ActivityToggled(
                                            activity
                                        )
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
            ?.let { errorRes ->

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                MissionFieldError(
                    errorRes =
                        errorRes
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
                            .CustomActivityNameChanged(
                                it
                            )
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
                            R.string.create_mission_materials_configured,
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
                ?.let { errorRes ->

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    MissionFieldError(
                        errorRes =
                            errorRes
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
                            R.string.create_mission_survey_configured,
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
                ?.let { errorRes ->

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    MissionFieldError(
                        errorRes =
                            errorRes
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
                        MaterialTheme.typography.bodyLarge,
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
                            MaterialTheme.typography.bodyMedium,
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