package br.com.ide.presentation.session

sealed interface SessionState {

    data object Loading : SessionState

    data object LoggedOut : SessionState

    data object NeedsRegistration : SessionState

    data object LoggedIn : SessionState
}