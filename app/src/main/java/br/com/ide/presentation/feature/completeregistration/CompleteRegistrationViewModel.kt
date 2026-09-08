package br.com.ide.presentation.feature.completeregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.usecase.CompleteGoogleRegistrationUseCase
import br.com.ide.domain.usecase.GetCurrentAuthenticatedUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteRegistrationViewModel @Inject constructor(
    private val completeGoogleRegistrationUseCase:
    CompleteGoogleRegistrationUseCase,
    private val getCurrentAuthenticatedUserUseCase:
    GetCurrentAuthenticatedUserUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CompleteRegistrationUiState()
        )

    val uiState: StateFlow<CompleteRegistrationUiState> =
        _uiState.asStateFlow()

    init {
        loadAuthenticatedUser()
    }

    fun onEvent(
        event: CompleteRegistrationEvent
    ) {
        when (event) {

            is CompleteRegistrationEvent.FirstNameChanged -> {
                _uiState.update {
                    it.copy(
                        firstName = event.firstName,
                        firstNameError = null
                    )
                }
            }

            is CompleteRegistrationEvent.LastNameChanged -> {
                _uiState.update {
                    it.copy(
                        lastName = event.lastName,
                        lastNameError = null
                    )
                }
            }

            is CompleteRegistrationEvent
            .SabbathSchoolClassChanged -> {

                _uiState.update {
                    it.copy(
                        sabbathSchoolClass =
                            event.sabbathSchoolClass,
                        sabbathSchoolClassError = null
                    )
                }
            }

            CompleteRegistrationEvent
                .CompleteRegistration -> {

                completeRegistration()
            }
        }
    }

    private fun loadAuthenticatedUser() {

        val googleUser =
            getCurrentAuthenticatedUserUseCase()
                ?: return

        val nameParts =
            googleUser.displayName
                .trim()
                .split(
                    regex = Regex("\\s+"),
                    limit = 2
                )

        val firstName =
            nameParts
                .firstOrNull()
                .orEmpty()

        val lastName =
            nameParts
                .getOrNull(1)
                .orEmpty()

        _uiState.update {
            it.copy(
                googleUser = googleUser,
                firstName = firstName,
                lastName = lastName,
                email = googleUser.email
            )
        }
    }

    private fun completeRegistration() {

        val state = _uiState.value

        val firstNameError =
            if (state.firstName.isBlank()) {
                R.string.error_first_name_required
            } else {
                null
            }

        val lastNameError =
            if (state.lastName.isBlank()) {
                R.string.error_last_name_required
            } else {
                null
            }

        val sabbathSchoolClassError =
            if (state.sabbathSchoolClass == null) {
                R.string
                    .error_sabbath_school_class_required
            } else {
                null
            }

        if (
            firstNameError != null ||
            lastNameError != null ||
            sabbathSchoolClassError != null
        ) {
            _uiState.update {
                it.copy(
                    firstNameError =
                        firstNameError,
                    lastNameError =
                        lastNameError,
                    sabbathSchoolClassError =
                        sabbathSchoolClassError
                )
            }

            return
        }

        val googleUser =
            state.googleUser
                ?: return

        val sabbathSchoolClass =
            state.sabbathSchoolClass
                ?: return

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            completeGoogleRegistrationUseCase(
                googleUser = googleUser,
                firstName = state.firstName,
                lastName = state.lastName,
                sabbathSchoolClass =
                    sabbathSchoolClass
            )
                .onSuccess {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCompleted = true
                        )
                    }
                }
                .onFailure {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                R.string.error_register_generic
                        )
                    }
                }
        }
    }
}