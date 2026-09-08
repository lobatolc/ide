package br.com.ide.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.usecase.GetCurrentUserProfileUseCase
import br.com.ide.domain.usecase.GetMissionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMissionsUseCase: GetMissionsUseCase,
    private val getCurrentUserProfileUseCase:
    GetCurrentUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    private var allMissions: List<Mission> = emptyList()

    init {
        loadUserProfile()
        onEvent(HomeEvent.LoadMissions)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {

            HomeEvent.LoadMissions -> {
                loadMissions()
            }

            is HomeEvent.SearchChanged -> {
                _uiState.update {
                    it.copy(
                        searchQuery = event.query
                    )
                }

                applyFilters()
            }

            is HomeEvent.StatusSelected -> {
                _uiState.update {
                    it.copy(
                        selectedStatus = event.status
                    )
                }

                applyFilters()
            }

            HomeEvent.Retry -> {
                loadMissions()
            }
        }
    }

    private fun loadMissions() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val missions = getMissionsUseCase()

                allMissions = missions

                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }

                applyFilters()

            } catch (exception: Exception) {

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            R.string.home_loading_error
                    )
                }
            }
        }
    }

    private fun applyFilters() {

        val state = _uiState.value

        val filteredMissions = allMissions
            .filter { mission ->

                val matchesSearch =
                    state.searchQuery.isBlank() ||
                            mission.name.contains(
                                state.searchQuery,
                                ignoreCase = true
                            ) ||
                            mission.address.contains(
                                state.searchQuery,
                                ignoreCase = true
                            )

                val matchesStatus =
                    state.selectedStatus == null ||
                            mission.status == state.selectedStatus

                matchesSearch && matchesStatus
            }
            .sortedBy { mission ->
                mission.scheduledAt
            }

        _uiState.update {
            it.copy(
                missions = filteredMissions
            )
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {

            getCurrentUserProfileUseCase()
                .onSuccess { user ->

                    _uiState.update {
                        it.copy(
                            userName = user.firstName
                        )
                    }
                }
        }
    }
}