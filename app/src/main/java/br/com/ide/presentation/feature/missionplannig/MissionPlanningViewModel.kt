package br.com.ide.presentation.feature.missionplanning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.domain.usecase.GetMissionByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissionPlanningViewModel @Inject constructor(
    private val getMissionByIdUseCase:
    GetMissionByIdUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MissionPlanningUiState()
        )

    val uiState:
            StateFlow<MissionPlanningUiState> =
        _uiState.asStateFlow()

    private var loadedMissionId:
            String? =
        null

    fun load(
        missionId: String,
        force: Boolean = false
    ) {

        if (
            !force &&
            loadedMissionId == missionId
        ) {
            return
        }

        loadedMissionId =
            missionId

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }

            try {

                val mission =
                    getMissionByIdUseCase(
                        missionId
                    )

                if (
                    mission == null
                ) {

                    _uiState.update {
                        it.copy(
                            isLoading = false
                        )
                    }

                    return@launch
                }

                _uiState.update {
                    it.copy(
                        missionId =
                            mission.id,

                        missionName =
                            mission.name,

                        hasDepartureLocation =
                            mission.departureLocation != null,

                        hasReturnLocation =
                            mission.returnLocation != null,

                        isLoading =
                            false
                    )
                }

            } catch (
                exception: Exception
            ) {

                _uiState.update {
                    it.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh(
        missionId: String
    ) {

        load(
            missionId = missionId,
            force = true
        )
    }
}