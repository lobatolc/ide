package br.com.ide.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.domain.usecase.GetActiveMissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getActiveMissionUseCase: GetActiveMissionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            isLoading = true,
            userName = "Lucas"
        )
    )

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    init {
        onEvent(HomeEvent.LoadHome)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {

            HomeEvent.LoadHome -> {
                loadHome()
            }

            HomeEvent.Retry -> {
                loadHome()
            }
        }
    }

    private fun loadHome() {

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {

                val mission = getActiveMissionUseCase()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeMissionName = mission?.name
                )

            } catch (exception: Exception) {

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os dados."
                )
            }
        }
    }
}