package br.com.ide.presentation.feature.home

sealed interface HomeEvent {

    data object LoadHome : HomeEvent

    data object Retry : HomeEvent

}