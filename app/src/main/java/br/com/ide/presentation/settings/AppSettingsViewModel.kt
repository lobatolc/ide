package br.com.ide.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.ide.data.preferences.AppPreferencesRepository
import br.com.ide.presentation.model.AppLanguage
import br.com.ide.presentation.model.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val appPreferencesRepository:
    AppPreferencesRepository
) : ViewModel() {

    val language: StateFlow<AppLanguage> =
        appPreferencesRepository
            .language
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted
                        .WhileSubscribed(5_000),
                initialValue =
                    AppLanguage.PORTUGUESE
            )

    val theme: StateFlow<AppTheme> =
        appPreferencesRepository
            .theme
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted
                        .WhileSubscribed(5_000),
                initialValue =
                    AppTheme.SYSTEM
            )

    fun changeLanguage(
        language: AppLanguage
    ) {
        viewModelScope.launch {
            appPreferencesRepository
                .setLanguage(language)
        }
    }

    fun changeTheme(
        theme: AppTheme
    ) {
        viewModelScope.launch {
            appPreferencesRepository
                .setTheme(theme)
        }
    }
}