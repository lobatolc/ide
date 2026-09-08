package br.com.ide.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.ide.presentation.model.AppLanguage
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
        val LANGUAGE_KEY =
            stringPreferencesKey("app_language")
    }

    val language: Flow<AppLanguage> =
        context.dataStore.data.map { preferences ->

            val languageTag =
                preferences[LANGUAGE_KEY]

            AppLanguage.entries.firstOrNull {
                it.languageTag == languageTag
            } ?: AppLanguage.PORTUGUESE
        }

    suspend fun setLanguage(
        language: AppLanguage
    ) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] =
                language.languageTag
        }
    }
}