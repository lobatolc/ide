package br.com.ide.presentation.feature.home

import br.com.ide.domain.model.MissionStatus

sealed interface HomeEvent {

    data object LoadMissions : HomeEvent

    data class SearchChanged(
        val query: String
    ) : HomeEvent

    data class StatusSelected(
        val status: MissionStatus?
    ) : HomeEvent

    data object Retry : HomeEvent
}