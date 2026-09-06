package br.com.ide.presentation.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import br.com.ide.domain.usecase.LoginWithGoogleUseCase
import br.com.ide.presentation.mapper.mapFirebaseAuthError

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {

        when (event) {

            is LoginEvent.EmailChanged -> {

                _uiState.value =
                    _uiState.value.copy(
                        email = event.email,
                        errorMessage = null
                    )
            }

            is LoginEvent.PasswordChanged -> {

                _uiState.value =
                    _uiState.value.copy(
                        password = event.password,
                        errorMessage = null
                    )
            }

            LoginEvent.Login -> {
                login()
            }

            is LoginEvent.GoogleLogin -> {
                loginWithGoogle(event.idToken)
            }

            LoginEvent.GoogleLoginError -> {

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = R.string.error_google_login
                )
            }
        }
    }

    private fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        val emailError =
            when {
                email.isBlank() ->
                    R.string.error_email_required

                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches() ->
                    R.string.error_email_invalid

                else -> null
            }

        val passwordError =
            when {
                password.isBlank() ->
                    R.string.error_password_required

                else -> null
            }

        if (emailError != null || passwordError != null) {
            _uiState.value = _uiState.value.copy(
                emailError = emailError,
                passwordError = passwordError,
                errorMessage = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                emailError = null,
                passwordError = null,
                errorMessage = null
            )

            val result = loginUseCase(email, password)

            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                .onFailure { exception ->

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = mapFirebaseAuthError(
                                exception
                            )
                        )
                }
        }
    }

    private fun loginWithGoogle(
        idToken: String
    ) {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            loginWithGoogleUseCase(idToken)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = mapFirebaseAuthError(exception)
                    )
                }
        }
    }
}