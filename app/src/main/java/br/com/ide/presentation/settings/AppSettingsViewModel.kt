package br.com.ide.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.data.preferences.AppPreferencesRepository
import br.com.ide.presentation.model.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val preferencesRepository:
    AppPreferencesRepository
) : ViewModel() {

    val language: StateFlow<AppLanguage> =
        preferencesRepository.language.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                5_000
            ),
            initialValue =
                AppLanguage.PORTUGUESE
        )

    fun changeLanguage(
        language: AppLanguage
    ) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(
                language
            )
        }
    }
}