package br.com.ide.presentation.feature.usermanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.UserRole
import br.com.ide.domain.usecase.GetChurchesByDistrictUseCase
import br.com.ide.domain.usecase.GetChurchesUseCase
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetDistrictsUseCase
import br.com.ide.domain.usecase.GetManageableUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase,

    private val getManageableUsersUseCase:
    GetManageableUsersUseCase,

    private val getDistrictsUseCase:
    GetDistrictsUseCase,

    private val getChurchesUseCase:
    GetChurchesUseCase,

    private val getChurchesByDistrictUseCase:
    GetChurchesByDistrictUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            UserManagementUiState()
        )

    val uiState: StateFlow<UserManagementUiState> =
        _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(
        event: UserManagementEvent
    ) {
        when (event) {

            is UserManagementEvent.SearchChanged -> {
                _uiState.update {
                    it.copy(
                        searchQuery = event.value
                    )
                }

                applyFilters()
            }

            is UserManagementEvent.DistrictSelected -> {
                selectDistrict(
                    event.districtId
                )
            }

            is UserManagementEvent.ChurchSelected -> {
                _uiState.update {
                    it.copy(
                        selectedChurchId =
                            event.churchId
                    )
                }

                applyFilters()
            }

            UserManagementEvent.Refresh -> {
                loadData()
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val managerResult =
                getCurrentUserProfileUseCase()

            val manager =
                managerResult.getOrElse {

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                R.string
                                    .user_management_loading_error
                        )
                    }

                    return@launch
                }

            val usersResult =
                getManageableUsersUseCase(
                    manager
                )

            val users =
                usersResult.getOrElse {

                    _uiState.update {
                        it.copy(
                            manager = manager,
                            isLoading = false,
                            errorMessage =
                                R.string
                                    .user_management_loading_error
                        )
                    }

                    return@launch
                }

            when (manager.role) {

                UserRole.PASTOR -> {
                    loadPastorData(
                        managerDistrictId =
                            manager.districtId,
                        manager = manager,
                        users = users
                    )
                }

                UserRole.ADMIN -> {
                    loadAdminData(
                        manager = manager,
                        users = users
                    )
                }

                UserRole.LEADER,
                UserRole.MISSIONARY -> {

                    _uiState.update {
                        it.copy(
                            manager = manager,
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

    private suspend fun loadPastorData(
        managerDistrictId: String?,
        manager: br.com.ide.domain.model.UserProfile,
        users: List<br.com.ide.domain.model.UserProfile>
    ) {

        if (managerDistrictId.isNullOrBlank()) {

            _uiState.update {
                it.copy(
                    manager = manager,
                    isLoading = false,
                    errorMessage =
                        R.string
                            .user_management_loading_error
                )
            }

            return
        }

        val districts =
            getDistrictsUseCase()
                .getOrDefault(
                    emptyList()
                )

        val churches =
            getChurchesByDistrictUseCase(
                managerDistrictId
            ).getOrDefault(
                emptyList()
            )

        _uiState.update {
            it.copy(
                manager = manager,
                users = users,
                filteredUsers = users,
                districts = districts,
                churches = churches,
                selectedDistrictId =
                    managerDistrictId,
                selectedChurchId = null,
                isLoading = false,
                errorMessage = null
            )
        }

        applyFilters()
    }

    private suspend fun loadAdminData(
        manager: br.com.ide.domain.model.UserProfile,
        users: List<br.com.ide.domain.model.UserProfile>
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
                users = users,
                filteredUsers = users,
                districts = districts,
                churches = churches,
                selectedDistrictId = null,
                selectedChurchId = null,
                isLoading = false,
                errorMessage = null
            )
        }

        applyFilters()
    }

    private fun selectDistrict(
        districtId: String?
    ) {

        val manager =
            _uiState.value.manager
                ?: return

        if (
            manager.role ==
            UserRole.PASTOR
        ) {
            return
        }

        _uiState.update {
            it.copy(
                selectedDistrictId =
                    districtId,
                selectedChurchId = null
            )
        }

        applyFilters()
    }

    private fun applyFilters() {

        val state =
            _uiState.value

        val search =
            state.searchQuery
                .trim()

        val filtered =
            state.users.filter { user ->

                val fullName =
                    "${user.firstName} ${user.lastName}"

                val matchesSearch =
                    search.isBlank() ||
                            fullName.contains(
                                search,
                                ignoreCase = true
                            )

                val matchesDistrict =
                    state.selectedDistrictId == null ||
                            user.districtId ==
                            state.selectedDistrictId

                val matchesChurch =
                    state.selectedChurchId == null ||
                            user.churchId ==
                            state.selectedChurchId

                matchesSearch &&
                        matchesDistrict &&
                        matchesChurch
            }

        _uiState.update {
            it.copy(
                filteredUsers = filtered
            )
        }
    }
}