package br.com.ide.presentation.feature.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.SabbathSchoolClass
import br.com.ide.domain.usecase.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {

            is RegisterEvent.FirstNameChanged -> {
                _uiState.update {
                    it.copy(
                        firstName = event.firstName,
                        firstNameError = null
                    )
                }
            }

            is RegisterEvent.LastNameChanged -> {
                _uiState.update {
                    it.copy(
                        lastName = event.lastName,
                        lastNameError = null
                    )
                }
            }

            is RegisterEvent.EmailChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.email,
                        emailError = null
                    )
                }
            }

            is RegisterEvent.SabbathSchoolClassChanged -> {
                _uiState.update {
                    it.copy(
                        sabbathSchoolClass = event.sabbathSchoolClass,
                        sabbathSchoolClassError = null
                    )
                }
            }

            is RegisterEvent.PasswordChanged -> {
                _uiState.update {
                    it.copy(
                        password = event.password,
                        passwordError = null
                    )
                }
            }

            is RegisterEvent.ConfirmPasswordChanged -> {
                _uiState.update {
                    it.copy(
                        confirmPassword = event.confirmPassword,
                        confirmPasswordError = null
                    )
                }
            }

            RegisterEvent.Register -> register()
        }
    }

    private fun register() {

        val state = _uiState.value

        val firstNameError = when {
            state.firstName.isBlank() ->
                R.string.error_first_name_required

            else -> null
        }

        val lastNameError = when {
            state.lastName.isBlank() ->
                R.string.error_last_name_required

            else -> null
        }

        val emailError = when {
            state.email.isBlank() ->
                R.string.error_email_required

            !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() ->
                R.string.error_email_invalid

            else -> null
        }

        val sabbathSchoolClassError = when {
            state.sabbathSchoolClass == null ->
                R.string.error_sabbath_school_class_required

            else -> null
        }

        val passwordError = when {
            state.password.isBlank() ->
                R.string.error_password_required

            state.password.length < 6 ->
                R.string.error_password_min_length

            else -> null
        }

        val confirmPasswordError = when {
            state.confirmPassword.isBlank() ->
                R.string.error_confirm_password_required

            state.password != state.confirmPassword ->
                R.string.error_passwords_do_not_match

            else -> null
        }

        val hasError =
            firstNameError != null ||
                    lastNameError != null ||
                    emailError != null ||
                    sabbathSchoolClassError != null ||
                    passwordError != null ||
                    confirmPasswordError != null

        if (hasError) {
            _uiState.update {
                it.copy(
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    emailError = emailError,
                    sabbathSchoolClassError = sabbathSchoolClassError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }

            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            registerUserUseCase(
                firstName = state.firstName,
                lastName = state.lastName,
                email = state.email.trim(),
                sabbathSchoolClass = state.sabbathSchoolClass!!,
                password = state.password
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRegistered = true
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = R.string.error_register_generic
                        )
                    }
                }
        }
    }
}