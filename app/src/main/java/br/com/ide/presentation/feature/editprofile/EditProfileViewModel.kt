package br.com.ide.presentation.feature.editprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.UserProfile
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase,
    private val updateUserProfileUseCase:
    UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(EditProfileUiState())

    val uiState: StateFlow<EditProfileUiState> =
        _uiState.asStateFlow()

    private var currentUserProfile: UserProfile? = null

    init {
        loadProfile()
    }

    fun onEvent(
        event: EditProfileEvent
    ) {
        when (event) {

            is EditProfileEvent.FirstNameChanged -> {
                _uiState.update {
                    it.copy(
                        firstName = event.firstName,
                        firstNameError = null,
                        errorMessage = null
                    )
                }
            }

            is EditProfileEvent.LastNameChanged -> {
                _uiState.update {
                    it.copy(
                        lastName = event.lastName,
                        lastNameError = null,
                        errorMessage = null
                    )
                }
            }

            is EditProfileEvent.SabbathSchoolClassChanged -> {
                _uiState.update {
                    it.copy(
                        sabbathSchoolClass =
                            event.sabbathSchoolClass,
                        sabbathSchoolClassError = null,
                        errorMessage = null
                    )
                }
            }

            EditProfileEvent.SaveChanges -> {
                saveChanges()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            getCurrentUserProfileUseCase()
                .onSuccess { user ->

                    currentUserProfile = user

                    _uiState.update {
                        it.copy(
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            sabbathSchoolClass =
                                user.sabbathSchoolClass,
                            isLoading = false
                        )
                    }
                }
                .onFailure {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                R.string.profile_loading_error
                        )
                    }
                }
        }
    }

    private fun saveChanges() {

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
                R.string.error_sabbath_school_class_required
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
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    sabbathSchoolClassError =
                        sabbathSchoolClassError
                )
            }

            return
        }

        val currentUser =
            currentUserProfile
                ?: return

        val sabbathSchoolClass =
            state.sabbathSchoolClass
                ?: return

        val updatedUser =
            currentUser.copy(
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                sabbathSchoolClass =
                    sabbathSchoolClass
            )

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            updateUserProfileUseCase(
                updatedUser
            )
                .onSuccess {

                    currentUserProfile =
                        updatedUser

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true
                        )
                    }
                }
                .onFailure {

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage =
                                R.string.profile_save_error
                        )
                    }
                }
        }
    }
}