package br.com.ide.presentation.feature.createmission

sealed interface CreateMissionEffect {

    data class MissionCreated(
        val missionId: String
    ) : CreateMissionEffect
}