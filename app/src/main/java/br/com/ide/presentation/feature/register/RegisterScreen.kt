package br.com.ide.presentation.feature.register

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdeDropdownField
import br.com.ide.presentation.components.IdePasswordField
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.components.IdeTextField

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IdeBackButton(
                    onClick = onBackClick,
                    contentDescription = stringResource(
                        R.string.register_back
                    )
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                IdeScreenTitle(
                    text = stringResource(
                        R.string.register_title
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            IdeScreenSubtitle(
                text = stringResource(
                    R.string.register_subtitle
                ),
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            IdeTextField(
                value = uiState.firstName,
                onValueChange = {
                    onEvent(
                        RegisterEvent.FirstNameChanged(it)
                    )
                },
                label = stringResource(
                    R.string.register_first_name
                ),
                leadingIcon = Icons.Outlined.Person,
                errorRes = uiState.firstNameError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            IdeTextField(
                value = uiState.lastName,
                onValueChange = {
                    onEvent(
                        RegisterEvent.LastNameChanged(it)
                    )
                },
                label = stringResource(
                    R.string.register_last_name
                ),
                leadingIcon = Icons.Outlined.Person,
                errorRes = uiState.lastNameError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            IdeTextField(
                value = uiState.email,
                onValueChange = {
                    onEvent(
                        RegisterEvent.EmailChanged(it)
                    )
                },
                label = stringResource(
                    R.string.register_email
                ),
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                errorRes = uiState.emailError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(6.dp)
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
                        RegisterEvent.SabbathSchoolClassChanged(it)
                    )
                },
                errorRes = uiState.sabbathSchoolClassError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            IdePasswordField(
                value = uiState.password,
                onValueChange = {
                    onEvent(
                        RegisterEvent.PasswordChanged(it)
                    )
                },
                label = stringResource(
                    R.string.register_password
                ),
                errorRes = uiState.passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            IdePasswordField(
                value = uiState.confirmPassword,
                onValueChange = {
                    onEvent(
                        RegisterEvent.ConfirmPasswordChanged(it)
                    )
                },
                label = stringResource(
                    R.string.register_confirm_password
                ),
                errorRes = uiState.confirmPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.errorMessage?.let { errorRes ->
                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = stringResource(errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            IdePrimaryButton(
                text = stringResource(
                    R.string.register_button
                ),
                onClick = {
                    onEvent(RegisterEvent.Register)
                },
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(
                        R.string.register_has_account
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = stringResource(
                        R.string.register_login
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        onLoginClick()
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }
}
