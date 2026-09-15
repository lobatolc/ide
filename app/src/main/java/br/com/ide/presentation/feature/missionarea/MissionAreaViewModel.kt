package br.com.ide.presentation.feature.missionarea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.R
import br.com.ide.domain.model.MissionArea
import br.com.ide.domain.model.MissionCoordinate
import br.com.ide.domain.usecase.GetMissionByIdUseCase
import br.com.ide.domain.usecase.UpdateMissionAreaUseCase
import br.com.ide.presentation.components.snackbar.IdeSnackbarManager
import br.com.ide.presentation.components.snackbar.IdeSnackbarType
import br.com.ide.presentation.components.snackbar.IdeSnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissionAreaViewModel @Inject constructor(

    private val getMissionByIdUseCase:
    GetMissionByIdUseCase,

    private val updateMissionAreaUseCase:
    UpdateMissionAreaUseCase,

    private val snackbarManager:
    IdeSnackbarManager

) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MissionAreaUiState()
        )

    val uiState:
            StateFlow<MissionAreaUiState> =
        _uiState.asStateFlow()

    private var loadedMissionId:
            String? =
        null

    // =========================================================
    // Carregamento
    // =========================================================

    fun load(
        missionId: String,
        force: Boolean = false
    ) {

        if (
            !force &&
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
                    isLoading =
                        true,

                    errorMessage =
                        null
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
                            isLoading =
                                false,

                            errorMessage =
                                R.string
                                    .mission_area_load_error
                        )
                    }

                    return@launch
                }

                val existingArea =
                    mission.area

                val mapLatitude =
                    mission
                        .departureLocation
                        ?.latitude
                        ?: existingArea
                            ?.polygonPoints
                            ?.firstOrNull()
                            ?.latitude
                        ?: -1.2939

                val mapLongitude =
                    mission
                        .departureLocation
                        ?.longitude
                        ?: existingArea
                            ?.polygonPoints
                            ?.firstOrNull()
                            ?.longitude
                        ?: -47.9260

                _uiState.update {
                    it.copy(

                        missionId =
                            mission.id,

                        missionName =
                            mission.name,

                        missionStatus =
                            mission.status,

                        mode =
                            if (
                                existingArea != null
                            ) {
                                MissionAreaMode.DEFINED
                            } else {
                                MissionAreaMode.FREE
                            },

                        polygonPoints =
                            existingArea
                                ?.polygonPoints
                                .orEmpty(),

                        mapLatitude =
                            mapLatitude,

                        mapLongitude =
                            mapLongitude,

                        isLoading =
                            false,

                        isSaved =
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
                                .mission_area_load_error
                    )
                }
            }
        }
    }

    // =========================================================
    // Eventos
    // =========================================================

    fun onEvent(
        event: MissionAreaEvent
    ) {

        if (
            !_uiState.value.canEdit
        ) {
            return
        }

        when (
            event
        ) {

            is MissionAreaEvent.SelectMode -> {

                selectMode(
                    event.mode
                )
            }

            is MissionAreaEvent.AddPoint -> {

                addPoint(
                    latitude =
                        event.latitude,

                    longitude =
                        event.longitude
                )
            }

            MissionAreaEvent.UndoLastPoint -> {

                undoLastPoint()
            }

            MissionAreaEvent.ClearArea -> {

                clearArea()
            }

            MissionAreaEvent.Save -> {

                save()
            }
        }
    }

    // =========================================================
    // Tipo da área
    // =========================================================

    private fun selectMode(
        mode: MissionAreaMode
    ) {

        _uiState.update {
            it.copy(
                mode =
                    mode,

                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Adicionar ponto
    // =========================================================

    private fun addPoint(
        latitude: Double,
        longitude: Double
    ) {

        val state =
            _uiState.value

        if (
            state.mode !=
            MissionAreaMode.DEFINED
        ) {
            return
        }

        val point =
            MissionCoordinate(
                latitude =
                    latitude,

                longitude =
                    longitude
            )

        _uiState.update {
            it.copy(
                polygonPoints =
                    it.polygonPoints +
                            point,

                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Desfazer
    // =========================================================

    private fun undoLastPoint() {

        _uiState.update { state ->

            if (
                state.polygonPoints
                    .isEmpty()
            ) {

                state

            } else {

                state.copy(
                    polygonPoints =
                        state
                            .polygonPoints
                            .dropLast(
                                1
                            ),

                    errorMessage =
                        null
                )
            }
        }
    }

    // =========================================================
    // Limpar
    // =========================================================

    private fun clearArea() {

        _uiState.update {
            it.copy(
                polygonPoints =
                    emptyList(),

                errorMessage =
                    null
            )
        }
    }

    // =========================================================
    // Salvar
    // =========================================================

    private fun save() {

        val state =
            _uiState.value

        if (
            state.missionId
                .isBlank()
        ) {
            return
        }

        val area =
            when (
                state.mode
            ) {

                MissionAreaMode.FREE -> {

                    null
                }

                MissionAreaMode.DEFINED -> {

                    if (
                        state.polygonPoints.size <
                        3
                    ) {

                        _uiState.update {
                            it.copy(
                                errorMessage =
                                    R.string
                                        .mission_area_minimum_points_error
                            )
                        }

                        return
                    }

                    MissionArea(
                        polygonPoints =
                            state.polygonPoints
                    )
                }
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

            updateMissionAreaUseCase(
                missionId =
                    state.missionId,

                area =
                    area
            )
                .onSuccess {

                    _uiState.update {
                        it.copy(

                            polygonPoints =
                                area
                                    ?.polygonPoints
                                    .orEmpty(),

                            mode =
                                if (
                                    area == null
                                ) {
                                    MissionAreaMode.FREE
                                } else {
                                    MissionAreaMode.DEFINED
                                },

                            isSaving =
                                false,

                            isSaved =
                                true,

                            errorMessage =
                                null
                        )
                    }

                    snackbarManager.show(
                        IdeSnackbarMessage(
                            messageRes =
                                R.string
                                    .mission_area_save_success,

                            type =
                                IdeSnackbarType.SUCCESS
                        )
                    )
                }
        }
    }
}