package br.com.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import br.com.ide.presentation.locale.ProvideLocalizedContext
import br.com.ide.presentation.navigation.AppNavGraph
import br.com.ide.presentation.navigation.CompleteRegistration
import br.com.ide.presentation.navigation.Home
import br.com.ide.presentation.navigation.Login
import br.com.ide.presentation.session.SessionState
import br.com.ide.presentation.session.SessionViewModel
import br.com.ide.presentation.settings.AppSettingsViewModel
import br.com.ide.presentation.theme.IdeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            val settingsViewModel:
                    AppSettingsViewModel =
                hiltViewModel()

            val sessionViewModel:
                    SessionViewModel =
                hiltViewModel()

            val language by
            settingsViewModel.language
                .collectAsStateWithLifecycle()

            val theme by
            settingsViewModel.theme
                .collectAsStateWithLifecycle()

            val sessionState by
            sessionViewModel.sessionState
                .collectAsStateWithLifecycle()

            ProvideLocalizedContext(
                language = language
            ) {

                IdeTheme(
                    appTheme = theme
                ) {

                    val navController =
                        rememberNavController()

                    when (sessionState) {

                        SessionState.Loading -> {

                            Box(
                                modifier =
                                    Modifier.fillMaxSize(),
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                CircularProgressIndicator(
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )
                            }
                        }

                        SessionState.LoggedOut -> {

                            AppNavGraph(
                                navController =
                                    navController,

                                appLanguage =
                                    language,

                                onLanguageChanged =
                                    settingsViewModel::changeLanguage,

                                appTheme =
                                    theme,

                                onThemeChanged =
                                    settingsViewModel::changeTheme,

                                startDestination =
                                    Login,

                                onSessionChanged =
                                    sessionViewModel::checkSession,

                                onLogout =
                                    sessionViewModel::logout
                            )
                        }

                        SessionState.NeedsRegistration -> {

                            AppNavGraph(
                                navController =
                                    navController,

                                appLanguage =
                                    language,

                                onLanguageChanged =
                                    settingsViewModel::changeLanguage,

                                appTheme =
                                    theme,

                                onThemeChanged =
                                    settingsViewModel::changeTheme,

                                startDestination =
                                    CompleteRegistration,

                                onSessionChanged =
                                    sessionViewModel::checkSession,

                                onLogout =
                                    sessionViewModel::logout
                            )
                        }

                        SessionState.LoggedIn -> {

                            AppNavGraph(
                                navController =
                                    navController,

                                appLanguage =
                                    language,

                                onLanguageChanged =
                                    settingsViewModel::changeLanguage,

                                appTheme =
                                    theme,

                                onThemeChanged =
                                    settingsViewModel::changeTheme,

                                startDestination =
                                    Home,

                                onSessionChanged =
                                    sessionViewModel::checkSession,

                                onLogout =
                                    sessionViewModel::logout
                            )
                        }
                    }
                }
            }
        }
    }
}