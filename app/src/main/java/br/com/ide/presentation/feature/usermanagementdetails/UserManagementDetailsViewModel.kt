package br.com.ide.presentation.feature.usermanagementdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.policy.UserManagementPolicy
import br.com.ide.domain.usecase.GetChurchesByDistrictUseCase
import br.com.ide.domain.usecase.GetChurchesUseCase
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetDistrictsUseCase
import br.com.ide.domain.usecase.GetUserByIdUseCase
import br.com.ide.domain.usecase.UpdateUserAssignmentUseCase
import br.com.ide.presentation.mapper.toUserManagementErrorRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementDetailsViewModel @Inject constructor(
    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase,
    private val getUserByIdUseCase:
    GetUserByIdUseCase,
    private val getDistrictsUseCase:
    GetDistrictsUseCase,
    private val getChurchesUseCase:
    GetChurchesUseCase,
    private val getChurchesByDistrictUseCase:
    GetChurchesByDistrictUseCase,
    private val updateUserAssignmentUseCase:
    UpdateUserAssignmentUseCase,
    private val userManagementPolicy:
    UserManagementPolicy
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            UserManagementDetailsUiState()
        )

    val uiState:
            StateFlow<UserManagementDetailsUiState> =
        _uiState.asStateFlow()

    private var loadedUserId: String? = null

    fun load(
        userId: String
    ) {
        if (loadedUserId == userId) {
            return
        }

        loadedUserId = userId

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val manager =
                getCurrentUserProfileUseCase()
                    .getOrElse {
                        showLoadingError()
                        return@launch
                    }

            val target =
                getUserByIdUseCase(
                    userId
                ).getOrElse {
                    showLoadingError()
                    return@launch
                }

            if (
                !userManagementPolicy.canManageUser(
                    manager = manager,
                    target = target
                )
            ) {
                _uiState.update {
                    it.copy(
                        manager = manager,
                        target = target,
                        isLoading = false,
                        errorMessage =
                            R.string
                                .user_management_access_denied
                    )
                }

                return@launch
            }

            val roles =
                userManagementPolicy
                    .getAssignableRoles(
                        manager = manager,
                        target = target
                    )

            when (manager.role) {

                UserRole.PASTOR -> {
                    loadPastorData(
                        manager = manager,
                        target = target,
                        roles = roles
                    )
                }

                UserRole.ADMIN -> {
                    loadAdminData(
                        manager = manager,
                        target = target,
                        roles = roles
                    )
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                R.string
                                    .user_management_access_denied
                        )
                    }
                }
            }
        }
    }

    fun onEvent(
        event: UserManagementDetailsEvent
    ) {
        when (event) {

            is UserManagementDetailsEvent.RoleSelected -> {
                selectRole(
                    event.role
                )
            }

            is UserManagementDetailsEvent.DistrictSelected -> {
                selectDistrict(
                    event.districtId
                )
            }

            is UserManagementDetailsEvent.ChurchSelected -> {
                _uiState.update {
                    it.copy(
                        selectedChurchId =
                            event.churchId,
                        errorMessage = null
                    )
                }
            }

            UserManagementDetailsEvent.Save -> {
                save()
            }
        }
    }

    private suspend fun loadPastorData(
        manager: br.com.ide.domain.model.UserProfile,
        target: br.com.ide.domain.model.UserProfile,
        roles: List<UserRole>
    ) {
        val districtId =
            manager.districtId
                ?: run {
                    showLoadingError()
                    return
                }

        val districts =
            getDistrictsUseCase()
                .getOrDefault(
                    emptyList()
                )

        val churches =
            getChurchesByDistrictUseCase(
                districtId
            ).getOrDefault(
                emptyList()
            )

        _uiState.update {
            it.copy(
                manager = manager,
                target = target,
                roles = roles,
                districts = districts,
                churches = churches,
                selectedRole = target.role,
                selectedDistrictId = districtId,
                selectedChurchId = target.churchId,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private suspend fun loadAdminData(
        manager: br.com.ide.domain.model.UserProfile,
        target: br.com.ide.domain.model.UserProfile,
        roles: List<UserRole>
    ) {
        val districts =
            getDistrictsUseCase()
                .getOrDefault(
                    emptyList()
                )

        val churches =
            getChurchesUseCase()
                .getOrDefault(
                    emptyList()
                )

        _uiState.update {
            it.copy(
                manager = manager,
                target = target,
                roles = roles,
                districts = districts,
                churches = churches,
                selectedRole = target.role,
                selectedDistrictId =
                    target.districtId,
                selectedChurchId =
                    target.churchId,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private fun selectRole(
        role: UserRole
    ) {
        _uiState.update { state ->

            when (role) {

                UserRole.PASTOR -> {
                    state.copy(
                        selectedRole = role,
                        selectedChurchId = null,
                        errorMessage = null
                    )
                }

                UserRole.ADMIN -> {
                    state.copy(
                        selectedRole = role,
                        selectedDistrictId = null,
                        selectedChurchId = null,
                        errorMessage = null
                    )
                }

                UserRole.MISSIONARY,
                UserRole.LEADER -> {
                    state.copy(
                        selectedRole = role,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun selectDistrict(
        districtId: String
    ) {
        val manager =
            _uiState.value.manager
                ?: return

        if (manager.role != UserRole.ADMIN) {
            return
        }

        _uiState.update {
            it.copy(
                selectedDistrictId =
                    districtId,
                selectedChurchId = null,
                errorMessage = null
            )
        }
    }

    private fun save() {
        val state =
            _uiState.value

        val manager =
            state.manager
                ?: return

        val target =
            state.target
                ?: return

        val role =
            state.selectedRole
                ?: return

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            updateUserAssignmentUseCase(
                manager = manager,
                target = target,
                newRole = role,
                newDistrictId =
                    state.selectedDistrictId,
                newChurchId =
                    state.selectedChurchId
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaved = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage =
                                error
                                    .toUserManagementErrorRes()
                        )
                    }
                }
        }
    }

    private fun showLoadingError() {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage =
                    R.string
                        .user_management_loading_error
            )
        }
    }
}