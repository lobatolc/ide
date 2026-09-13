package br.com.ide.presentation.feature.createmission.summary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionMovement
import br.com.ide.domain.model.UserRole
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import br.com.ide.presentation.feature.createmission.components.CreateMissionHeader
import br.com.ide.presentation.feature.createmission.components.CreateMissionStepHeader
import br.com.ide.presentation.feature.createmission.components.MissionScreenContainer
import br.com.ide.presentation.mapper.toStringRes
import java.time.format.DateTimeFormatter

@Composable
fun CreateMissionSummaryScreen(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit,
    onCreateMissionClick: () -> Unit
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
                    4,
                    4
                ),
            title =
                stringResource(
                    R.string.create_mission_summary_title
                ),
            subtitle =
                stringResource(
                    R.string.create_mission_summary_subtitle
                )
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        GeneralSummaryCard(
            uiState =
                uiState,
            onEditClick = {
                onEvent(
                    CreateMissionEvent.GoToStep(1)
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        ParticipantsSummaryCard(
            uiState =
                uiState,
            onEditClick = {
                onEvent(
                    CreateMissionEvent.GoToStep(2)
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        ActionsSummaryCard(
            uiState =
                uiState,
            onEditClick = {
                onEvent(
                    CreateMissionEvent.GoToStep(3)
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(28.dp)
        )

        IdePrimaryButton(
            text =
                stringResource(
                    R.string.create_mission_confirm
                ),
            onClick =
                onCreateMissionClick,
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        TextButton(
            onClick = {
                onEvent(
                    CreateMissionEvent.GoToStep(3)
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Text(
                text =
                    stringResource(
                        R.string.create_mission_back_to_edit
                    )
            )
        }
    }
}

@Composable
private fun GeneralSummaryCard(
    uiState: CreateMissionUiState,
    onEditClick: () -> Unit
) {
    SummaryCard(
        title =
            stringResource(
                R.string.create_mission_summary_general
            ),
        onEditClick =
            onEditClick
    ) {

        SummaryValueRow(
            label =
                stringResource(
                    R.string.create_mission_name
                ),
            value =
                uiState.name
        )

        uiState.date
            ?.let { date ->

                SummaryValueRow(
                    label =
                        stringResource(
                            R.string.create_mission_date
                        ),
                    value =
                        date.format(
                            DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy"
                            )
                        )
                )
            }

        uiState.time
            ?.let { time ->

                SummaryValueRow(
                    label =
                        stringResource(
                            R.string.create_mission_time
                        ),
                    value =
                        time.format(
                            DateTimeFormatter.ofPattern(
                                "HH:mm"
                            )
                        )
                )
            }

        val movementText =
            when (uiState.movement) {

                MissionMovement.OTHER -> {
                    uiState.customMovementName
                }

                null -> {
                    "-"
                }

                else -> {
                    stringResource(
                        uiState.movement.toStringRes()
                    )
                }
            }

        SummaryValueRow(
            label =
                stringResource(
                    R.string.create_mission_movement
                ),
            value =
                movementText
        )

        if (
            uiState.description
                .isNotBlank()
        ) {

            SummaryValueRow(
                label =
                    stringResource(
                        R.string.create_mission_description
                    ),
                value =
                    uiState.description
            )
        }
    }
}

@Composable
private fun ParticipantsSummaryCard(
    uiState: CreateMissionUiState,
    onEditClick: () -> Unit
) {
    SummaryCard(
        title =
            stringResource(
                R.string.create_mission_summary_participants
            ),
        onEditClick =
            onEditClick
    ) {

        when (uiState.creatorRole) {

            UserRole.LEADER -> {

                Text(
                    text =
                        stringResource(
                            R.string
                                .create_mission_summary_leader_church
                        ),
                    style =
                        MaterialTheme.typography.labelMedium,
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

            UserRole.PASTOR,
            UserRole.ADMIN -> {

                val selectedChurches =
                    uiState.churches
                        .filter {
                            it.id in
                                    uiState.selectedChurchIds
                        }

                Text(
                    text =
                        stringResource(
                            R.string
                                .create_mission_summary_selected_churches,
                            selectedChurches.size
                        ),
                    style =
                        MaterialTheme.typography.bodyMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme.colorScheme.onSurface
                )

                if (
                    selectedChurches.isNotEmpty()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        selectedChurches
                            .forEach { church ->

                                Text(
                                    text =
                                        "• ${church.name}",
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
                }
            }

            else -> Unit
        }
    }
}

@Composable
private fun ActionsSummaryCard(
    uiState: CreateMissionUiState,
    onEditClick: () -> Unit
) {
    SummaryCard(
        title =
            stringResource(
                R.string.create_mission_summary_actions
            ),
        onEditClick =
            onEditClick
    ) {

        Text(
            text =
                stringResource(
                    R.string.create_mission_summary_activities
                ),
            style =
                MaterialTheme.typography.labelMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        FlowRow(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            uiState.selectedActivities
                .forEach { activity ->

                    val activityText =
                        when (activity) {

                            MissionActivityType.OTHER -> {
                                uiState.customActivityName
                            }

                            else -> {
                                stringResource(
                                    activity.toStringRes()
                                )
                            }
                        }

                    SummaryChip(
                        text =
                            activityText
                    )
                }
        }

        if (
            MissionActivityType.MATERIAL_DELIVERY in
            uiState.selectedActivities &&
            uiState.selectedMaterials.isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Text(
                text =
                    stringResource(
                        R.string.create_mission_materials_title
                    ),
                style =
                    MaterialTheme.typography.labelMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            FlowRow(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                uiState.selectedMaterials
                    .forEach { material ->

                        val materialText =
                            when (material) {

                                MissionMaterialType.OTHER -> {
                                    uiState.customMaterialName
                                }

                                else -> {
                                    stringResource(
                                        material.toStringRes()
                                    )
                                }
                            }

                        SummaryChip(
                            text =
                                materialText
                        )
                    }
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

            HorizontalDivider()

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            SummaryValueRow(
                label =
                    stringResource(
                        R.string.create_mission_configure_survey
                    ),
                value =
                    stringResource(
                        R.string
                            .create_mission_summary_questions_count,
                        uiState.surveyQuestions.size
                    )
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    onEditClick: () -> Unit,
    content: @Composable () -> Unit
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
                        title,
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier.weight(1f)
                )

                TextButton(
                    onClick =
                        onEditClick
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Edit,
                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(4.dp)
                    )

                    Text(
                        text =
                            stringResource(
                                R.string
                                    .create_mission_summary_edit
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            content()
        }
    }
}

@Composable
private fun SummaryValueRow(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),
        verticalAlignment =
            Alignment.Top
    ) {

        Text(
            text =
                label,
            style =
                MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.weight(0.38f)
        )

        Spacer(
            modifier =
                Modifier.width(12.dp)
        )

        Text(
            text =
                value,
            style =
                MaterialTheme.typography.bodyMedium,
            fontWeight =
                FontWeight.Medium,
            color =
                MaterialTheme.colorScheme.onSurface,
            modifier =
                Modifier.weight(0.62f)
        )
    }
}

@Composable
private fun SummaryChip(
    text: String
) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text =
                    text
            )
        }
    )
}