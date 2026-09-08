package br.com.ide.presentation.feature.editprofile

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
import androidx.compose.material3.CircularProgressIndicator
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
fun EditProfileScreen(
    onBackClick: () -> Unit,
    onSaved: (
        String,
        String,
        SabbathSchoolClass
    ) -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(
        uiState.isSaved
    ) {
        if (uiState.isSaved) {

            val sabbathSchoolClass =
                uiState.sabbathSchoolClass
                    ?: return@LaunchedEffect

            onSaved(
                uiState.firstName.trim(),
                uiState.lastName.trim(),
                sabbathSchoolClass
            )
        }
    }

    if (uiState.isLoading) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        return
    }

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
                onClick = onBackClick,
                contentDescription = stringResource(
                    R.string.edit_profile_back
                )
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            IdeScreenTitle(
                text = stringResource(
                    R.string.edit_profile_title
                )
            )
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        IdeScreenSubtitle(
            text = stringResource(
                R.string.edit_profile_subtitle
            )
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        IdeTextField(
            value = uiState.firstName,
            onValueChange = {
                viewModel.onEvent(
                    EditProfileEvent
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
                viewModel.onEvent(
                    EditProfileEvent
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
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        IdeDropdownField(
            selectedValue =
                uiState.sabbathSchoolClass,
            options =
                SabbathSchoolClass.entries,
            label = stringResource(
                R.string
                    .register_sabbath_school_class
            ),
            optionText = {
                stringResource(
                    it.toStringRes()
                )
            },
            onOptionSelected = {
                viewModel.onEvent(
                    EditProfileEvent
                        .SabbathSchoolClassChanged(it)
                )
            },
            errorRes =
                uiState.sabbathSchoolClassError,
            modifier = Modifier.fillMaxWidth()
        )

        uiState.errorMessage?.let { errorMessage ->

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = stringResource(
                    errorMessage
                ),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        IdePrimaryButton(
            text = stringResource(
                R.string.profile_save
            ),
            onClick = {
                viewModel.onEvent(
                    EditProfileEvent.SaveChanges
                )
            },
            isLoading = uiState.isSaving,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}