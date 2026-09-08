package br.com.ide.presentation.session

import androidx.lifecycle.ViewModel
import br.com.ide.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}