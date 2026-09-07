package br.com.ide.presentation.feature.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import br.com.ide.presentation.components.IdeBackButton
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.components.IdeTextField

@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onEvent: (ForgotPasswordEvent) -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {

    val isCooldownActive =
        uiState.resendSecondsRemaining > 0

    val buttonText = when {

        isCooldownActive -> {
            stringResource(
                R.string.forgot_password_wait_to_resend,
                uiState.resendSecondsRemaining
            )
        }

        uiState.isEmailSent -> {
            stringResource(
                R.string.forgot_password_resend
            )
        }

        else -> {
            stringResource(
                R.string.forgot_password_button
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                )
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IdeBackButton(
                    onClick = onBackClick,
                    contentDescription =
                        stringResource(
                            R.string.forgot_password_back
                        )
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                IdeScreenTitle(
                    text = stringResource(
                        R.string.forgot_password_title
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            IdeScreenSubtitle(
                text = stringResource(
                    R.string.forgot_password_subtitle
                ),
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            IdeTextField(
                value = uiState.email,
                onValueChange = {
                    onEvent(
                        ForgotPasswordEvent.EmailChanged(it)
                    )
                },
                label = stringResource(
                    R.string.forgot_password_email
                ),
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                errorRes = uiState.emailError,
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

            uiState.successMessage?.let { successRes ->

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = stringResource(successRes),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            IdePrimaryButton(
                text = buttonText,
                onClick = {
                    onEvent(
                        ForgotPasswordEvent.SendResetEmail
                    )
                },
                enabled = !isCooldownActive,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = stringResource(
                        R.string.forgot_password_remembered
                    ),
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = stringResource(
                        R.string.forgot_password_login
                    ),
                    color =
                        MaterialTheme.colorScheme.primary,
                    fontWeight =
                        FontWeight.SemiBold,
                    modifier =
                        Modifier.clickable {
                            onLoginClick()
                        }
                )
            }
        }
    }
}