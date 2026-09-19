package br.com.ide.presentation.feature.home

import br.com.ide.domain.model.MissionStatus

sealed interface HomeEffect {

    data class OpenMission(
        val missionId: String,
        val missionStatus: MissionStatus
    ) : HomeEffect
}
