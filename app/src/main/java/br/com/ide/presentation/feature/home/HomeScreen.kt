package br.com.ide.presentation.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.com.ide.R
import br.com.ide.domain.model.MissionStatus
import br.com.ide.presentation.components.BottomNavigationItem
import br.com.ide.presentation.components.IdeBottomNavigation
import br.com.ide.presentation.components.LanguageSelector
import br.com.ide.presentation.components.MissionCard
import br.com.ide.presentation.components.MissionSearchBar
import br.com.ide.presentation.components.MissionStatusChip
import br.com.ide.presentation.mapper.toStringRes
import br.com.ide.presentation.model.AppLanguage

@Composable
fun HomeScreen(
    appLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    onMissionClick: (String) -> Unit,
    onMetricsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFilterClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = uiState,
        appLanguage = appLanguage,
        onLanguageChanged =
            onLanguageChanged,
        onEvent = viewModel::onEvent,
        onMissionClick = onMissionClick,
        onMetricsClick = onMetricsClick,
        onProfileClick = onProfileClick,
        onFilterClick = onFilterClick
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    appLanguage: AppLanguage,
    onLanguageChanged:
        (AppLanguage) -> Unit,
    onEvent: (HomeEvent) -> Unit,
    onMissionClick: (String) -> Unit,
    onMetricsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            IdeBottomNavigation(
                selectedItem = BottomNavigationItem.HOME,
                onItemSelected = { item ->
                    when (item) {
                        BottomNavigationItem.HOME -> Unit

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
                userName = uiState.userName,
                appLanguage = appLanguage,
                onLanguageChanged =
                    onLanguageChanged
            )

            MissionSearchBar(
                value = uiState.searchQuery,
                onValueChange = {
                    onEvent(
                        HomeEvent.SearchChanged(it)
                    )
                },
                onFilterClick = onFilterClick,
                modifier = Modifier.padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                )
            )

            MissionStatusFilters(
                selectedStatus = uiState.selectedStatus,
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
                        errorMessage = stringResource(
                            uiState.errorMessage
                        ),
                        onRetry = {
                            onEvent(HomeEvent.Retry)
                        }
                    )
                }

                uiState.missions.isEmpty() -> {
                    HomeEmptyState(
                        hasSearch =
                            uiState.searchQuery.isNotBlank() ||
                                    uiState.selectedStatus != null
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 16.dp,
                            bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            14.dp
                        )
                    ) {
                        items(
                            items = uiState.missions,
                            key = { mission ->
                                mission.id
                            }
                        ) { mission ->

                            MissionCard(
                                mission = mission,
                                statusText = stringResource(
                                    mission.status.toStringRes()
                                ),
                                onClick = {
                                    onMissionClick(
                                        mission.id
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
    userName: String,
    appLanguage: AppLanguage,
    onLanguageChanged:
        (AppLanguage) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 12.dp,
                top = 20.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (userName.isBlank()) {
                    stringResource(
                        R.string.home_greeting
                    )
                } else {
                    stringResource(
                        R.string.home_greeting_name,
                        userName
                    )
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.padding(
                    vertical = 2.dp
                )
            )

            Text(
                text = stringResource(
                    R.string.home_subtitle
                ),
                style = MaterialTheme.typography.bodyLarge,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LanguageSelector(
            selectedLanguage =
                appLanguage,
            onLanguageSelected =
                onLanguageChanged
        )
    }
}

@Composable
private fun MissionStatusFilters(
    selectedStatus: MissionStatus?,
    onStatusSelected: (MissionStatus?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = 20.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(
            8.dp
        )
    ) {
        item {
            MissionStatusChip(
                text = stringResource(
                    R.string.mission_filter_all
                ),
                selected = selectedStatus == null,
                onClick = {
                    onStatusSelected(null)
                }
            )
        }

        item {
            MissionStatusChip(
                text = stringResource(
                    R.string.mission_status_in_progress
                ),
                selected =
                    selectedStatus ==
                            MissionStatus.IN_PROGRESS,
                onClick = {
                    onStatusSelected(
                        MissionStatus.IN_PROGRESS
                    )
                }
            )
        }

        item {
            MissionStatusChip(
                text = stringResource(
                    R.string.mission_status_scheduled
                ),
                selected =
                    selectedStatus ==
                            MissionStatus.SCHEDULED,
                onClick = {
                    onStatusSelected(
                        MissionStatus.SCHEDULED
                    )
                }
            )
        }

        item {
            MissionStatusChip(
                text = stringResource(
                    R.string.mission_status_completed
                ),
                selected =
                    selectedStatus ==
                            MissionStatus.COMPLETED,
                onClick = {
                    onStatusSelected(
                        MissionStatus.COMPLETED
                    )
                }
            )
        }
    }
}

@Composable
private fun HomeLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
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
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(
                onClick = onRetry
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null
                )

                Text(
                    text = stringResource(
                        R.string.home_retry
                    ),
                    modifier = Modifier.padding(
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
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasSearch) {
                    stringResource(
                        R.string.home_no_missions_found
                    )
                } else {
                    stringResource(
                        R.string.home_no_missions
                    )
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (hasSearch) {
                    stringResource(
                        R.string.home_no_missions_found_subtitle
                    )
                } else {
                    stringResource(
                        R.string.home_no_missions_subtitle
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    top = 6.dp
                )
            )
        }
    }
}