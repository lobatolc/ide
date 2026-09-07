package br.com.ide.presentation.feature.forgotpassword

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.usecase.SendPasswordResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> =
        _uiState.asStateFlow()

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {

            is ForgotPasswordEvent.EmailChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.email,
                        emailError = null,
                        errorMessage = null,
                        successMessage = null
                    )
                }
            }

            ForgotPasswordEvent.SendResetEmail -> {
                sendResetEmail()
            }
        }
    }

    private fun sendResetEmail() {

        val state = _uiState.value
        val email = state.email.trim()

        Log.d(
            "ForgotPassword",
            "email=[$email] | tamanho=${email.length} | valido=${
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }"
        )

        val emailError = when {
            email.isBlank() ->
                R.string.error_email_required

            !Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches() ->
                R.string.error_email_invalid

            else -> null
        }

        if (emailError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    errorMessage = null,
                    successMessage = null
                )
            }

            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    emailError = null,
                    errorMessage = null,
                    successMessage = null
                )
            }

            sendPasswordResetEmailUseCase(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEmailSent = true,
                            successMessage =
                                R.string.forgot_password_success
                        )
                    }
                    startResendCooldown()
                }
                .onFailure { exception ->

                    val isUnknownUser =
                        exception is com.google.firebase.auth.FirebaseAuthInvalidUserException ||
                                (
                                        exception is com.google.firebase.auth.FirebaseAuthException &&
                                                exception.errorCode == "ERROR_USER_NOT_FOUND"
                                        )

                    if (isUnknownUser) {

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isEmailSent = true,
                                errorMessage = null,
                                successMessage =
                                    R.string.forgot_password_success
                            )
                        }

                    } else {

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage =
                                    mapForgotPasswordError(exception)
                            )
                        }
                    }
                }
        }
    }

    private fun startResendCooldown() {
        viewModelScope.launch {
            for (seconds in 60 downTo 1) {
                _uiState.update {
                    it.copy(
                        resendSecondsRemaining = seconds
                    )
                }

                kotlinx.coroutines.delay(1_000)
            }

            _uiState.update {
                it.copy(
                    resendSecondsRemaining = 0
                )
            }
        }
    }
}