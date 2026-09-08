package br.com.ide.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.ide.presentation.model.AppLanguage
import br.com.ide.presentation.model.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(
    name = "app_preferences"
)

@Singleton
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    private companion object {

        val APP_LANGUAGE =
            stringPreferencesKey(
                "app_language"
            )

        val APP_THEME =
            stringPreferencesKey(
                "app_theme"
            )
    }

    val language: Flow<AppLanguage> =
        context.dataStore.data.map { preferences ->

            val savedLanguage =
                preferences[
                    APP_LANGUAGE
                ]

            runCatching {
                AppLanguage.valueOf(
                    savedLanguage
                        ?: AppLanguage
                            .PORTUGUESE
                            .name
                )
            }.getOrDefault(
                AppLanguage.PORTUGUESE
            )
        }

    val theme: Flow<AppTheme> =
        context.dataStore.data.map { preferences ->

            val savedTheme =
                preferences[
                    APP_THEME
                ]

            runCatching {
                AppTheme.valueOf(
                    savedTheme
                        ?: AppTheme
                            .SYSTEM
                            .name
                )
            }.getOrDefault(
                AppTheme.SYSTEM
            )
        }

    suspend fun setLanguage(
        language: AppLanguage
    ) {
        context.dataStore.edit { preferences ->

            preferences[
                APP_LANGUAGE
            ] = language.name
        }
    }

    suspend fun setTheme(
        theme: AppTheme
    ) {
        context.dataStore.edit { preferences ->

            preferences[
                APP_THEME
            ] = theme.name
        }
    }
}