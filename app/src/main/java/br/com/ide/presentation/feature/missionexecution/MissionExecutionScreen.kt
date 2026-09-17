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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
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
        onGroupsClick = onGroupsClick,
        onFinishMissionClick = onFinishMissionClick,
        onMetricsClick = onMetricsClick,
        onGeneralMetricsClick = onGeneralMetricsClick,
        onFilterGroupsClick = onFilterGroupsClick,
        onRequestSupportClick = {
            if (
                uiState.isSupportRequested
            ) {
                viewModel.confirmSupportReceived()
            } else {
                viewModel.requestSupport()
                onRequestSupportClick()
            }
        },
        onEndParticipationClick = onEndParticipationClick
    )
}

@Composable
private fun MissionExecutionContent(
    uiState: MissionExecutionUiState,
    focusParticipantUserId: String?,
    onFocusParticipantHandled: () -> Unit,
    onBackClick: () -> Unit,
    onRegisterEncounterClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onFinishMissionClick: () -> Unit,
    onMetricsClick: () -> Unit,
    onGeneralMetricsClick: () -> Unit,
    onFilterGroupsClick: () -> Unit,
    onRequestSupportClick: () -> Unit,
    onEndParticipationClick: () -> Unit
) {
    val context =
        LocalContext.current

    val isDarkTheme =
        MaterialTheme.colorScheme.background.luminance() < 0.5f

    var recenterKey by remember { mutableIntStateOf(0) }
    var speedDialExpanded by remember { mutableStateOf(false) }
    var headerMenuExpanded by remember { mutableStateOf(false) }

    var selectedParticipantUserId by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    val selectedParticipant =
        selectedParticipantUserId
            ?.let { userId ->
                uiState.participantMarkers
                    .firstOrNull {
                        it.userId ==
                                userId
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
            participantMarkers = uiState.participantMarkers,
            participantTracks = uiState.participantTracks,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.fillMaxSize(),
            recenterKey = recenterKey,
            focusParticipantUserId =
                focusParticipantUserId,
            onFocusParticipantHandled =
                onFocusParticipantHandled,
            onParticipantClick = { participant ->

                selectedParticipantUserId =
                    participant.userId
            }
        )

        MissionExecutionHeader(
            missionName = uiState.missionName,
            elapsedSeconds = uiState.elapsedSeconds,
            canManageMission = uiState.canFinishMission,
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
                onFilterGroupsClick()
            },
            onFinishMissionClick = {
                headerMenuExpanded = false
                onFinishMissionClick()
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
                onGroupsClick()
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
            onRequestSupportClick = {
                speedDialExpanded = false
                onRequestSupportClick()
            },
            onEndParticipationClick = {
                speedDialExpanded = false
                onEndParticipationClick()
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


@Composable
private fun MissionExecutionHeader(
    missionName: String,
    elapsedSeconds: Long,
    canManageMission: Boolean,
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
                        end = 2.dp,
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
                            onClick = onFilterGroupsClick
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
                                    .mission_execution_support_received
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
                            R.string
                                .mission_execution_end_participation
                        ),
                    icon =
                        Icons.Outlined.Logout,
                    isDestructive =
                        true,
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
