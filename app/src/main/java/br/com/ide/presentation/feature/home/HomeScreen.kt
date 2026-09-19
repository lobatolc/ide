package br.com.ide.presentation.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.model.UserRole
import br.com.ide.presentation.components.BottomNavigationItem
import br.com.ide.presentation.components.IdeBottomNavigation
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.components.IdeSearchBar
import br.com.ide.presentation.components.MissionCard
import br.com.ide.presentation.components.MissionStatusChip
import br.com.ide.presentation.mapper.toStringRes

@Composable
fun HomeScreen(
    onMissionClick: (String, MissionStatus) -> Unit,
    onCreateMissionClick: () -> Unit,
    onMetricsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFilterClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner =
        LocalLifecycleOwner.current

    LaunchedEffect(
        viewModel
    ) {
        viewModel
            .effects
            .collect { effect ->
                when (
                    effect
                ) {
                    is HomeEffect.OpenMission -> {
                        onMissionClick(
                            effect.missionId,
                            effect.missionStatus
                        )
                    }
                }
            }
    }

    DisposableEffect(
        lifecycleOwner
    ) {

        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {

                    viewModel.onEvent(
                        HomeEvent.Refresh
                    )
                }
            }

        lifecycleOwner
            .lifecycle
            .addObserver(
                observer
            )

        onDispose {

            lifecycleOwner
                .lifecycle
                .removeObserver(
                    observer
                )
        }
    }

    HomeContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onMetricsClick = onMetricsClick,
        onProfileClick = onProfileClick,
        onFilterClick = onFilterClick,
        onCreateMissionClick = onCreateMissionClick
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onCreateMissionClick: () -> Unit,
    onMetricsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Scaffold(
        containerColor =
            MaterialTheme.colorScheme.background,

        floatingActionButton = {
            if (
                uiState.userRole.hasAtLeast(
                    UserRole.LEADER
                )
            ) {
                FloatingActionButton(
                    onClick = onCreateMissionClick,
                    containerColor =
                        MaterialTheme.colorScheme.primary,
                    contentColor =
                        MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(
                            R.string.home_create_mission
                        )
                    )
                }
            }
        },

        bottomBar = {
            IdeBottomNavigation(
                selectedItem =
                    BottomNavigationItem.HOME,

                onItemSelected = { item ->
                    when (item) {

                        BottomNavigationItem.HOME ->
                            Unit

                        BottomNavigationItem.METRICS ->
                            onMetricsClick()

                        BottomNavigationItem.PROFILE ->
                            onProfileClick()
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            HomeHeader(
                userName = uiState.userName
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            IdeSearchBar(
                query = uiState.searchQuery,
                onQueryChange = {
                    onEvent(
                        HomeEvent.SearchChanged(it)
                    )
                },
                placeholder = stringResource(
                    R.string.mission_search_placeholder
                ),
                modifier = Modifier.padding(
                    start = 20.dp,
                    end = 12.dp
                )
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            MissionStatusFilters(
                selectedStatus =
                    uiState.selectedStatus,
                onStatusSelected = {
                    onEvent(
                        HomeEvent.StatusSelected(it)
                    )
                }
            )

            when {

                uiState.isLoading -> {
                    HomeLoading()
                }

                uiState.errorMessage != null -> {
                    HomeError(
                        errorMessage =
                            stringResource(
                                uiState.errorMessage
                            ),
                        onRetry = {
                            onEvent(
                                HomeEvent.Retry
                            )
                        }
                    )
                }

                uiState.missions.isEmpty() -> {
                    HomeEmptyState(
                        hasSearch =
                            uiState.searchQuery
                                .isNotBlank() ||
                                    uiState.selectedStatus != null
                    )
                }

                else -> {
                    LazyColumn(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentPadding =
                            PaddingValues(
                                start = 20.dp,
                                end = 12.dp,
                                top = 16.dp,
                                bottom = 24.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                14.dp
                            )
                    ) {

                        items(
                            items =
                                uiState.missions,

                            key = { mission ->
                                mission.id
                            }
                        ) { mission ->

                            MissionCard(
                                mission = mission,

                                statusText =
                                    stringResource(
                                        mission.status
                                            .toStringRes()
                                    ),

                                onClick = {
                                    onEvent(
                                        HomeEvent.MissionClicked(
                                            missionId =
                                                mission.id,
                                            missionStatus =
                                                mission.status
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 12.dp,
                top = 20.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            IdeScreenTitle(
                text =
                    if (userName.isBlank()) {

                        stringResource(
                            R.string.home_greeting
                        )

                    } else {

                        stringResource(
                            R.string.home_greeting_name,
                            userName
                        )
                    }
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            IdeScreenSubtitle(
                text = stringResource(
                    R.string.home_subtitle
                )
            )
        }
    }
}

@Composable
private fun MissionStatusFilters(
    selectedStatus: MissionStatus?,
    onStatusSelected: (MissionStatus?) -> Unit
) {

    val statuses =
        listOf(
            MissionStatus.PLANNING,
            MissionStatus.SCHEDULED,
            MissionStatus.IN_PROGRESS,
            MissionStatus.COMPLETED,
            MissionStatus.CANCELLED
        )

    LazyRow(
        modifier =
            Modifier.fillMaxWidth(),

        contentPadding =
            PaddingValues(
                start = 20.dp,
                end = 12.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        item {
            MissionStatusChip(
                text =
                    stringResource(
                        R.string.mission_filter_all
                    ),
                selected =
                    selectedStatus ==
                            null,
                onClick = {
                    onStatusSelected(
                        null
                    )
                }
            )
        }

        items(
            items =
                statuses,
            key = {
                it.name
            }
        ) { status ->

            MissionStatusChip(
                text =
                    stringResource(
                        status.toStringRes()
                    ),
                selected =
                    selectedStatus ==
                            status,
                onClick = {
                    onStatusSelected(
                        status
                    )
                }
            )
        }
    }
}

@Composable
private fun HomeLoading() {
    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        CircularProgressIndicator(
            color =
                MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HomeError(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = errorMessage,
                style =
                    MaterialTheme.typography.bodyLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            TextButton(
                onClick = onRetry
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Refresh,
                    contentDescription = null
                )

                Text(
                    text = stringResource(
                        R.string.home_retry
                    ),
                    modifier =
                        Modifier.padding(
                            start = 6.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeEmptyState(
    hasSearch: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text =
                    if (hasSearch) {

                        stringResource(
                            R.string
                                .home_no_missions_found
                        )

                    } else {

                        stringResource(
                            R.string.home_no_missions
                        )
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )

            Text(
                text =
                    if (hasSearch) {

                        stringResource(
                            R.string
                                .home_no_missions_found_subtitle
                        )

                    } else {

                        stringResource(
                            R.string
                                .home_no_missions_subtitle
                        )
                    },

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
                        top = 6.dp
                    )
            )
        }
    }
}
