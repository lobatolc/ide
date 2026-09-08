package br.com.ide.presentation.feature.completeregistration

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import br.com.ide.R
import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdeDropdownField
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.components.IdeTextField
import br.com.ide.presentation.feature.register.toStringRes

@Composable
fun CompleteRegistrationScreen(
    onRegistrationCompleted: () -> Unit,
    onCancelRegistration: () -> Unit,
    viewModel: CompleteRegistrationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(
        uiState.isCompleted
    ) {
        if (uiState.isCompleted) {
            onRegistrationCompleted()
        }
    }

    CompleteRegistrationContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onCancelRegistration = onCancelRegistration
    )
}

@Composable
private fun CompleteRegistrationContent(
    uiState: CompleteRegistrationUiState,
    onEvent: (CompleteRegistrationEvent) -> Unit,
    onCancelRegistration: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp
            ),
        verticalArrangement = Arrangement.Top
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IdeBackButton(
                onClick = onCancelRegistration,
                contentDescription = stringResource(
                    R.string.complete_registration_back
                )
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            IdeScreenTitle(
                text = stringResource(
                    R.string.complete_registration_title
                )
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        IdeScreenSubtitle(
            text = stringResource(
                R.string.complete_registration_subtitle
            )
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        IdeTextField(
            value = uiState.firstName,
            onValueChange = {
                onEvent(
                    CompleteRegistrationEvent
                        .FirstNameChanged(it)
                )
            },
            label = stringResource(
                R.string.register_first_name
            ),
            errorRes = uiState.firstNameError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        IdeTextField(
            value = uiState.lastName,
            onValueChange = {
                onEvent(
                    CompleteRegistrationEvent
                        .LastNameChanged(it)
                )
            },
            label = stringResource(
                R.string.register_last_name
            ),
            errorRes = uiState.lastNameError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        IdeTextField(
            value = uiState.email,
            onValueChange = {},
            label = stringResource(
                R.string.register_email
            ),
            enabled = true,
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        IdeDropdownField(
            selectedValue = uiState.sabbathSchoolClass,
            options = SabbathSchoolClass.entries,
            label = stringResource(
                R.string.register_sabbath_school_class
            ),
            optionText = {
                stringResource(
                    it.toStringRes()
                )
            },
            onOptionSelected = {
                onEvent(
                    CompleteRegistrationEvent
                        .SabbathSchoolClassChanged(it)
                )
            },
            errorRes =
                uiState.sabbathSchoolClassError,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.errorMessage != null) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = stringResource(
                    uiState.errorMessage
                ),
                color =
                    MaterialTheme.colorScheme.error,
                style =
                    MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        IdePrimaryButton(
            text = stringResource(
                R.string.complete_registration_button
            ),
            onClick = {
                onEvent(
                    CompleteRegistrationEvent
                        .CompleteRegistration
                )
            },
            isLoading = uiState.isLoading,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}