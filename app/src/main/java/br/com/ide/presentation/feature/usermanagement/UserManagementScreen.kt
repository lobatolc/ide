package br.com.ide.presentation.feature.usermanagement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.domain.model.Church
import br.com.ide.domain.model.District
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.model.UserRole
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdeDropdownField
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.components.IdeSearchBar
import br.com.ide.presentation.mapper.toStringRes

@Composable
fun UserManagementScreen(
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: UserManagementViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    Scaffold(
        containerColor =
            MaterialTheme.colorScheme.background
    ) { innerPadding ->

        when {

            uiState.isLoading -> {

                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment =
                        Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {

                UserManagementContent(
                    uiState = uiState,
                    onEvent =
                        viewModel::onEvent,
                    onBackClick =
                        onBackClick,
                    onUserClick =
                        onUserClick,
                    modifier = Modifier
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun UserManagementContent(
    uiState: UserManagementUiState,
    onEvent: (UserManagementEvent) -> Unit,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                end = 12.dp,
                top = 20.dp
            )
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IdeBackButton(
                onClick = onBackClick,
                contentDescription =
                    stringResource(
                        R.string
                            .user_management_back
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            IdeScreenTitle(
                text = stringResource(
                    R.string
                        .user_management_title
                )
            )
        }

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        IdeScreenSubtitle(
            text = stringResource(
                R.string
                    .user_management_subtitle
            )
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        IdeSearchBar(
            query = uiState.searchQuery,
            onQueryChange = {
                onEvent(
                    UserManagementEvent.SearchChanged(it)
                )
            },
            placeholder = stringResource(
                R.string.user_management_search
            )
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        UserManagementFilters(
            uiState = uiState,
            onEvent = onEvent
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(
            text = stringResource(
                R.string
                    .user_management_people_count,
                uiState.filteredUsers.size
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
                Modifier.height(8.dp)
        )

        uiState.errorMessage?.let {
                errorMessage ->

            Text(
                text =
                    stringResource(
                        errorMessage
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )
        }

        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            items(
                items =
                    uiState.filteredUsers,
                key = {
                    it.id
                }
            ) { user ->

                val church =
                    uiState.churches
                        .firstOrNull {
                            it.id ==
                                    user.churchId
                        }

                val district =
                    uiState.districts
                        .firstOrNull {
                            it.id ==
                                    user.districtId
                        }

                UserManagementCard(
                    user = user,
                    church = church,
                    district = district,
                    onClick = {
                        onUserClick(
                            user.id
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun UserManagementFilters(
    uiState: UserManagementUiState,
    onEvent: (UserManagementEvent) -> Unit
) {
    val managerRole =
        uiState.manager?.role

    Column(
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        if (
            managerRole ==
            UserRole.ADMIN
        ) {

            IdeDropdownField(
                selectedValue =
                    uiState.districts
                        .firstOrNull {
                            it.id ==
                                    uiState
                                        .selectedDistrictId
                        },
                options =
                    uiState.districts,
                label =
                    stringResource(
                        R.string
                            .user_management_district
                    ),
                optionText = {
                    it.name
                },
                onOptionSelected = {
                    onEvent(
                        UserManagementEvent
                            .DistrictSelected(
                                it.id
                            )
                    )
                },
                modifier =
                    Modifier.fillMaxWidth()
            )
        }

        val availableChurches =
            when {

                uiState.selectedDistrictId == null ->
                    uiState.churches

                else ->
                    uiState.churches.filter {
                        it.districtId ==
                                uiState
                                    .selectedDistrictId
                    }
            }

        IdeDropdownField(
            selectedValue =
                availableChurches
                    .firstOrNull {
                        it.id ==
                                uiState
                                    .selectedChurchId
                    },
            options =
                availableChurches,
            label =
                stringResource(
                    R.string
                        .user_management_church
                ),
            optionText = {
                it.name
            },
            onOptionSelected = {
                onEvent(
                    UserManagementEvent
                        .ChurchSelected(
                            it.id
                        )
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UserManagementCard(
    user: UserProfile,
    church: Church?,
    district: District?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape =
            MaterialTheme.shapes.large,
        color =
            MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {

        Row(
            modifier =
                Modifier.padding(16.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                shape =
                    MaterialTheme.shapes.medium,
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Person,
                    contentDescription = null,
                    modifier =
                        Modifier.padding(12.dp),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "${user.firstName} ${user.lastName}"
                            .trim(),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )

                Text(
                    text =
                        stringResource(
                            user.role.toStringRes()
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                church?.let {

                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )

                    Text(
                        text = it.name,
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

                district?.let {

                    Text(
                        text = it.name,
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
}