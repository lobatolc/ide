package br.com.ide.presentation.feature.usermanagementdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.domain.model.UserRole
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdeDropdownField
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.mapper.toStringRes

@Composable
fun UserManagementDetailsScreen(
    userId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: UserManagementDetailsViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaved()
        }
    }

    when {

        uiState.isLoading -> {
            Box(
                modifier =
                    Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        else -> {
            UserManagementDetailsContent(
                uiState = uiState,
                onEvent =
                    viewModel::onEvent,
                onBackClick =
                    onBackClick
            )
        }
    }
}

@Composable
private fun UserManagementDetailsContent(
    uiState: UserManagementDetailsUiState,
    onEvent:
        (UserManagementDetailsEvent) -> Unit,
    onBackClick: () -> Unit
) {
    val target =
        uiState.target
            ?: return

    val manager =
        uiState.manager
            ?: return

    val selectedRole =
        uiState.selectedRole

    val selectedDistrict =
        uiState.districts
            .firstOrNull {
                it.id ==
                        uiState.selectedDistrictId
            }

    val availableChurches =
        uiState.churches.filter { church ->

            uiState.selectedDistrictId == null ||
                    church.districtId ==
                    uiState.selectedDistrictId
        }

    val selectedChurch =
        availableChurches
            .firstOrNull {
                it.id ==
                        uiState.selectedChurchId
            }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        verticalArrangement =
            Arrangement.Top
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
                        R.string
                            .user_management_details_back
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            IdeScreenTitle(
                text = stringResource(
                    R.string
                        .user_management_details_title
                )
            )
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        IdeScreenSubtitle(
            text = stringResource(
                R.string
                    .user_management_details_subtitle
            )
        )

        Spacer(
            modifier =
                Modifier.height(28.dp)
        )

        Text(
            text =
                "${target.firstName} ${target.lastName}"
                    .trim(),
            style =
                MaterialTheme
                    .typography
                    .headlineSmall,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        IdeDropdownField(
            selectedValue =
                selectedRole,
            options =
                uiState.roles,
            label =
                stringResource(
                    R.string
                        .user_management_details_role
                ),
            optionText = {
                stringResource(
                    it.toStringRes()
                )
            },
            onOptionSelected = {
                onEvent(
                    UserManagementDetailsEvent
                        .RoleSelected(it)
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        if (
            manager.role ==
            UserRole.ADMIN &&
            selectedRole !=
            UserRole.ADMIN
        ) {

            IdeDropdownField(
                selectedValue =
                    selectedDistrict,
                options =
                    uiState.districts,
                label =
                    stringResource(
                        R.string
                            .user_management_details_district
                    ),
                optionText = {
                    it.name
                },
                onOptionSelected = {
                    onEvent(
                        UserManagementDetailsEvent
                            .DistrictSelected(
                                it.id
                            )
                    )
                },
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }

        if (
            manager.role ==
            UserRole.PASTOR
        ) {

            Text(
                text = stringResource(
                    R.string
                        .user_management_details_district
                ),
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text =
                    selectedDistrict
                        ?.name
                        .orEmpty(),
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }

        if (
            selectedRole ==
            UserRole.MISSIONARY ||
            selectedRole ==
            UserRole.LEADER
        ) {

            IdeDropdownField(
                selectedValue =
                    selectedChurch,
                options =
                    availableChurches,
                label =
                    stringResource(
                        R.string
                            .user_management_details_church
                    ),
                optionText = {
                    it.name
                },
                onOptionSelected = {
                    onEvent(
                        UserManagementDetailsEvent
                            .ChurchSelected(
                                it.id
                            )
                    )
                },
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }

        if (
            selectedRole ==
            UserRole.PASTOR
        ) {

            Text(
                text = stringResource(
                    R.string
                        .user_management_details_pastor_no_church
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
                    Modifier.height(16.dp)
            )
        }

        uiState.errorMessage?.let { errorMessage ->

            Text(
                text =
                    stringResource(
                        errorMessage
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

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }

        IdePrimaryButton(
            text =
                stringResource(
                    R.string
                        .user_management_details_save
                ),
            onClick = {
                onEvent(
                    UserManagementDetailsEvent.Save
                )
            },
            isLoading =
                uiState.isSaving,
            enabled =
                !uiState.isSaving,
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}