package br.com.ide.presentation.feature.missiongroups

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.domain.model.MissionGroup
import br.com.ide.domain.model.UserProfile
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle

private enum class EmptyGroupWarningAction {
    SAVE,
    EXIT
}

@Composable
fun MissionGroupsScreen(
    missionId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: MissionGroupsViewModel =
        hiltViewModel()
) {

    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    var showCreateDialog by
    remember {
        mutableStateOf(
            false
        )
    }

    var editingGroup by
    remember {
        mutableStateOf<MissionGroup?>(
            null
        )
    }

    var emptyGroupWarningAction by
    remember {
        mutableStateOf<EmptyGroupWarningAction?>(
            null
        )
    }

    val emptyGroups =
        uiState.groups
            .filter {
                it.participantIds.isEmpty()
            }

    val emptyGroupNames =
        emptyGroups
            .joinToString(
                separator = ", "
            ) { group ->
                "“${group.name}”"
            }

    val handleBackClick: () -> Unit = {
        if (
            emptyGroups.isNotEmpty()
        ) {
            emptyGroupWarningAction =
                EmptyGroupWarningAction.EXIT
        } else {
            onBackClick()
        }
    }

    BackHandler(
        enabled =
            !uiState.isLoading
    ) {
        handleBackClick()
    }

    LaunchedEffect(
        missionId
    ) {

        viewModel.load(
            missionId
        )
    }

    LaunchedEffect(
        uiState.isSaved
    ) {

        if (
            uiState.isSaved
        ) {
            onSaved()
        }
    }

    if (
        uiState.isLoading
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {

            CircularProgressIndicator()
        }

        return
    }

    MissionGroupsContent(
        uiState =
            uiState,

        onBackClick =
            handleBackClick,

        onCreateGroupClick = {

            showCreateDialog =
                true
        },

        onEditGroupClick = { group ->

            editingGroup =
                group
        },

        onSaveClick = {

            if (
                emptyGroups.isNotEmpty()
            ) {
                emptyGroupWarningAction =
                    EmptyGroupWarningAction.SAVE
            } else {
                viewModel.onEvent(
                    MissionGroupsEvent.Save
                )
            }
        }
    )

    emptyGroupWarningAction
        ?.let { action ->

            val isExitWarning =
                action ==
                        EmptyGroupWarningAction.EXIT

            AlertDialog(
                onDismissRequest = {
                    emptyGroupWarningAction =
                        null
                },
                icon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Groups,
                        contentDescription =
                            null,
                        tint =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                },
                title = {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .mission_groups_empty_group_title
                            )
                    )
                },
                text = {
                    Text(
                        text =
                            stringResource(
                                if (
                                    isExitWarning
                                ) {
                                    R.string
                                        .mission_groups_empty_group_exit_message
                                } else {
                                    R.string
                                        .mission_groups_empty_group_save_message
                                },
                                emptyGroupNames
                            )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            emptyGroupWarningAction =
                                null

                            if (
                                isExitWarning
                            ) {
                                onBackClick()
                            }
                        }
                    ) {
                        Text(
                            text =
                                stringResource(
                                    if (
                                        isExitWarning
                                    ) {
                                        R.string
                                            .mission_groups_empty_group_leave
                                    } else {
                                        R.string
                                            .mission_groups_empty_group_ok
                                    }
                                )
                        )
                    }
                },
                dismissButton =
                    if (
                        isExitWarning
                    ) {
                        {
                            TextButton(
                                onClick = {
                                    emptyGroupWarningAction =
                                        null
                                }
                            ) {
                                Text(
                                    text =
                                        stringResource(
                                            R.string
                                                .mission_groups_empty_group_keep_editing
                                        )
                                )
                            }
                        }
                    } else {
                        null
                    }
            )
        }

    // =========================================================
    // Criar grupo
    // =========================================================

    if (
        showCreateDialog
    ) {

        CreateGroupDialog(
            onDismiss = {

                showCreateDialog =
                    false
            },

            onConfirm = { name ->

                viewModel.onEvent(
                    MissionGroupsEvent
                        .CreateGroup(
                            name =
                                name
                        )
                )

                showCreateDialog =
                    false
            }
        )
    }

    // =========================================================
    // Editar grupo
    // =========================================================

    editingGroup
        ?.let { group ->

            val currentGroup =
                uiState.groups
                    .firstOrNull {
                        it.id ==
                                group.id
                    }
                    ?: group

            EditGroupDialog(
                group =
                    currentGroup,

                availableParticipants =
                    availableParticipantsForGroup(
                        uiState =
                            uiState,

                        group =
                            currentGroup
                    ),

                onDismiss = {

                    editingGroup =
                        null
                },

                onConfirm = {
                        name,
                        selectedParticipantIds,
                        supportUserId ->

                    viewModel.onEvent(
                        MissionGroupsEvent
                            .RenameGroup(
                                groupId =
                                    currentGroup.id,

                                name =
                                    name
                            )
                    )

                    currentGroup
                        .participantIds
                        .filterNot {
                            it in
                                    selectedParticipantIds
                        }
                        .forEach { participantId ->

                            viewModel.onEvent(
                                MissionGroupsEvent
                                    .RemoveParticipant(
                                        groupId =
                                            currentGroup.id,

                                        participantId =
                                            participantId
                                    )
                            )
                        }

                    selectedParticipantIds
                        .forEach { participantId ->

                            viewModel.onEvent(
                                MissionGroupsEvent
                                    .AddParticipant(
                                        groupId =
                                            currentGroup.id,

                                        participantId =
                                            participantId
                                    )
                            )
                        }

                    viewModel.onEvent(
                        MissionGroupsEvent
                            .SetSupport(
                                groupId =
                                    currentGroup.id,

                                participantId =
                                    supportUserId
                            )
                    )

                    editingGroup =
                        null
                },

                onDelete = {

                    viewModel.onEvent(
                        MissionGroupsEvent
                            .DeleteGroup(
                                groupId =
                                    currentGroup.id
                            )
                    )

                    editingGroup =
                        null
                }
            )
        }
}

@Composable
private fun MissionGroupsContent(
    uiState: MissionGroupsUiState,
    onBackClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onEditGroupClick: (MissionGroup) -> Unit,
    onSaveClick: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                )
    ) {

        // =====================================================
        // Header
        // =====================================================

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
                        R.string
                            .mission_groups_back
                    )
            )

            Spacer(
                modifier =
                    Modifier.size(
                        8.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                IdeScreenTitle(
                    text =
                        stringResource(
                            R.string
                                .mission_groups_title
                        )
                )

                if (
                    uiState.missionName
                        .isNotBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                2.dp
                            )
                    )

                    IdeScreenSubtitle(
                        text =
                            uiState.missionName
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    26.dp
                )
        )

        // =====================================================
        // Introdução
        // =====================================================

        Text(
            text =
                stringResource(
                    R.string
                        .mission_groups_section_title
                ),
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )

        Text(
            text =
                stringResource(
                    R.string
                        .mission_groups_section_description
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )

        // =====================================================
        // Criar grupo
        // =====================================================

        if (
            uiState.canEdit
        ) {

            Button(
                onClick =
                    onCreateGroupClick,
                enabled =
                    uiState
                        .unassignedParticipants
                        .isNotEmpty(),
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(
                        16.dp
                    ),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Add,
                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.size(
                            8.dp
                        )
                )

                Text(
                    text =
                        if (
                            uiState
                                .unassignedParticipants
                                .isEmpty()
                        ) {
                            stringResource(
                                R.string
                                    .mission_groups_all_assigned
                            )
                        } else {
                            stringResource(
                                R.string
                                    .mission_groups_create_group
                            )
                        },
                    fontWeight =
                        FontWeight.SemiBold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        22.dp
                    )
            )
        }

        // =====================================================
        // Nenhum grupo
        // =====================================================

        if (
            uiState.groups.isEmpty()
        ) {

            EmptyGroupsCard(
                participantCount =
                    uiState
                        .eligibleParticipants
                        .size
            )

        } else {

            uiState.groups
                .forEachIndexed {
                        index,
                        group ->

                    GroupCard(
                        group =
                            group,

                        participants =
                            group
                                .participantIds
                                .mapNotNull { participantId ->

                                    uiState
                                        .eligibleParticipants
                                        .firstOrNull {
                                            it.id ==
                                                    participantId
                                        }
                                },

                        support =
                            uiState
                                .eligibleParticipants
                                .firstOrNull {
                                    it.id ==
                                            group
                                                .supportUserId
                                },

                        canEdit =
                            uiState.canEdit,

                        onEditClick = {

                            onEditGroupClick(
                                group
                            )
                        }
                    )

                    if (
                        index !=
                        uiState.groups
                            .lastIndex
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    14.dp
                                )
                        )
                    }
                }
        }

        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )

        // =====================================================
        // Sem grupo
        // =====================================================

        UnassignedParticipantsSection(
            participants =
                uiState
                    .unassignedParticipants
        )

        uiState.errorMessage
            ?.let { errorRes ->

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            errorRes
                        ),
                    color =
                        MaterialTheme
                            .colorScheme
                            .error,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )
            }

        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )

        // =====================================================
        // Salvar
        // =====================================================

        if (
            uiState.canEdit
        ) {

            IdePrimaryButton(
                text =
                    stringResource(
                        R.string
                            .mission_groups_save
                    ),
                onClick =
                    onSaveClick,
                isLoading =
                    uiState.isSaving,
                enabled =
                    !uiState.isSaving,
                modifier =
                    Modifier.fillMaxWidth()
            )

        } else {

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(
                        18.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
            ) {

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_groups_read_only
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    modifier =
                        Modifier.padding(
                            16.dp
                        )
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    24.dp
                )
        )
    }
}

@Composable
private fun EmptyGroupsCard(
    participantCount: Int
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        22.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Surface(
                shape =
                    CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Groups,
                    contentDescription =
                        null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onPrimary,
                    modifier =
                        Modifier
                            .padding(
                                12.dp
                            )
                            .size(
                                28.dp
                            )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_groups_empty_title
                    ),
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_groups_empty_description,
                        participantCount
                    ),
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

@Composable
private fun GroupCard(
    group: MissionGroup,
    participants: List<UserProfile>,
    support: UserProfile?,
    canEdit: Boolean,
    onEditClick: () -> Unit
) {

    val groupColor =
        parseGroupColor(
            group.colorHex
        )

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                22.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        18.dp
                    )
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(
                                16.dp
                            )
                            .background(
                                color =
                                    groupColor,
                                shape =
                                    CircleShape
                            )
                )

                Spacer(
                    modifier =
                        Modifier.size(
                            10.dp
                        )
                )

                Text(
                    text =
                        group.name,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                if (
                    canEdit
                ) {

                    TextButton(
                        onClick =
                            onEditClick
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Edit,
                            contentDescription =
                                null,
                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )

                        Spacer(
                            modifier =
                                Modifier.size(
                                    5.dp
                                )
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_groups_edit
                                )
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            if (
                participants.isEmpty()
            ) {

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_groups_no_participants
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

            } else {

                participants
                    .forEach { participant ->

                        ParticipantRow(
                            participant =
                                participant,

                            isSupport =
                                participant.id ==
                                        support?.id,

                            groupColor =
                                groupColor
                        )
                    }
            }

            if (
                support != null
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                Surface(
                    shape =
                        RoundedCornerShape(
                            50
                        ),
                    color =
                        groupColor
                            .copy(
                                alpha = 0.14f
                            )
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined
                                    .VolunteerActivism,
                            contentDescription =
                                null,
                            tint =
                                groupColor,
                            modifier =
                                Modifier.size(
                                    16.dp
                                )
                        )

                        Spacer(
                            modifier =
                                Modifier.size(
                                    6.dp
                                )
                        )

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_groups_support_name,
                                    userFullName(
                                        support
                                    )
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium,
                            fontWeight =
                                FontWeight.SemiBold,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: UserProfile,
    isSupport: Boolean,
    groupColor: Color
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 5.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Surface(
            modifier =
                Modifier.size(
                    32.dp
                ),
            shape =
                CircleShape,
            color =
                groupColor
                    .copy(
                        alpha = 0.14f
                    )
        ) {

            Box(
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Person,
                    contentDescription =
                        null,
                    tint =
                        groupColor,
                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )
            }
        }

        Spacer(
            modifier =
                Modifier.size(
                    10.dp
                )
        )

        Text(
            text =
                userFullName(
                    participant
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface,
            modifier =
                Modifier.weight(
                    1f
                )
        )

        if (
            isSupport
        ) {

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_groups_support
                    ),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    groupColor
            )
        }
    }
}

@Composable
private fun UnassignedParticipantsSection(
    participants: List<UserProfile>
) {

    Text(
        text =
            stringResource(
                R.string
                    .mission_groups_unassigned_title
            ),
        style =
            MaterialTheme
                .typography
                .titleLarge,
        fontWeight =
            FontWeight.Bold,
        color =
            MaterialTheme
                .colorScheme
                .onBackground
    )

    Spacer(
        modifier =
            Modifier.height(
                6.dp
            )
    )

    Text(
        text =
            if (
                participants.isEmpty()
            ) {
                stringResource(
                    R.string
                        .mission_groups_unassigned_empty
                )
            } else {
                stringResource(
                    R.string
                        .mission_groups_unassigned_description
                )
            },
        style =
            MaterialTheme
                .typography
                .bodyMedium,
        color =
            MaterialTheme
                .colorScheme
                .onSurfaceVariant
    )

    if (
        participants.isNotEmpty()
    ) {

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )

        Card(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(
                    20.dp
                ),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surface
                )
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            14.dp
                        )
            ) {

                participants
                    .forEachIndexed {
                            index,
                            participant ->

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 8.dp
                                    ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Outlined.Person,
                                contentDescription =
                                    null,
                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(
                                        10.dp
                                    )
                            )

                            Text(
                                text =
                                    userFullName(
                                        participant
                                    ),
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurface
                            )
                        }

                        if (
                            index !=
                            participants
                                .lastIndex
                        ) {

                            HorizontalDivider()
                        }
                    }
            }
        }
    }
}

@Composable
private fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {

    var name by
    remember {
        mutableStateOf(
            ""
        )
    }

    Dialog(
        onDismissRequest =
            onDismiss
    ) {

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(
                    28.dp
                ),
            color =
                MaterialTheme
                    .colorScheme
                    .surface
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        22.dp
                    )
            ) {

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_groups_create_dialog_title
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_groups_create_dialog_description
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                OutlinedTextField(
                    value =
                        name,
                    onValueChange = {
                        name =
                            it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_groups_name
                                )
                        )
                    },
                    singleLine =
                        true
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            22.dp
                        )
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.End
                ) {

                    TextButton(
                        onClick =
                            onDismiss
                    ) {

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_groups_cancel
                                )
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.size(
                                8.dp
                            )
                    )

                    Button(
                        onClick = {

                            onConfirm(
                                name
                            )
                        },
                        enabled =
                            name
                                .isNotBlank()
                    ) {

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_groups_create
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditGroupDialog(
    group: MissionGroup,
    availableParticipants: List<UserProfile>,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        participantIds: List<String>,
        supportUserId: String?
    ) -> Unit,
    onDelete: () -> Unit
) {

    var name by
    remember(
        group.id
    ) {
        mutableStateOf(
            group.name
        )
    }

    var selectedParticipantIds by
    remember(
        group.id
    ) {
        mutableStateOf(
            group
                .participantIds
                .toSet()
        )
    }

    var supportUserId by
    remember(
        group.id
    ) {
        mutableStateOf(
            group.supportUserId
        )
    }

    Dialog(
        onDismissRequest =
            onDismiss
    ) {

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        max = 680.dp
                    ),
            shape =
                RoundedCornerShape(
                    28.dp
                ),
            color =
                MaterialTheme
                    .colorScheme
                    .surface
        ) {

            Column(
                modifier =
                    Modifier
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(
                            22.dp
                        )
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier =
                            Modifier
                                .size(
                                    18.dp
                                )
                                .background(
                                    parseGroupColor(
                                        group.colorHex
                                    ),
                                    CircleShape
                                )
                    )

                    Spacer(
                        modifier =
                            Modifier.size(
                                10.dp
                            )
                    )

                    Text(
                        text =
                            stringResource(
                                R.string
                                    .mission_groups_edit_dialog_title
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .titleLarge,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                OutlinedTextField(
                    value =
                        name,
                    onValueChange = {
                        name =
                            it
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    label = {

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_groups_name
                                )
                        )
                    },
                    singleLine =
                        true
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            22.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_groups_participants_title
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            5.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_groups_participants_description
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                availableParticipants
                    .forEach { participant ->

                        val selected =
                            participant.id in
                                    selectedParticipantIds

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        selectedParticipantIds =
                                            if (
                                                selected
                                            ) {

                                                selectedParticipantIds -
                                                        participant.id

                                            } else {

                                                selectedParticipantIds +
                                                        participant.id
                                            }

                                        if (
                                            supportUserId ==
                                            participant.id &&
                                            selected
                                        ) {

                                            supportUserId =
                                                null
                                        }
                                    }
                                    .padding(
                                        vertical = 4.dp
                                    ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked =
                                    selected,
                                onCheckedChange = {
                                        checked ->

                                    selectedParticipantIds =
                                        if (
                                            checked
                                        ) {

                                            selectedParticipantIds +
                                                    participant.id

                                        } else {

                                            selectedParticipantIds -
                                                    participant.id
                                        }

                                    if (
                                        !checked &&
                                        supportUserId ==
                                        participant.id
                                    ) {

                                        supportUserId =
                                            null
                                    }
                                }
                            )

                            Text(
                                text =
                                    userFullName(
                                        participant
                                    ),
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium
                            )
                        }
                    }

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                HorizontalDivider()

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_groups_support_title
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            5.dp
                        )
                )

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_groups_support_description
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                SupportOption(
                    selected =
                        supportUserId ==
                                null,
                    text =
                        stringResource(
                            R.string
                                .mission_groups_support_none
                        ),
                    onClick = {

                        supportUserId =
                            null
                    }
                )

                availableParticipants
                    .filter {
                        it.id in
                                selectedParticipantIds
                    }
                    .forEach { participant ->

                        SupportOption(
                            selected =
                                supportUserId ==
                                        participant.id,

                            text =
                                userFullName(
                                    participant
                                ),

                            onClick = {

                                supportUserId =
                                    participant.id
                            }
                        )
                    }

                Spacer(
                    modifier =
                        Modifier.height(
                            22.dp
                        )
                )

                TextButton(
                    onClick =
                        onDelete,
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.DeleteOutline,
                        contentDescription =
                            null
                    )

                    Spacer(
                        modifier =
                            Modifier.size(
                                6.dp
                            )
                    )

                    Text(
                        text =
                            stringResource(
                                R.string
                                    .mission_groups_delete
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.End
                ) {

                    TextButton(
                        onClick =
                            onDismiss
                    ) {

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_groups_cancel
                                )
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.size(
                                8.dp
                            )
                    )

                    Button(
                        onClick = {

                            onConfirm(
                                name.trim(),
                                selectedParticipantIds
                                    .toList(),
                                supportUserId
                            )
                        },
                        enabled =
                            name
                                .isNotBlank()
                    ) {

                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_groups_confirm
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportOption(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                )
                .padding(
                    vertical = 3.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        RadioButton(
            selected =
                selected,
            onClick =
                onClick
        )

        Text(
            text =
                text,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}

private fun availableParticipantsForGroup(
    uiState: MissionGroupsUiState,
    group: MissionGroup
): List<UserProfile> {

    val allowedIds =
        uiState
            .unassignedParticipants
            .map {
                it.id
            }
            .toSet() +
                group.participantIds

    return uiState
        .eligibleParticipants
        .filter {
            it.id in allowedIds
        }
}

private fun userFullName(
    user: UserProfile
): String {

    return listOf(
        user.firstName,
        user.lastName
    )
        .filter {
            it.isNotBlank()
        }
        .joinToString(
            " "
        )
}

private fun parseGroupColor(
    hex: String
): Color {

    val normalized =
        hex
            .removePrefix(
                "#"
            )

    val rgb =
        normalized
            .toLongOrNull(
                16
            )
            ?.toInt()
            ?: 0x2EC4B6

    return Color(
        0xFF000000
            .toInt() or
                rgb
    )
}
