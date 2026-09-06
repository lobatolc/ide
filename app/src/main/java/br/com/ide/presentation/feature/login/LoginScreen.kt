package br.com.ide.presentation.feature.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ide.R
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    onForgotPasswordClick: () -> Unit = {},
    onCreateAccountClick: () -> Unit = {}
) {
    var passwordVisible by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val credentialManager = remember {
        CredentialManager.create(context)
    }

    val webClientId = stringResource(
        id = R.string.google_web_client_id
    )

    val googleIdOption = remember(webClientId) {
        GetSignInWithGoogleOption.Builder(
            serverClientId = webClientId
        ).build()
    }

    val googleRequest = remember {
        GetCredentialRequest.Builder()
            .addCredentialOption(
                googleIdOption
            )
            .build()
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.login_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.35f),
                                    0.45f to Color.Transparent,
                                    0.72f to Color.Transparent,
                                    1.0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Icon(
                        painter = painterResource(
                            id = R.drawable.ide_logo
                        ),
                        contentDescription = "Logo IDE",
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .height(135.dp),
                        tint = Color.Unspecified
                    )



                    Text(
                        text = stringResource(R.string.login_slogan),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.height(56.dp)
                    )

                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().offset(y = (-32).dp),
                shape = RoundedCornerShape(
                    topStart = 32.dp,
                    topEnd = 32.dp
                ),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = {
                            onEvent(LoginEvent.EmailChanged(it))
                        },
                        label = { Text(stringResource(R.string.login_email_label)) },
                        placeholder = { Text(stringResource(R.string.login_email_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.emailError != null,
                        supportingText = {
                            uiState.emailError?.let { errorRes ->
                                Text(
                                    text = stringResource(errorRes)
                                )
                            }
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = {
                            onEvent(LoginEvent.PasswordChanged(it))
                        },
                        label = { Text(stringResource(R.string.login_password_label)) },
                        placeholder = { Text(stringResource(R.string.login_password_placeholder)) },
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
                                        stringResource(R.string.login_hide_password)
                                    } else {
                                        stringResource(R.string.login_show_password)
                                    }
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.passwordError != null,
                        supportingText = {
                            uiState.passwordError?.let { errorRes ->
                                Text(
                                    text = stringResource(errorRes)
                                )
                            }
                        }
                    )

                    uiState.errorMessage?.let { errorRes ->
                        Text(
                            text = stringResource(errorRes),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(R.string.login_forgot_password),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    onForgotPasswordClick()
                                }
                                .padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Button(
                        onClick = {
                            onEvent(LoginEvent.Login)
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.login_button),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(28.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = stringResource(R.string.login_or),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(28.dp)
                    )

                    OutlinedButton(
                        onClick = {

                            scope.launch {

                                try {

                                    val result =
                                        credentialManager.getCredential(
                                            context = context,
                                            request = googleRequest
                                        )

                                    val credential =
                                        result.credential

                                    if (
                                        credential is CustomCredential &&
                                        credential.type ==
                                        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                    ) {

                                        val googleCredential =
                                            GoogleIdTokenCredential
                                                .createFrom(
                                                    credential.data
                                                )

                                        onEvent(
                                            LoginEvent.GoogleLogin(
                                                googleCredential.idToken
                                            )
                                        )
                                    }

                                } catch (exception: GetCredentialException) {

                                    onEvent(
                                        LoginEvent.GoogleLoginError
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.ic_google_logo
                            ),
                            contentDescription = stringResource(R.string.google_logo_description),
                            modifier = Modifier.size(20.dp),
                            tint = Color.Unspecified
                        )

                        Spacer(
                            modifier = Modifier.size(12.dp)
                        )

                        Text(
                            text = stringResource(R.string.login_google_button),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(36.dp)
                    )

                    Text(
                        text = stringResource(R.string.login_no_account),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = stringResource(R.string.login_create_account),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            onCreateAccountClick()
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(40.dp)
                    )

                    Text(
                        text = stringResource(R.string.login_bible_verse),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = stringResource(R.string.login_bible_reference),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}