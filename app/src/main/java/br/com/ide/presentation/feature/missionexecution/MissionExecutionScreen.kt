package br.com.ide.presentation.feature.missionexecution

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.os.Build
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.domain.model.MissionEncounter
import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.model.UserRole
import br.com.ide.presentation.components.map.MissionParticipantMarker
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.map.MissionMap
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun MissionExecutionScreen(
    missionId: String,
    focusParticipantUserId: String? = null,
    onFocusParticipantHandled: () -> Unit = {},
    onBackClick: () -> Unit,
    onRegisterEncounterClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onFinishMissionClick: () -> Unit,
    onMetricsClick: () -> Unit = {},
    onGeneralMetricsClick: () -> Unit = {},
    onFilterGroupsClick: () -> Unit = {},
    onRequestSupportClick: () -> Unit = {},
    onEndParticipationClick: () -> Unit = {},
    viewModel: MissionExecutionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val endParticipationErrorMessage =
        uiState
            .endParticipationErrorMessage
            ?.let { messageRes ->
                stringResource(
                    messageRes
                )
            }

    LaunchedEffect(
        uiState.participationEnded
    ) {
        if (
            uiState.participationEnded
        ) {
            viewModel
                .consumeParticipationEnded()

            onEndParticipationClick()
        }
    }

    LaunchedEffect(
        uiState.missionFinished
    ) {
        if (
            uiState.missionFinished
        ) {
            viewModel.consumeMissionFinished()

            onBackClick()
        }
    }

    LaunchedEffect(
        uiState.participationAccessDenied
    ) {
        if (
            uiState.participationAccessDenied
        ) {
            viewModel
                .consumeParticipationAccessDenied()

            onEndParticipationClick()
        }
    }

    LaunchedEffect(
        endParticipationErrorMessage
    ) {
        endParticipationErrorMessage
            ?.let { message ->
                Toast
                    .makeText(
                        context,
                        message,
                        Toast.LENGTH_LONG
                    )
                    .show()

                viewModel
                    .consumeEndParticipationError()
            }
    }

    fun hasLocationPermission(): Boolean {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fineGranted ||
                coarseGranted
    }

    var locationPermissionGranted by
    remember {
        mutableStateOf(
            hasLocationPermission()
        )
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||
                        permissions[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true

            locationPermissionGranted =
                granted
        }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestPermission()
        ) {
            /*
             * A missão continua funcionando mesmo que
             * a permissão de notificações seja negada.
             */
        }

    LaunchedEffect(missionId) {
        viewModel.load(missionId)

        locationPermissionGranted =
            hasLocationPermission()

        if (
            !locationPermissionGranted
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    LaunchedEffect(
        uiState.missionId,
        locationPermissionGranted
    ) {

        if (
            uiState.missionId ==
            missionId &&
            locationPermissionGranted
        ) {
            viewModel.startLocationTracking()
        }
    }

    DisposableEffect(lifecycleOwner, missionId) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                event ==
                Lifecycle.Event.ON_RESUME
            ) {

                locationPermissionGranted =
                    hasLocationPermission()

                viewModel.refresh(
                    missionId
                )
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (uiState.isLoading && uiState.missionName.isBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val errorMessage = uiState.errorMessage

    if (errorMessage != null) {
        MissionExecutionError(
            message = stringResource(errorMessage),
            onBackClick = onBackClick
        )
        return
    }

    MissionExecutionContent(
        uiState = uiState,
        focusParticipantUserId =
            focusParticipantUserId,
        onFocusParticipantHandled =
            onFocusParticipantHandled,
        onBackClick = onBackClick,
        onRegisterEncounterClick = onRegisterEncounterClick,
        onFinishMissionClick =
            viewModel::finishMission,
        onMetricsClick = onMetricsClick,
        onGeneralMetricsClick = onGeneralMetricsClick,
        onFilterGroupsClick = onFilterGroupsClick,
        onApplyMapGroupFilter =
            viewModel::applyMapGroupFilter,
        onClearMapGroupFilter =
            viewModel::clearMapGroupFilter,
        onRequestSupportClick = {
            viewModel.requestSupport()
            onRequestSupportClick()
        },
        onConfirmSupportReceived =
            viewModel::confirmSupportReceived,
        isEndingParticipation =
            uiState.isEndingParticipation,
        onEndParticipationClick =
            viewModel::endParticipation
    )
}

@Composable
private fun MissionExecutionContent(
    uiState: MissionExecutionUiState,
    focusParticipantUserId: String?,
    onFocusParticipantHandled: () -> Unit,
    onBackClick: () -> Unit,
    onRegisterEncounterClick: () -> Unit,
    onFinishMissionClick: () -> Unit,
    onMetricsClick: () -> Unit,
    onGeneralMetricsClick: () -> Unit,
    onFilterGroupsClick: () -> Unit,
    onApplyMapGroupFilter: (
        Set<String>,
        Boolean
    ) -> Unit,
    onClearMapGroupFilter: () -> Unit,
    onRequestSupportClick: () -> Unit,
    onConfirmSupportReceived: () -> Unit,
    isEndingParticipation: Boolean,
    onEndParticipationClick: () -> Unit
) {
    val context =
        LocalContext.current

    val isDarkTheme =
        MaterialTheme.colorScheme.background.luminance() < 0.5f

    var recenterKey by remember { mutableIntStateOf(0) }
    var speedDialExpanded by remember { mutableStateOf(false) }
    var headerMenuExpanded by remember { mutableStateOf(false) }
    var showMyGroup by remember { mutableStateOf(false) }
    var showGroupFilter by remember { mutableStateOf(false) }
    var showSupportStatusSheet by remember { mutableStateOf(false) }
    var showEndParticipationConfirmation by
    remember {
        mutableStateOf(
            false
        )
    }

    var showFinishMissionConfirmation by
    remember {
        mutableStateOf(
            false
        )
    }

    var localFocusParticipantUserId by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    var selectedParticipantUserId by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    var selectedEncounterId by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    val visibleParticipantMarkers =
        if (
            uiState.isMapGroupFilterActive
        ) {
            uiState
                .participantMarkers
                .filter { marker ->
                    marker.groupId
                        ?.let {
                            it in
                                    uiState.selectedMapGroupIds
                        }
                        ?: uiState.includeUngroupedOnMap
                }
        } else {
            uiState.participantMarkers
        }

    val visibleParticipantTracks =
        if (
            uiState.isMapGroupFilterActive
        ) {
            uiState
                .participantTracks
                .filter { track ->
                    track.groupId
                        ?.let {
                            it in
                                    uiState.selectedMapGroupIds
                        }
                        ?: uiState.includeUngroupedOnMap
                }
        } else {
            uiState.participantTracks
        }

    val selectedParticipant =
        selectedParticipantUserId
            ?.let { userId ->
                visibleParticipantMarkers
                    .firstOrNull {
                        it.userId ==
                                userId
                    }
            }

    val selectedEncounter =
        selectedEncounterId
            ?.let { encounterId ->
                uiState.encounters
                    .firstOrNull {
                        it.id ==
                                encounterId
                    }
            }

    val mapLatitude =
        uiState.currentLatitude
            ?: uiState.departureLatitude
            ?: uiState.areaPoints.firstOrNull()?.latitude
            ?: -1.2939

    val mapLongitude =
        uiState.currentLongitude
            ?: uiState.departureLongitude
            ?: uiState.areaPoints.firstOrNull()?.longitude
            ?: -47.9260

    Box(modifier = Modifier.fillMaxSize()) {
        MissionMap(
            latitude = mapLatitude,
            longitude = mapLongitude,
            zoom = 15.0,
            selectedLatitude =
                uiState.currentLatitude ?: uiState.departureLatitude,
            selectedLongitude =
                uiState.currentLongitude ?: uiState.departureLongitude,
            polygonPoints = uiState.areaPoints,
            participantMarkers = visibleParticipantMarkers,
            encounterMarkers = uiState.encounterMarkers,
            participantTracks = visibleParticipantTracks,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.fillMaxSize(),
            recenterKey = recenterKey,
            focusParticipantUserId =
                localFocusParticipantUserId
                    ?: focusParticipantUserId,
            onFocusParticipantHandled = {

                if (
                    localFocusParticipantUserId != null
                ) {
                    localFocusParticipantUserId =
                        null
                } else {
                    onFocusParticipantHandled()
                }
            },
            onParticipantClick = { participant ->

                selectedEncounterId =
                    null

                selectedParticipantUserId =
                    participant.userId
            },
            onEncounterClick = { encounterId ->

                selectedParticipantUserId =
                    null

                selectedEncounterId =
                    encounterId
            }
        )

        MissionExecutionHeader(
            missionName = uiState.missionName,
            elapsedSeconds = uiState.elapsedSeconds,
            canViewGeneralMetrics =
                uiState.canViewGeneralMetrics,
            canManageMission = uiState.canFinishMission,
            isGroupFilterActive =
                uiState.isMapGroupFilterActive,
            menuExpanded = headerMenuExpanded,
            onMenuExpandedChange = {
                headerMenuExpanded = it
            },
            onBackClick = onBackClick,
            onGeneralMetricsClick = {
                headerMenuExpanded = false
                onGeneralMetricsClick()
            },
            onFilterGroupsClick = {
                headerMenuExpanded = false
                showGroupFilter = true
                onFilterGroupsClick()
            },
            onFinishMissionClick = {
                headerMenuExpanded = false
                showFinishMissionConfirmation =
                    true
            },
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 10.dp
                    )
        )

        MissionExecutionActionCluster(
            recenterContentDescription =
                stringResource(
                    R.string.mission_execution_recenter
                ),
            onRecenterClick = {
                recenterKey++
            },
            expanded = speedDialExpanded,
            onExpandedChange = {
                speedDialExpanded = it
            },
            onMyGroupClick = {
                speedDialExpanded = false
                showMyGroup =
                    true
            },
            onMetricsClick = {
                speedDialExpanded = false
                onMetricsClick()
            },
            onNewEncounterClick = {
                speedDialExpanded = false
                onRegisterEncounterClick()
            },
            isSupportRequested =
                uiState.isSupportRequested,
            isUpdatingSupportStatus =
                uiState.isUpdatingSupportStatus,
            isEndingParticipation =
                isEndingParticipation,
            onRequestSupportClick = {
                speedDialExpanded = false

                if (
                    uiState.isSupportRequested
                ) {
                    showSupportStatusSheet =
                        true
                } else {
                    onRequestSupportClick()

                    /*
                     * O pedido é disparado e o usuário recebe imediatamente
                     * um retorno visual claro sobre o que acabou de fazer.
                     *
                     * O estado real continua vindo do listener em tempo real.
                     * Se a atualização falhar, o botão do menu continuará
                     * refletindo o estado persistido.
                     */
                    showSupportStatusSheet =
                        true
                }
            },
            onEndParticipationClick = {
                speedDialExpanded = false
                showEndParticipationConfirmation =
                    true
            },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(
                        end = 16.dp,
                        bottom = 16.dp
                    )
        )
    }

    if (
        showSupportStatusSheet
    ) {

        SupportRequestedBottomSheet(
            isUpdatingSupportStatus =
                uiState.isUpdatingSupportStatus,
            onDismiss = {
                if (
                    !uiState.isUpdatingSupportStatus
                ) {
                    showSupportStatusSheet =
                        false
                }
            },
            onSupportReceivedClick = {
                onConfirmSupportReceived()

                /*
                 * Fecha após a confirmação. Caso a atualização remota falhe,
                 * o listener manterá isSupportRequested = true e o usuário
                 * poderá abrir o bottom sheet novamente pelo menu.
                 */
                showSupportStatusSheet =
                    false
            }
        )
    }

    if (
        showMyGroup
    ) {

        MyGroupBottomSheet(
            groupName =
                uiState.currentGroupName,
            groupColorHex =
                uiState.currentGroupColorHex,
            members =
                uiState.currentGroupMembers,
            onDismiss = {
                showMyGroup =
                    false
            },
            onMemberClick = { member ->

                val markerExists =
                    uiState.participantMarkers
                        .any {
                            it.userId ==
                                    member.userId
                        }

                if (
                    markerExists
                ) {
                    showMyGroup =
                        false

                    localFocusParticipantUserId =
                        member.userId
                }
            }
        )
    }

    if (
        showGroupFilter
    ) {
        MapGroupFilterDialog(
            groups =
                uiState.mapGroupFilters,
            selectedGroupIds =
                uiState.selectedMapGroupIds,
            includeUngrouped =
                uiState.includeUngroupedOnMap,
            isFilterActive =
                uiState.isMapGroupFilterActive,
            /*
             * groupId == null aparece como Grupo Geral na própria lista de
             * grupos; não existe mais uma opção paralela "Sem grupo".
             */
            ungroupedParticipantCount =
                0,
            showUngroupedOption =
                false,
            onDismiss = {
                showGroupFilter = false
            },
            onApply = {
                    selectedGroupIds,
                    includeUngrouped ->

                onApplyMapGroupFilter(
                    selectedGroupIds,
                    includeUngrouped
                )

                showGroupFilter = false
            },
            onClear = {
                onClearMapGroupFilter()
                showGroupFilter = false
            }
        )
    }

    if (
        showFinishMissionConfirmation
    ) {
        AlertDialog(
            onDismissRequest = {
                if (
                    !uiState.isFinishing
                ) {
                    showFinishMissionConfirmation =
                        false
                }
            },
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.Flag,
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
                                .mission_finish_title
                        )
                )
            },
            text = {
                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_finish_message
                        )
                )
            },
            confirmButton = {
                TextButton(
                    onClick =
                        onFinishMissionClick,
                    enabled =
                        !uiState.isFinishing
                ) {
                    Text(
                        text =
                            stringResource(
                                if (
                                    uiState.isFinishing
                                ) {
                                    R.string
                                        .mission_finish_finishing
                                } else {
                                    R.string
                                        .mission_finish_confirm
                                }
                            ),
                        color =
                            if (
                                uiState.isFinishing
                            ) {
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .error
                            }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showFinishMissionConfirmation =
                            false
                    },
                    enabled =
                        !uiState.isFinishing
                ) {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .mission_finish_cancel
                            )
                    )
                }
            }
        )
    }

    if (
        showEndParticipationConfirmation
    ) {
        AlertDialog(
            onDismissRequest = {
                if (
                    !isEndingParticipation
                ) {
                    showEndParticipationConfirmation =
                        false
                }
            },
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.Logout,
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
                                .mission_end_participation_title
                        )
                )
            },
            text = {
                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_end_participation_message
                        )
                )
            },
            confirmButton = {
                TextButton(
                    onClick =
                        onEndParticipationClick,
                    enabled =
                        !isEndingParticipation
                ) {
                    Text(
                        text =
                            stringResource(
                                if (
                                    isEndingParticipation
                                ) {
                                    R.string
                                        .mission_end_participation_ending
                                } else {
                                    R.string
                                        .mission_end_participation_confirm
                                }
                            ),
                        color =
                            if (
                                isEndingParticipation
                            ) {
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .error
                            }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndParticipationConfirmation =
                            false
                    },
                    enabled =
                        !isEndingParticipation
                ) {
                    Text(
                        text =
                            stringResource(
                                R.string
                                    .mission_end_participation_cancel
                            )
                    )
                }
            }
        )
    }

    val customActivityPerformedLabel =
        uiState
            .customActivityName
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { customActivityName ->

                val genericOtherActivityLabel =
                    stringResource(
                        R.string
                            .new_encounter_activity_other
                    )

                val performedSuffix =
                    genericOtherActivityLabel
                        .substringAfterLast(
                            " ",
                            missingDelimiterValue =
                                ""
                        )
                        .trim()

                if (
                    performedSuffix.isNotBlank()
                ) {
                    "$customActivityName $performedSuffix"
                } else {
                    customActivityName
                }
            }

    /*
     * Se o encontro for removido ou deixar de existir enquanto
     * estiver selecionado, limpamos a seleção para não manter
     * uma referência antiga na tela.
     */
    LaunchedEffect(
        selectedEncounterId,
        selectedEncounter
    ) {

        if (
            selectedEncounterId != null &&
            selectedEncounter == null
        ) {
            selectedEncounterId =
                null
        }
    }

    if (
        selectedParticipant != null
    ) {

        ParticipantDetailsBottomSheet(
            participant =
                selectedParticipant,
            onDismiss = {
                selectedParticipantUserId =
                    null
            },
            onTraceRouteClick = {
                openParticipantInMapApp(
                    context =
                        context,
                    participant =
                        selectedParticipant
                )
            }
        )
    }

    if (
        selectedEncounter != null
    ) {

        EncounterDetailsBottomSheet(
            encounter =
                selectedEncounter,
            onDismiss = {
                selectedEncounterId =
                    null
            },
            onWhatsAppClick = {
                openEncounterInWhatsApp(
                    context =
                        context,
                    encounter =
                        selectedEncounter
                )
            },
            onTraceRouteClick = {
                openEncounterInMapApp(
                    context =
                        context,
                    encounter =
                        selectedEncounter
                )
            },
            customActivityPerformedLabel =
                customActivityPerformedLabel
        )
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupportRequestedBottomSheet(
    isUpdatingSupportStatus: Boolean,
    onDismiss: () -> Unit,
    onSupportReceivedClick: () -> Unit
) {

    ModalBottomSheet(
        onDismissRequest = {
            if (
                !isUpdatingSupportStatus
            ) {
                onDismiss()
            }
        }
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 28.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            Surface(
                modifier =
                    Modifier.size(
                        52.dp
                    ),
                shape =
                    CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .errorContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Sos,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(
                                27.dp
                            ),
                        tint =
                            MaterialTheme
                                .colorScheme
                                .onErrorContainer
                    )
                }
            }

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_support_sheet_title
                    ),
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_support_sheet_message
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Button(
                onClick =
                    onSupportReceivedClick,
                enabled =
                    !isUpdatingSupportStatus,
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                if (
                    isUpdatingSupportStatus
                ) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                18.dp
                            ),
                        strokeWidth =
                            2.dp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onPrimary
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                8.dp
                            )
                    )
                }

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_execution_support_received
                        )
                )
            }

            TextButton(
                onClick =
                    onDismiss,
                enabled =
                    !isUpdatingSupportStatus,
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_support_sheet_close
                        )
                )
            }
        }
    }
}


@Composable
private fun MapGroupFilterDialog(
    groups: List<MissionMapGroupFilterUiModel>,
    selectedGroupIds: Set<String>,
    includeUngrouped: Boolean,
    isFilterActive: Boolean,
    ungroupedParticipantCount: Int,
    showUngroupedOption: Boolean,
    onDismiss: () -> Unit,
    onApply: (
        Set<String>,
        Boolean
    ) -> Unit,
    onClear: () -> Unit
) {

    val allGroupIds =
        groups
            .map {
                it.id
            }
            .toSet()

    var draftGroupIds by
    remember(
        groups,
        selectedGroupIds,
        isFilterActive
    ) {
        mutableStateOf(
            if (
                isFilterActive
            ) {
                selectedGroupIds
            } else {
                allGroupIds
            }
        )
    }

    var draftIncludeUngrouped by
    remember(
        includeUngrouped,
        isFilterActive,
        showUngroupedOption
    ) {
        mutableStateOf(
            if (
                !showUngroupedOption
            ) {
                false
            } else if (
                isFilterActive
            ) {
                includeUngrouped
            } else {
                true
            }
        )
    }

    val allSelected =
        draftGroupIds ==
                allGroupIds &&
                (
                        !showUngroupedOption ||
                                draftIncludeUngrouped
                        )

    val hasSelection =
        draftGroupIds.isNotEmpty() ||
                (
                        showUngroupedOption &&
                                draftIncludeUngrouped
                        )

    Dialog(
        onDismissRequest =
            onDismiss
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        max = 560.dp
                    ),
            shape =
                RoundedCornerShape(
                    24.dp
                ),
            color =
                MaterialTheme
                    .colorScheme
                    .surface,
            tonalElevation =
                6.dp,
            shadowElevation =
                10.dp
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(
                            horizontal = 24.dp,
                            vertical = 22.dp
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_execution_filter_groups
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_group_filter_description
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

                MapGroupFilterOption(
                    title =
                        stringResource(
                            R.string
                                .mission_group_filter_all
                        ),
                    supportingText =
                        null,
                    checked =
                        allSelected,
                    colorHex =
                        null,
                    onToggle = {
                        if (
                            allSelected
                        ) {
                            draftGroupIds =
                                emptySet()

                            draftIncludeUngrouped =
                                false
                        } else {
                            draftGroupIds =
                                allGroupIds

                            draftIncludeUngrouped =
                                showUngroupedOption
                        }
                    }
                )

                HorizontalDivider()

                groups
                    .forEach { group ->
                        MapGroupFilterOption(
                            title =
                                group.name,
                            supportingText =
                                stringResource(
                                    R.string
                                        .mission_group_filter_participants,
                                    group.participantCount
                                ),
                            checked =
                                group.id in
                                        draftGroupIds,
                            colorHex =
                                group.colorHex,
                            onToggle = {
                                draftGroupIds =
                                    if (
                                        group.id in
                                        draftGroupIds
                                    ) {
                                        draftGroupIds -
                                                group.id
                                    } else {
                                        draftGroupIds +
                                                group.id
                                    }
                            }
                        )
                    }

                if (
                    showUngroupedOption
                ) {
                    MapGroupFilterOption(
                        title =
                            stringResource(
                                R.string
                                    .mission_group_filter_ungrouped
                            ),
                        supportingText =
                            stringResource(
                                R.string
                                    .mission_group_filter_participants,
                                ungroupedParticipantCount
                            ),
                        checked =
                            draftIncludeUngrouped,
                        colorHex =
                            null,
                        onToggle = {
                            draftIncludeUngrouped =
                                !draftIncludeUngrouped
                        }
                    )
                }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    if (
                        isFilterActive
                    ) {
                        TextButton(
                            onClick =
                                onClear
                        ) {
                            Text(
                                text =
                                    stringResource(
                                        R.string
                                            .mission_group_filter_show_all
                                    )
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    Button(
                        onClick = {
                            onApply(
                                draftGroupIds,
                                draftIncludeUngrouped
                            )
                        },
                        enabled =
                            hasSelection
                    ) {
                        Text(
                            text =
                                stringResource(
                                    R.string
                                        .mission_group_filter_apply
                                )
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun MapGroupFilterOption(
    title: String,
    supportingText: String?,
    checked: Boolean,
    colorHex: String?,
    onToggle: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onToggle
                )
                .padding(
                    vertical = 6.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {
        Checkbox(
            checked =
                checked,
            onCheckedChange = {
                onToggle()
            }
        )

        Box(
            modifier =
                Modifier
                    .size(
                        14.dp
                    )
                    .background(
                        color =
                            colorHex
                                .toComposeColorOrNull()
                                ?: MaterialTheme
                                    .colorScheme
                                    .outline,
                        shape =
                            CircleShape
                    )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                fontWeight =
                    FontWeight.Medium
            )

            if (
                supportingText != null
            ) {
                Text(
                    text =
                        supportingText,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyGroupBottomSheet(
    groupName: String?,
    groupColorHex: String?,
    members: List<MissionGroupMemberUiModel>,
    onDismiss: () -> Unit,
    onMemberClick: (
        MissionGroupMemberUiModel
    ) -> Unit
) {

    ModalBottomSheet(
        onDismissRequest =
            onDismiss
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 28.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            if (
                groupName == null
            ) {

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_my_group_title
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        stringResource(
                            R.string
                                .mission_my_group_no_group
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                return@Column
            }

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(
                                16.dp
                            )
                            .background(
                                color =
                                    groupColorHex
                                        .toComposeColorOrNull()
                                        ?: MaterialTheme
                                            .colorScheme
                                            .primary,
                                shape =
                                    CircleShape
                            )
                )

                Column {

                    Text(
                        text =
                            groupName,
                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            stringResource(
                                R.string
                                    .mission_my_group_members_count,
                                members.size
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

            Text(
                text =
                    stringResource(
                        R.string
                            .mission_my_group_participants
                    ),
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold
            )

            members
                .forEach { member ->

                    MyGroupMemberRow(
                        member =
                            member,
                        groupColorHex =
                            groupColorHex,
                        onClick = {
                            onMemberClick(
                                member
                            )
                        }
                    )
                }
        }
    }
}

@Composable
private fun MyGroupMemberRow(
    member: MissionGroupMemberUiModel,
    groupColorHex: String?,
    onClick: () -> Unit
) {

    val statusText =
        when (
            member.status
        ) {

            MissionParticipantStatus
                .NEEDS_SUPPORT ->
                stringResource(
                    R.string
                        .mission_participant_requested_support
                )

            MissionParticipantStatus
                .FINISHED ->
                stringResource(
                    R.string
                        .mission_my_group_finished
                )

            MissionParticipantStatus
                .ACTIVE ->
                stringResource(
                    R.string
                        .mission_participant_active
                )
        }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled =
                        member.hasMapPosition,
                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(
                16.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(
                    alpha =
                        0.55f
                )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        14.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            Surface(
                modifier =
                    Modifier.size(
                        42.dp
                    ),
                shape =
                    CircleShape,
                color =
                    groupColorHex
                        .toComposeColorOrNull()
                        ?: MaterialTheme
                            .colorScheme
                            .primary
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            if (
                                member.status ==
                                MissionParticipantStatus
                                    .NEEDS_SUPPORT
                            ) {
                                Icons.Outlined.Sos
                            } else {
                                Icons.Outlined.Groups
                            },
                        contentDescription =
                            null,
                        tint =
                            Color.White
                    )
                }
            }

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        3.dp
                    )
            ) {

                Text(
                    text =
                        member.displayName
                            .takeIf {
                                it.isNotBlank()
                            }
                            ?: stringResource(
                                R.string
                                    .mission_participant_destination
                            ),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text =
                        buildString {

                            append(
                                participantRoleLabel(
                                    member.role
                                )
                            )

                            if (
                                member.isCurrentUser
                            ) {
                                append(
                                    " • "
                                )
                                append(
                                    stringResource(
                                        R.string
                                            .mission_my_group_you
                                    )
                                )
                            }

                            if (
                                member.isSupport
                            ) {
                                append(
                                    " • "
                                )
                                append(
                                    stringResource(
                                        R.string
                                            .mission_my_group_support
                                    )
                                )
                            }
                        },
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text =
                        statusText,
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    fontWeight =
                        FontWeight.Medium,
                    color =
                        if (
                            member.status ==
                            MissionParticipantStatus
                                .NEEDS_SUPPORT
                        ) {
                            MaterialTheme
                                .colorScheme
                                .error
                        } else {
                            MaterialTheme
                                .colorScheme
                                .primary
                        }
                )
            }
        }
    }
}

private fun String?.toComposeColorOrNull():
        Color? {

    val value =
        this
            ?.takeIf {
                it.matches(
                    Regex(
                        "^#[0-9A-Fa-f]{6}$"
                    )
                )
            }
            ?: return null

    return runCatching {
        Color(
            android.graphics.Color
                .parseColor(
                    value
                )
        )
    }
        .getOrNull()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantDetailsBottomSheet(
    participant: MissionParticipantMarker,
    onDismiss: () -> Unit,
    onTraceRouteClick: () -> Unit
) {

    ModalBottomSheet(
        onDismissRequest =
            onDismiss
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 28.dp
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    14.dp
                )
        ) {

            Text(
                text =
                    participant.displayName,
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            Text(
                text =
                    participantRoleLabel(
                        participant.role
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            ParticipantDetailRow(
                label =
                    stringResource(
                        R.string
                            .mission_participant_group
                    ),
                value =
                    participant.groupName
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: stringResource(
                            R.string
                                .mission_participant_no_group
                        )
            )

            if (
                participant.isSupport
            ) {

                ParticipantStatusBadge(
                    text =
                        stringResource(
                            R.string
                                .mission_participant_group_support
                        ),
                    isAlert =
                        false
                )
            }

            ParticipantStatusBadge(
                text =
                    if (
                        participant.status ==
                        MissionParticipantStatus
                            .NEEDS_SUPPORT
                    ) {
                        stringResource(
                            R.string
                                .mission_participant_requested_support
                        )
                    } else {
                        stringResource(
                            R.string
                                .mission_participant_active
                        )
                    },
                isAlert =
                    participant.status ==
                            MissionParticipantStatus
                                .NEEDS_SUPPORT
            )

            Spacer(
                modifier =
                    Modifier.height(
                        2.dp
                    )
            )

            if (!participant.isCurrentUser) {
                Button(
                    onClick = onTraceRouteClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(
                            R.string.mission_participant_trace_route
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantDetailRow(
    label: String,
    value: String
) {

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                2.dp
            )
    ) {

        Text(
            text =
                label,
            style =
                MaterialTheme
                    .typography
                    .labelMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Text(
            text =
                value,
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            fontWeight =
                FontWeight.Medium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )
    }
}

@Composable
private fun ParticipantStatusBadge(
    text: String,
    isAlert: Boolean
) {

    Surface(
        shape =
            RoundedCornerShape(
                12.dp
            ),
        color =
            if (
                isAlert
            ) {
                MaterialTheme
                    .colorScheme
                    .errorContainer
            } else {
                MaterialTheme
                    .colorScheme
                    .secondaryContainer
            },
        contentColor =
            if (
                isAlert
            ) {
                MaterialTheme
                    .colorScheme
                    .onErrorContainer
            } else {
                MaterialTheme
                    .colorScheme
                    .onSecondaryContainer
            }
    ) {

        Text(
            text =
                text,
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.SemiBold,
            modifier =
                Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                )
        )
    }
}

@Composable
private fun participantRoleLabel(
    role: UserRole?
): String {

    return stringResource(
        when (
            role
        ) {

            UserRole.MISSIONARY ->
                R.string
                    .mission_participant_role_missionary

            UserRole.LEADER ->
                R.string
                    .mission_participant_role_leader

            UserRole.PASTOR ->
                R.string
                    .mission_participant_role_pastor

            UserRole.ADMIN ->
                R.string
                    .mission_participant_role_admin

            null ->
                R.string
                    .mission_participant_role_unknown
        }
    )
}

private fun openParticipantInMapApp(
    context: Context,
    participant: MissionParticipantMarker
) {

    val label =
        participant.displayName
            .takeIf {
                it.isNotBlank()
            }
            ?: context.getString(
                R.string
                    .mission_participant_destination
            )

    val uri =
        Uri.parse(
            "geo:0,0?q=" +
                    "${participant.latitude}," +
                    "${participant.longitude}" +
                    "(${Uri.encode(label)})"
        )

    val mapIntent =
        Intent(
            Intent.ACTION_VIEW,
            uri
        )

    val chooser =
        Intent.createChooser(
            mapIntent,
            context.getString(
                R.string
                    .mission_participant_choose_map_app
            )
        )

    try {

        context.startActivity(
            chooser
        )

    } catch (
        exception:
        ActivityNotFoundException
    ) {

        Toast
            .makeText(
                context,
                context.getString(
                    R.string
                        .mission_participant_no_map_app
                ),
                Toast.LENGTH_SHORT
            )
            .show()
    }
}

private fun openEncounterInWhatsApp(
    context: Context,
    encounter: MissionEncounter
) {

    val phoneDigits =
        encounter
            .phoneDigits
            .orEmpty()
            .filter(
                Char::isDigit
            )

    if (
        phoneDigits.length !=
        11
    ) {
        return
    }

    val firstName =
        encounter
            .personName
            ?.trim()
            ?.substringBefore(
                " "
            )
            ?.takeIf {
                it.isNotBlank()
            }

    val message =
        if (
            firstName != null
        ) {
            context.getString(
                R.string
                    .new_encounter_whatsapp_message_named,
                firstName
            )
        } else {
            context.getString(
                R.string
                    .new_encounter_whatsapp_message_unnamed
            )
        }

    val opened =
        openEncounterWhatsAppConversation(
            context =
                context,
            phoneDigits =
                phoneDigits,
            message =
                message
        )

    if (
        !opened
    ) {

        Toast
            .makeText(
                context,
                context.getString(
                    R.string
                        .new_encounter_whatsapp_error
                ),
                Toast.LENGTH_SHORT
            )
            .show()
    }
}

private fun openEncounterWhatsAppConversation(
    context: Context,
    phoneDigits: String,
    message: String
): Boolean {

    val normalizedPhone =
        phoneDigits
            .filter(
                Char::isDigit
            )

    if (
        normalizedPhone.length !=
        11
    ) {
        return false
    }

    val fullPhone =
        "55$normalizedPhone"

    val encodedMessage =
        Uri.encode(
            message
        )

    val nativeUri =
        Uri.parse(
            "whatsapp://send" +
                    "?phone=$fullPhone" +
                    "&text=$encodedMessage"
        )

    val whatsappPackages =
        listOf(
            "com.whatsapp",
            "com.whatsapp.w4b"
        )

    whatsappPackages
        .forEach { packageName ->

            val opened =
                runCatching {

                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            nativeUri
                        )
                            .apply {
                                setPackage(
                                    packageName
                                )

                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                )
                            }

                    context.startActivity(
                        intent
                    )

                    true
                }
                    .getOrDefault(
                        false
                    )

            if (
                opened
            ) {
                return true
            }
        }

    return runCatching {

        val webUri =
            Uri.parse(
                "https://wa.me/$fullPhone" +
                        "?text=$encodedMessage"
            )

        val intent =
            Intent(
                Intent.ACTION_VIEW,
                webUri
            )
                .apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }

        context.startActivity(
            intent
        )

        true
    }
        .getOrDefault(
            false
        )
}

private fun openEncounterInMapApp(
    context: Context,
    encounter: MissionEncounter
) {

    val latitude =
        encounter.latitude
            ?: return

    val longitude =
        encounter.longitude
            ?: return

    val label =
        encounter
            .personName
            ?.takeIf {
                it.isNotBlank()
            }
            ?: context.getString(
                R.string
                    .mission_participant_destination
            )

    val uri =
        Uri.parse(
            "geo:0,0?q=" +
                    "$latitude," +
                    "$longitude" +
                    "(${Uri.encode(label)})"
        )

    val mapIntent =
        Intent(
            Intent.ACTION_VIEW,
            uri
        )

    val chooser =
        Intent.createChooser(
            mapIntent,
            context.getString(
                R.string
                    .mission_participant_choose_map_app
            )
        )

    try {

        context.startActivity(
            chooser
        )

    } catch (
        exception:
        ActivityNotFoundException
    ) {

        Toast
            .makeText(
                context,
                context.getString(
                    R.string
                        .mission_participant_no_map_app
                ),
                Toast.LENGTH_SHORT
            )
            .show()
    }
}


@Composable
private fun MissionExecutionHeader(
    missionName: String,
    elapsedSeconds: Long,
    canViewGeneralMetrics: Boolean,
    canManageMission: Boolean,
    isGroupFilterActive: Boolean,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onGeneralMetricsClick: () -> Unit,
    onFilterGroupsClick: () -> Unit,
    onFinishMissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color =
            MaterialTheme.colorScheme.surface.copy(
                alpha = 0.97f
            ),
        contentColor =
            MaterialTheme.colorScheme.onSurface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 4.dp,
                        end =
                            if (
                                canViewGeneralMetrics ||
                                canManageMission
                            ) {
                                2.dp
                            } else {
                                14.dp
                            },
                        top = 6.dp,
                        bottom = 6.dp
                    ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.ArrowBack,
                    contentDescription =
                        stringResource(
                            R.string.mission_execution_back
                        ),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )
            }

            Text(
                text = missionName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp)
                )

                Text(
                    text = formatElapsedTime(elapsedSeconds),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )
            }

            if (
                canViewGeneralMetrics ||
                canManageMission
            ) {
                Box {
                    IconButton(
                        onClick = {
                            onMenuExpandedChange(true)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription =
                                stringResource(
                                    R.string
                                        .mission_execution_more_options
                                ),
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            onMenuExpandedChange(false)
                        }
                    ) {
                        if (
                            canViewGeneralMetrics
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            R.string
                                                .mission_execution_general_metrics
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.BarChart,
                                        contentDescription = null
                                    )
                                },
                                onClick = onGeneralMetricsClick
                            )
                        }

                        if (canManageMission) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(
                                            R.string
                                                .mission_execution_filter_groups
                                        )
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.FilterList,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon =
                                    if (
                                        isGroupFilterActive
                                    ) {
                                        {
                                            Text(
                                                text =
                                                    stringResource(
                                                        R.string
                                                            .mission_group_filter_active
                                                    ),
                                                color =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .primary,
                                                style =
                                                    MaterialTheme
                                                        .typography
                                                        .labelMedium
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                onClick =
                                    onFilterGroupsClick
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text =
                                            stringResource(
                                                R.string
                                                    .mission_execution_finish_mission
                                            ),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = onFinishMissionClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapFloatingButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(56.dp),
        shape =
            RoundedCornerShape(
                16.dp
            ),
        color =
            MaterialTheme.colorScheme.surface.copy(
                alpha = 0.97f
            ),
        shadowElevation = 4.dp
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MissionExecutionActionCluster(
    recenterContentDescription: String,
    onRecenterClick: () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMyGroupClick: () -> Unit,
    onMetricsClick: () -> Unit,
    onNewEncounterClick: () -> Unit,
    isSupportRequested: Boolean,
    isUpdatingSupportStatus: Boolean,
    isEndingParticipation: Boolean,
    onRequestSupportClick: () -> Unit,
    onEndParticipationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.width(
                230.dp
            ),
        horizontalAlignment =
            Alignment.End,
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {

        /*
         * O botão de centralizar fica acima das ações.
         * Quando o menu abre, o bloco de ações cresce ENTRE
         * ele e o FAB principal. Assim:
         *
         * - o + permanece imóvel;
         * - os botões aparecem no mesmo eixo;
         * - o botão de centralizar sobe suavemente;
         * - não existe offset fixo dependente da quantidade
         *   ou do tamanho dos botões.
         */
        MapFloatingButton(
            icon =
                Icons.Outlined.MyLocation,
            contentDescription =
                recenterContentDescription,
            onClick =
                onRecenterClick
        )

        AnimatedVisibility(
            visible =
                expanded,
            enter =
                expandVertically(
                    expandFrom =
                        Alignment.Bottom
                ) +
                        fadeIn() +
                        scaleIn(),
            exit =
                shrinkVertically(
                    shrinkTowards =
                        Alignment.Bottom
                ) +
                        fadeOut() +
                        scaleOut()
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalAlignment =
                    Alignment.End,
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                SpeedDialAction(
                    label =
                        stringResource(
                            R.string
                                .mission_execution_my_group
                        ),
                    icon =
                        Icons.Outlined.Groups,
                    onClick =
                        onMyGroupClick
                )

                SpeedDialAction(
                    label =
                        stringResource(
                            R.string
                                .mission_execution_metrics
                        ),
                    icon =
                        Icons.Outlined.BarChart,
                    onClick =
                        onMetricsClick
                )

                SpeedDialAction(
                    label =
                        stringResource(
                            R.string
                                .mission_execution_new_encounter
                        ),
                    icon =
                        Icons.Outlined.PersonAdd,
                    onClick =
                        onNewEncounterClick
                )

                SpeedDialAction(
                    label =
                        stringResource(
                            if (
                                isSupportRequested
                            ) {
                                R.string
                                    .mission_execution_support_requested
                            } else {
                                R.string
                                    .mission_execution_request_support
                            }
                        ),
                    icon =
                        Icons.Outlined.Sos,
                    enabled =
                        !isUpdatingSupportStatus,
                    onClick =
                        onRequestSupportClick
                )

                SpeedDialAction(
                    label =
                        stringResource(
                            if (
                                isEndingParticipation
                            ) {
                                R.string
                                    .mission_end_participation_ending
                            } else {
                                R.string
                                    .mission_execution_end_participation
                            }
                        ),
                    icon =
                        Icons.Outlined.Logout,
                    isDestructive =
                        true,
                    enabled =
                        !isEndingParticipation,
                    onClick =
                        onEndParticipationClick
                )
            }
        }

        FloatingActionButton(
            onClick = {
                onExpandedChange(
                    !expanded
                )
            },
            shape =
                RoundedCornerShape(
                    18.dp
                ),
            containerColor =
                MaterialTheme
                    .colorScheme
                    .primary,
            contentColor =
                MaterialTheme
                    .colorScheme
                    .onPrimary,
            elevation =
                androidx.compose.material3
                    .FloatingActionButtonDefaults
                    .elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 6.dp,
                        focusedElevation = 4.dp,
                        hoveredElevation = 5.dp
                    )
        ) {
            Icon(
                imageVector =
                    if (
                        expanded
                    ) {
                        Icons.Default.Close
                    } else {
                        Icons.Default.Add
                    },
                contentDescription =
                    if (
                        expanded
                    ) {
                        stringResource(
                            R.string
                                .mission_execution_close_actions
                        )
                    } else {
                        stringResource(
                            R.string
                                .mission_execution_open_actions
                        )
                    }
            )
        }
    }
}

@Composable
private fun SpeedDialAction(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    enabled: Boolean = true
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color =
                MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.98f
                ),
            shadowElevation = 6.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color =
                    if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                modifier =
                    Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 9.dp
                    )
            )
        }

        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.size(8.dp)
        )

        Surface(
            modifier = Modifier.size(56.dp),
            shape =
                RoundedCornerShape(
                    18.dp
                ),
            color =
                if (isDestructive) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.98f
                    )
                },
            shadowElevation = 4.dp
        ) {
            IconButton(
                onClick = onClick,
                enabled = enabled
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint =
                        if (isDestructive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                )
            }
        }
    }
}

@Composable
private fun MissionExecutionError(
    message: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            IdePrimaryButton(
                text =
                    stringResource(
                        R.string.mission_execution_back
                    ),
                onClick = onBackClick
            )
        }
    }
}

private fun formatElapsedTime(
    elapsedSeconds: Long
): String {
    val safeSeconds =
        elapsedSeconds.coerceAtLeast(0L)

    val hours =
        safeSeconds / 3600L

    val minutes =
        (safeSeconds % 3600L) / 60L

    val seconds =
        safeSeconds % 60L

    return "%02d:%02d:%02d"
        .format(
            hours,
            minutes,
            seconds
        )
}
