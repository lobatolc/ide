package br.com.ide.presentation.feature.missionarea

import androidx.annotation.StringRes
import br.com.ide.domain.model.MissionCoordinate
import br.com.ide.domain.model.MissionStatus

data class MissionAreaUiState(

    val missionId: String = "",
    val missionName: String = "",

    val missionStatus: MissionStatus? = null,

    val mode: MissionAreaMode =
        MissionAreaMode.FREE,

    val polygonPoints: List<MissionCoordinate> =
        emptyList(),

    val mapLatitude: Double =
        -1.2939,

    val mapLongitude: Double =
        -47.9260,

    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,

    @StringRes
    val errorMessage: Int? = null

) {

    val canEdit: Boolean
        get() =
            missionStatus ==
                    MissionStatus.PLANNING ||
                    missionStatus ==
                    MissionStatus.SCHEDULED

    val isFreeArea: Boolean
        get() =
            mode ==
                    MissionAreaMode.FREE

    val isDefinedArea: Boolean
        get() =
            mode ==
                    MissionAreaMode.DEFINED

    val canSaveDefinedArea: Boolean
        get() =
            polygonPoints.size >= 3

    val canUndo: Boolean
        get() =
            polygonPoints.isNotEmpty()
}