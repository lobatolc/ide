package br.com.ide.presentation.feature.missionlocations

import androidx.annotation.StringRes
import br.com.ide.domain.model.MissionLocation
import br.com.ide.domain.model.MissionStatus

data class MissionLocationsUiState(

    // Missão
    val missionId: String = "",
    val missionName: String = "",
    val missionStatus: MissionStatus? = null,

    // Locais
    val departureLocation: MissionLocation? = null,
    val returnLocation: MissionLocation? = null,

    // Seleção atual no mapa
    val selectedLocationType:
    MissionLocationType =
        MissionLocationType.DEPARTURE,

    // Controle
    val isLoading: Boolean = false,
    val isGeocoding: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,

    @StringRes
    val errorMessage: Int? = null
)