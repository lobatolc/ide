package br.com.ide.presentation.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.usecase.GetChurchByIdUseCase
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetDistrictByIdUseCase
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
    GetCurrentUserProfileUseCase,

    private val getDistrictByIdUseCase:
    GetDistrictByIdUseCase,

    private val getChurchByIdUseCase:
    GetChurchByIdUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ProfileUiState()
        )

    val uiState:
            StateFlow<ProfileUiState> =
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
                        firstName =
                            event.firstName,
                        lastName =
                            event.lastName,
                        sabbathSchoolClass =
                            event.sabbathSchoolClass,
                        isLoading = false
                    )
                }

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

                    val districtName =
                        loadDistrictName(
                            user.districtId
                        )

                    val churchName =
                        loadChurchName(
                            user.churchId
                        )

                    _uiState.update {
                        it.copy(
                            firstName =
                                user.firstName,

                            lastName =
                                user.lastName,

                            email =
                                user.email,

                            districtName =
                                districtName,

                            churchName =
                                churchName,

                            sabbathSchoolClass =
                                user.sabbathSchoolClass,

                            role =
                                user.role,

                            isLoading = false,

                            errorMessage = null
                        )
                    }
                }
                .onFailure {

                    if (showLoading) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage =
                                    R.string
                                        .profile_loading_error
                            )
                        }
                    }
                }
        }
    }

    private suspend fun loadDistrictName(
        districtId: String?
    ): String {

        if (districtId.isNullOrBlank()) {
            return ""
        }

        return getDistrictByIdUseCase(
            districtId
        )
            .getOrNull()
            ?.name
            .orEmpty()
    }

    private suspend fun loadChurchName(
        churchId: String?
    ): String {

        if (churchId.isNullOrBlank()) {
            return ""
        }

        return getChurchByIdUseCase(
            churchId
        )
            .getOrNull()
            ?.name
            .orEmpty()
    }
}