package br.com.ide.presentation.feature.createmission.participants

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.domain.model.Church
import br.com.ide.domain.model.District
import br.com.ide.domain.model.UserRole
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import br.com.ide.presentation.feature.createmission.components.CreateMissionHeader
import br.com.ide.presentation.feature.createmission.components.CreateMissionStepHeader
import br.com.ide.presentation.feature.createmission.components.MissionFieldError
import br.com.ide.presentation.feature.createmission.components.MissionScreenContainer
import br.com.ide.presentation.feature.createmission.components.SelectionRow

@Composable
fun CreateMissionParticipantsScreen(
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
                    2,
                    4
                ),
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
