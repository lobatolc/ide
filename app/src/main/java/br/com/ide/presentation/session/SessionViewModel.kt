package br.com.ide.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.domain.repository.AuthRepository
import br.com.ide.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _sessionState =
        MutableStateFlow<SessionState>(
            SessionState.Loading
        )

    val sessionState: StateFlow<SessionState> =
        _sessionState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {

            _sessionState.value =
                SessionState.Loading

            if (!authRepository.isUserLoggedIn()) {

                _sessionState.value =
                    SessionState.LoggedOut

                return@launch
            }

            val userId =
                authRepository.getCurrentUserId()

            if (userId.isNullOrBlank()) {

                authRepository.logout()

                _sessionState.value =
                    SessionState.LoggedOut

                return@launch
            }

            userRepository
                .userExists(userId)
                .onSuccess { exists ->

                    _sessionState.value =
                        if (exists) {
                            SessionState.LoggedIn
                        } else {
                            SessionState.NeedsRegistration
                        }
                }
                .onFailure {

                    _sessionState.value =
                        SessionState.LoggedOut
                }
        }
    }

    fun logout() {
        authRepository.logout()

        _sessionState.value =
            SessionState.LoggedOut
    }
}