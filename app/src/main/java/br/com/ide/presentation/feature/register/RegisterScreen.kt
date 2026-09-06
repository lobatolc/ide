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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.domain.model.SabbathSchoolClass
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        errorBorderColor = MaterialTheme.colorScheme.error,

        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        errorLabelColor = MaterialTheme.colorScheme.error,

        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,

        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        errorContainerColor = MaterialTheme.colorScheme.surface,

        cursorColor = MaterialTheme.colorScheme.primary
    )

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var confirmPasswordVisible by remember {
        mutableStateOf(false)
    }

    var schoolClassMenuExpanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
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
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(
                            R.string.register_back
                        ),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = stringResource(R.string.register_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedTextField(
                value = uiState.firstName,
                onValueChange = {
                    onEvent(
                        RegisterEvent.FirstNameChanged(it)
                    )
                },
                label = {
                    Text(
                        stringResource(
                            R.string.register_first_name
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null
                    )
                },
                singleLine = true,
                isError = uiState.firstNameError != null,
                supportingText = {
                    uiState.firstNameError?.let { errorRes ->
                        Text(
                            text = stringResource(errorRes)
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedTextField(
                value = uiState.lastName,
                onValueChange = {
                    onEvent(
                        RegisterEvent.LastNameChanged(it)
                    )
                },
                label = {
                    Text(
                        stringResource(
                            R.string.register_last_name
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null
                    )
                },
                singleLine = true,
                isError = uiState.lastNameError != null,
                supportingText = {
                    uiState.lastNameError?.let { errorRes ->
                        Text(
                            text = stringResource(errorRes)
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedTextField(
                value = uiState.email,
                onValueChange = {
                    onEvent(
                        RegisterEvent.EmailChanged(it)
                    )
                },
                label = {
                    Text(
                        stringResource(
                            R.string.register_email
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let { errorRes ->
                        Text(
                            text = stringResource(errorRes)
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            ExposedDropdownMenuBox(
                expanded = schoolClassMenuExpanded,
                onExpandedChange = {
                    schoolClassMenuExpanded = !schoolClassMenuExpanded
                }
            ) {
                OutlinedTextField(
                    value = uiState.sabbathSchoolClass
                        ?.let {
                            stringResource(
                                it.toStringRes()
                            )
                        }
                        ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(
                            text = stringResource(
                                R.string.register_sabbath_school_class
                            )
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = schoolClassMenuExpanded
                        )
                    },
                    isError = uiState.sabbathSchoolClassError != null,
                    supportingText = {
                        uiState.sabbathSchoolClassError?.let { errorRes ->
                            Text(
                                text = stringResource(errorRes)
                            )
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth(),
                    colors = textFieldColors
                )

                ExposedDropdownMenu(
                    expanded = schoolClassMenuExpanded,
                    onDismissRequest = {
                        schoolClassMenuExpanded = false
                    }
                ) {
                    SabbathSchoolClass.entries.forEach { schoolClass ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(
                                        schoolClass.toStringRes()
                                    )
                                )
                            },
                            onClick = {
                                onEvent(
                                    RegisterEvent.SabbathSchoolClassChanged(
                                        schoolClass
                                    )
                                )

                                schoolClassMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = {
                    onEvent(
                        RegisterEvent.PasswordChanged(it)
                    )
                },
                label = {
                    Text(
                        stringResource(
                            R.string.register_password
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                stringResource(
                                    R.string.login_hide_password
                                )
                            } else {
                                stringResource(
                                    R.string.login_show_password
                                )
                            }
                        )
                    }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                isError = uiState.passwordError != null,
                supportingText = {
                    uiState.passwordError?.let { errorRes ->
                        Text(
                            text = stringResource(errorRes)
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = {
                    onEvent(
                        RegisterEvent.ConfirmPasswordChanged(it)
                    )
                },
                label = {
                    Text(
                        stringResource(
                            R.string.register_confirm_password
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            confirmPasswordVisible =
                                !confirmPasswordVisible
                        }
                    ) {
                        Icon(
                            imageVector =
                                if (confirmPasswordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                            contentDescription =
                                if (confirmPasswordVisible) {
                                    stringResource(
                                        R.string.login_hide_password
                                    )
                                } else {
                                    stringResource(
                                        R.string.login_show_password
                                    )
                                }
                        )
                    }
                },
                visualTransformation =
                    if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                isError =
                    uiState.confirmPasswordError != null,
                supportingText = {
                    uiState.confirmPasswordError
                        ?.let { errorRes ->
                            Text(
                                text = stringResource(errorRes)
                            )
                        }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
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

            Button(
                onClick = {
                    onEvent(RegisterEvent.Register)
                },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(22.dp)
                            .height(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.register_button
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

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
