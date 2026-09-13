package br.com.ide.presentation.feature.missionlocations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.MissionLocation
import br.com.ide.domain.usecase.GetMissionByIdUseCase
import br.com.ide.domain.usecase.ReverseGeocodeUseCase
import br.com.ide.domain.usecase.UpdateMissionLocationsUseCase
import br.com.ide.presentation.components.snackbar.IdeSnackbarManager
import br.com.ide.presentation.components.snackbar.IdeSnackbarMessage
import br.com.ide.presentation.components.snackbar.IdeSnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissionLocationsViewModel @Inject constructor(
    private val getMissionByIdUseCase:
    GetMissionByIdUseCase,

    private val updateMissionLocationsUseCase:
    UpdateMissionLocationsUseCase,

    private val reverseGeocodeUseCase:
    ReverseGeocodeUseCase,

    private val snackbarManager:
    IdeSnackbarManager
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MissionLocationsUiState()
        )

    val uiState:
            StateFlow<MissionLocationsUiState> =
        _uiState.asStateFlow()

    private var loadedMissionId:
            String? =
        null

    // =========================================================
    // Carregamento
    // =========================================================

    fun load(
        missionId: String
    ) {

        if (
            loadedMissionId ==
            missionId
        ) {
            return
        }

        loadedMissionId =
            missionId

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
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
                            isLoading = false,
                            errorMessage =
                                R.string
                                    .mission_locations_loading_error
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

                        missionStatus =
                            mission.status,

                        departureLocation =
                            mission.departureLocation,

                        returnLocation =
                            mission.returnLocation,

                        selectedLocationType =
                            MissionLocationType.DEPARTURE,

                        isLoading =
                            false,

                        errorMessage =
                            null
                    )
                }

            } catch (
                exception: Exception
            ) {

                _uiState.update {
                    it.copy(
                        isLoading =
                            false,
                        errorMessage =
                            R.string
                                .mission_locations_loading_error
                    )
                }
            }
        }
    }

    // =========================================================
    // Eventos
    // =========================================================

    fun onEvent(
        event: MissionLocationsEvent
    ) {

        when (
            event
        ) {

            is MissionLocationsEvent
            .SelectLocationType -> {

                selectLocationType(
                    event.type
                )
            }

            is MissionLocationsEvent
            .LocationSelected -> {

                selectLocation(
                    latitude =
                        event.latitude,
                    longitude =
                        event.longitude
                )
            }

            MissionLocationsEvent
                .RemoveReturnLocation -> {

                removeReturnLocation()
            }

            MissionLocationsEvent.Save -> {

                save()
            }
        }
    }

    // =========================================================
    // Tipo de local
    // =========================================================

    private fun selectLocationType(
        type: MissionLocationType
    ) {

        _uiState.update {
            it.copy(
                selectedLocationType =
                    type,
                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Seleção no mapa + reverse geocoding
    // =========================================================

    private fun selectLocation(
        latitude: Double,
        longitude: Double
    ) {

        val selectedType =
            _uiState.value
                .selectedLocationType

        viewModelScope.launch {

            val geocodedAddress =
                reverseGeocodeUseCase(
                    latitude =
                        latitude,
                    longitude =
                        longitude
                )
                    .getOrNull()

            val location =
                MissionLocation(
                    name =
                        geocodedAddress
                            ?.name
                            .orEmpty(),

                    address =
                        geocodedAddress
                            ?.address
                            .orEmpty(),

                    latitude =
                        latitude,

                    longitude =
                        longitude
                )

            _uiState.update { state ->

                when (
                    selectedType
                ) {

                    MissionLocationType.DEPARTURE -> {

                        state.copy(
                            departureLocation =
                                location,
                            errorMessage =
                                null
                        )
                    }

                    MissionLocationType.RETURN -> {

                        state.copy(
                            returnLocation =
                                location,
                            errorMessage =
                                null
                        )
                    }
                }
            }
        }
    }

    // =========================================================
    // Remover retorno
    // =========================================================

    private fun removeReturnLocation() {

        _uiState.update {
            it.copy(
                returnLocation =
                    null,
                selectedLocationType =
                    MissionLocationType.DEPARTURE,
                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Salvamento
    // =========================================================

    private fun save() {

        val state =
            _uiState.value

        if (
            state.isSaving
        ) {
            return
        }

        val departureLocation =
            state.departureLocation

        if (
            departureLocation ==
            null
        ) {

            _uiState.update {
                it.copy(
                    errorMessage =
                        R.string
                            .mission_locations_departure_required
                )
            }

            return
        }

        if (
            state.missionId.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isSaving =
                        true,
                    errorMessage =
                        null
                )
            }

            updateMissionLocationsUseCase(
                missionId =
                    state.missionId,

                departureLocation =
                    departureLocation,

                returnLocation =
                    state.returnLocation
            )
                .onSuccess {

                    _uiState.update {
                        it.copy(
                            isSaving =
                                false,
                            isSaved =
                                true
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .mission_locations_save_success,
                            type =
                                IdeSnackbarType.SUCCESS
                        )
                    )
                }
                .onFailure {

                    _uiState.update {
                        it.copy(
                            isSaving =
                                false
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .mission_locations_save_error,
                            type =
                                IdeSnackbarType.ERROR
                        )
                    )
                }
        }
    }
}