package br.com.ide.presentation.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(ProfileUiState())

    val uiState: StateFlow<ProfileUiState> =
        _uiState.asStateFlow()

    init {
        loadProfile(
            showLoading = true
        )
    }

    fun onEvent(
        event: ProfileEvent
    ) {
        when (event) {

            ProfileEvent.Refresh -> {
                loadProfile(
                    showLoading = false
                )
            }

            is ProfileEvent.ProfileUpdated -> {

                _uiState.update {
                    it.copy(
                        firstName = event.firstName,
                        lastName = event.lastName,
                        sabbathSchoolClass =
                            event.sabbathSchoolClass,
                        isLoading = false
                    )
                }

                /*
                 * Confirma silenciosamente os dados
                 * com o Firestore.
                 */
                loadProfile(
                    showLoading = false
                )
            }
        }
    }

    private fun loadProfile(
        showLoading: Boolean
    ) {
        viewModelScope.launch {

            if (showLoading) {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                }
            }

            getCurrentUserProfileUseCase()
                .onSuccess { user ->

                    _uiState.update {
                        it.copy(
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            sabbathSchoolClass =
                                user.sabbathSchoolClass,
                            role = user.role,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure {

                    /*
                     * Se já temos dados na tela,
                     * não vamos destruí-los só porque
                     * o refresh silencioso falhou.
                     */
                    if (showLoading) {
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
    }
}